/*
 * Copyright 2011 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.impl.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.addressable.AddressableRegistry.DuplicateAddressException;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.counter.CounterSynchronizer;
import com.eviware.loadui.api.dispatch.ExecutorManager;
import com.eviware.loadui.api.events.TerminalMessageEvent;
import com.eviware.loadui.api.messaging.BroadcastMessageEndpoint;
import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.messaging.SceneCommunication;
import com.eviware.loadui.api.messaging.ServerEndpoint;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.property.PropertySynchronizer;
import com.eviware.loadui.api.terminal.DualTerminal;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.api.terminal.TerminalProxy;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.dispatch.CustomThreadPoolExecutor;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class ControllerImpl
{
	public static final Logger log = LoggerFactory.getLogger( ControllerImpl.class );

	// private final Map<String, SceneItem> scenes = new HashMap<String,
	// SceneItem>();
	private final ExecutorService executorService;
	private final ExecutorManager executorManager;
	private final ConversionService conversionService;
	private final TerminalProxy terminalProxy;
	private final AddressableRegistry addressableRegistry;
	private final PropertySynchronizer propertySynchronizer;
	private final CounterSynchronizer counterSynchronizer;
	private final BroadcastMessageEndpoint broadcastEndpoint;

	private final Map<String, SceneAgent> sceneAgents = Collections.synchronizedMap( new HashMap<String, SceneAgent>() );
	private final Map<String, AgentProjectItem> projects = new HashMap<String, AgentProjectItem>();
	private final Set<MessageEndpoint> clients = new HashSet<MessageEndpoint>();

	public ControllerImpl( ScheduledExecutorService scheduledExecutorService, ExecutorManager executorManager,
			ConversionService conversionService, ServerEndpoint serverEndpoint, TerminalProxy terminalProxy,
			AddressableRegistry addressableRegistry, PropertySynchronizer propertySynchronizer,
			CounterSynchronizer counterSynchronizer, BroadcastMessageEndpoint broadcastEndpoint )
	{
		this.executorManager = executorManager;
		this.executorService = executorManager.getExecutor();
		this.conversionService = conversionService;
		this.terminalProxy = terminalProxy;
		this.addressableRegistry = addressableRegistry;
		this.propertySynchronizer = propertySynchronizer;
		this.counterSynchronizer = counterSynchronizer;
		this.broadcastEndpoint = broadcastEndpoint;

		serverEndpoint.addConnectionListener( new ConnectionListener()
		{
			@Override
			public void handleConnectionChange( final MessageEndpoint endpoint, boolean connected )
			{
				if( connected )
				{
					if( clients.add( endpoint ) )
					{
						log.info( "Client connected" );
						ControllerImpl.this.broadcastEndpoint.registerEndpoint( endpoint );
						AgentListener agentListener = new AgentListener();
						endpoint.addConnectionListener( agentListener );
						endpoint.addMessageListener( AgentItem.AGENT_CHANNEL, agentListener );
						endpoint.addMessageListener( SceneCommunication.CHANNEL, new SceneListener() );
						endpoint.addMessageListener( ComponentContext.COMPONENT_CONTEXT_CHANNEL,
								new ComponentContextListener() );
						endpoint.sendMessage( AgentItem.AGENT_CHANNEL,
								Collections.singletonMap( AgentItem.CONNECTED, LoadUI.AGENT_VERSION ) );
					}
				}
				else
				{
					clients.remove( endpoint );
					for( Map.Entry<String, AgentProjectItem> entry : Iterables.filter( projects.entrySet(),
							new Predicate<Map.Entry<String, AgentProjectItem>>()
							{
								@Override
								public boolean apply( Map.Entry<String, AgentProjectItem> input )
								{
									return input.getValue().getEndpoint() == endpoint;
								}
							} ) )
					{
						for( SceneItem scene : entry.getValue().getScenes() )
						{
							SceneAgent sceneAgent = sceneAgents.get( scene.getId() );
							if( sceneAgent != null )
							{
								sceneAgent.addCommand( Collections.singletonList( ( String )null ) );
							}
						}
						ReleasableUtils.release( entry.getValue() );
						projects.remove( entry.getKey() );
					}
					ControllerImpl.this.broadcastEndpoint.deregisterEndpoint( endpoint );
					log.info( "Client disconnected" );
				}
			}
		} );
		for( MessageEndpoint endpoint : serverEndpoint.getConnectedEndpoints() )
		{
			if( clients.add( endpoint ) )
			{
				log.info( "Client connected" );
				ControllerImpl.this.broadcastEndpoint.registerEndpoint( endpoint );
				AgentListener agentListener = new AgentListener();
				endpoint.addConnectionListener( agentListener );
				endpoint.addMessageListener( AgentItem.AGENT_CHANNEL, agentListener );
				endpoint.addMessageListener( SceneCommunication.CHANNEL, new SceneListener() );
				endpoint.addMessageListener( ComponentContext.COMPONENT_CONTEXT_CHANNEL, new ComponentContextListener() );
				endpoint.sendMessage( AgentItem.AGENT_CHANNEL,
						Collections.singletonMap( AgentItem.CONNECTED, LoadUI.AGENT_VERSION ) );
			}
		}

		scheduledExecutorService.scheduleAtFixedRate( new Runnable()
		{
			int lastUtilization = -1;

			@Override
			public void run()
			{
				if( executorService instanceof CustomThreadPoolExecutor )
				{
					int utilization = ( ( CustomThreadPoolExecutor )executorService ).getUtilization();
					if( lastUtilization != utilization )
					{
						for( MessageEndpoint endpoint : clients )
						{
							endpoint.sendMessage( AgentItem.AGENT_CHANNEL,
									Collections.singletonMap( AgentItem.SET_UTILIZATION, utilization ) );
						}
						lastUtilization = utilization;
					}
				}
			}
		}, 1, 1, TimeUnit.SECONDS );
	}

	private class AgentListener implements MessageListener, ConnectionListener
	{
		@Override
		@SuppressWarnings( "unchecked" )
		public void handleMessage( String channel, final MessageEndpoint endpoint, Object data )
		{
			// log.debug( "handleMessage got data: {} on channel {}", data, channel
			// );
			Map<String, String> message = ( Map<String, String> )data;
			if( message.containsKey( AgentItem.TIME_CHECK ) )
			{
				Map<String, String> newMessage = Maps.newHashMap( message );
				newMessage.put( AgentItem.TIME_CHECK, String.valueOf( System.currentTimeMillis() ) );
				endpoint.sendMessage( AgentItem.AGENT_CHANNEL, newMessage );
			}
			if( message.containsKey( AgentItem.CONNECTED ) )
			{
				log.info( "Client connected: {}", message.get( AgentItem.CONNECTED ) );
			}
			if( message.containsKey( AgentItem.SET_MAX_THREADS ) )
			{
				executorManager.setMaxPoolSize( Integer.parseInt( message.get( AgentItem.SET_MAX_THREADS ) ) );
			}
			else if( message.containsKey( AgentItem.ASSIGN ) )
			{
				String projectId = message.get( AgentItem.PROJECT_ID );
				String sceneId = message.get( AgentItem.ASSIGN );
				synchronized( sceneAgents )
				{
					if( !projects.containsKey( projectId ) )
					{
						try
						{
							projects.put( projectId, new AgentProjectItem( endpoint, projectId ) );
						}
						catch( DuplicateAddressException e )
						{
							log.error( "Unable to create Project", e );
						}
					}
					if( !sceneAgents.containsKey( sceneId ) )
					{
						SceneItem scene = ( SceneItem )addressableRegistry.lookup( sceneId );
						if( scene != null )
						{
							log.warn( "Attempted to load an already existing TestCase! Force releasing old TestCase..." );
							ReleasableUtils.release( scene );
							addressableRegistry.unregister( scene );
						}
						log.info( "Loading SceneItem {}", sceneId );
						executorService.execute( new SceneAgent( sceneId, endpoint, projects.get( projectId ) ) );
					}
				}
			}
			else if( message.containsKey( AgentItem.UNASSIGN ) )
			{
				SceneAgent sceneAgent = sceneAgents.get( message.get( AgentItem.UNASSIGN ) );
				if( sceneAgent != null )
				{
					sceneAgent.addCommand( Collections.singletonList( ( String )null ) );
				}
			}
			else if( message.containsKey( AgentItem.SCENE_DEFINITION ) )
			{
				log.debug( "Got TestCase Definition for: {}", message.get( AgentItem.SCENE_ID ) );
				SceneAgent sceneAgent = sceneAgents.get( message.get( AgentItem.SCENE_ID ) );
				if( sceneAgent != null )
				{
					synchronized( sceneAgent )
					{
						sceneAgent.sceneDef = message.get( AgentItem.SCENE_DEFINITION );
						sceneAgent.notifyAll();
					}
				}
				else
					log.warn( "No SceneAgent for TestCase: {}", message.get( AgentItem.SCENE_ID ) );
			}
		}

		@Override
		public void handleConnectionChange( final MessageEndpoint endpoint, boolean connected )
		{
			if( !connected )
			{
				Iterable<SceneAgent> agents = Iterables.filter( ImmutableList.copyOf( sceneAgents.values() ),
						new Predicate<SceneAgent>()
						{
							@Override
							public boolean apply( SceneAgent input )
							{
								return input.getEndpoint() == endpoint;
							}
						} );

				for( SceneAgent sceneAgent : agents )
				{
					sceneAgent.addCommand( Collections.<String> singletonList( null ) );
				}
			}
		}
	}

	private class SceneAgent implements Runnable
	{
		private final AgentProjectItem project;
		private final String sceneId;
		private final MessageEndpoint endpoint;
		private final BlockingQueue<List<String>> commands = new LinkedBlockingQueue<List<String>>();
		private String sceneDef;
		private SceneItem scene;

		public SceneAgent( String sceneId, MessageEndpoint endpoint, AgentProjectItem project )
		{
			this.project = project;
			this.sceneId = sceneId;
			this.endpoint = endpoint;
			sceneAgents.put( sceneId, this );
		}

		public MessageEndpoint getEndpoint()
		{
			return endpoint;
		}

		public void addCommand( List<String> args )
		{
			try
			{
				commands.put( args );
			}
			catch( InterruptedException e )
			{
				e.printStackTrace();
			}
		}

		@Override
		public void run()
		{
			// Load SceneItem
			int tries = 0;
			synchronized( this )
			{
				while( sceneDef == null && tries < 5 )
				{
					log.debug( "REQUESTING DEFINITION: {} from: {}", sceneId, endpoint );
					endpoint.sendMessage( AgentItem.AGENT_CHANNEL,
							Collections.singletonMap( AgentItem.DEFINE_SCENE, sceneId ) );
					try
					{
						wait( ( long )Math.pow( 2, tries++ ) * 1000 );
					}
					catch( InterruptedException e )
					{
					}
				}

				if( sceneDef == null )
				{
					log.error( "Unable to get TestCase definition: {} from endpoint: {}", sceneId, endpoint );
					synchronized( sceneAgents )
					{
						sceneAgents.remove( sceneId );
					}
					return;
				}

				scene = conversionService.convert( sceneDef, SceneItem.class );
			}

			// TODO: This is really ugly and should be fixed when there is time.
			try
			{
				scene.getClass().getMethod( "setMessageEndpoint", MessageEndpoint.class ).invoke( scene, endpoint );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}

			project.addScene( scene );

			propertySynchronizer.syncProperties( scene, endpoint );
			counterSynchronizer.syncCounters( scene, endpoint );
			for( ComponentItem component : scene.getComponents() )
			{
				propertySynchronizer.syncProperties( component, endpoint );
				counterSynchronizer.syncCounters( component, endpoint );
			}

			endpoint.sendMessage( AgentItem.AGENT_CHANNEL, Collections.singletonMap( AgentItem.STARTED, sceneId ) );
			log.info( "Started scene: {}", scene.getLabel() );

			while( true )
			{
				try
				{
					List<String> args = commands.take();
					if( args.get( 0 ) == null )
					{
						log.info( "Stopping scene: {}", scene.getLabel() );
						project.removeScene( scene );
						ReleasableUtils.release( scene );
						synchronized( sceneAgents )
						{
							sceneAgents.remove( sceneId );
						}
						return;
					}
					else if( scene.getVersion() > Long.parseLong( args.get( 1 ) ) )
					{
						log.debug( "SceneItem out of sync with controller, restarting..." );
						project.removeScene( scene );
						ReleasableUtils.release( scene );
						synchronized( sceneAgents )
						{
							sceneAgents.remove( sceneId );
						}
						executorService.execute( new SceneAgent( sceneId, endpoint, project ) );
						return;
					}
					else if( SceneCommunication.LABEL.equals( args.get( 2 ) ) )
					{
						scene.setLabel( args.get( 3 ) );
					}
					else if( SceneCommunication.ADD_COMPONENT.equals( args.get( 2 ) ) )
					{
						ComponentItem component = conversionService.convert( args.get( 3 ), ComponentItem.class );
						propertySynchronizer.syncProperties( component, endpoint );
						counterSynchronizer.syncCounters( component, endpoint );
					}
					else if( SceneCommunication.REMOVE_COMPONENT.equals( args.get( 2 ) ) )
					{
						ComponentItem component = ( ComponentItem )addressableRegistry.lookup( args.get( 3 ) );
						component.delete();
					}
					else if( SceneCommunication.CONNECT.equals( args.get( 2 ) ) )
					{
						OutputTerminal output = ( OutputTerminal )addressableRegistry.lookup( args.get( 3 ) );
						InputTerminal input = ( InputTerminal )addressableRegistry.lookup( args.get( 4 ) );
						scene.connect( output, input );
					}
					else if( SceneCommunication.DISCONNECT.equals( args.get( 2 ) ) )
					{
						OutputTerminal output = ( OutputTerminal )addressableRegistry.lookup( args.get( 3 ) );
						InputTerminal input = ( InputTerminal )addressableRegistry.lookup( args.get( 4 ) );
						scene.connect( output, input ).disconnect();
					}
					else if( SceneCommunication.EXPORT.equals( args.get( 2 ) ) )
					{
						OutputTerminal terminal = ( OutputTerminal )addressableRegistry.lookup( args.get( 3 ) );
						scene.exportTerminal( terminal );
						terminalProxy.export( terminal );
					}
					else if( SceneCommunication.UNEXPORT.equals( args.get( 2 ) ) )
					{
						OutputTerminal terminal = ( OutputTerminal )addressableRegistry.lookup( args.get( 3 ) );
						scene.unexportTerminal( terminal );
						terminalProxy.unexport( terminal );
					}
					else if( SceneCommunication.ACTION_EVENT.equals( args.get( 2 ) ) )
					{
						( ( ModelItem )addressableRegistry.lookup( args.get( 4 ) ) ).triggerAction( args.get( 3 ) );
					}
					else if( SceneCommunication.CANCEL_COMPONENTS.equals( args.get( 2 ) ) )
					{
						scene.cancelComponents();
					}
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
				}
			}
		}
	}

	private class SceneListener implements MessageListener
	{
		@Override
		public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
		{
			final List<String> args = new ArrayList<String>();
			for( Object arg : ( Object[] )data )
				args.add( ( String )arg );

			log.debug( "Got command: {}", args.get( 2 ) );

			SceneAgent sceneAgent = sceneAgents.get( args.get( 0 ) );
			if( sceneAgent != null )
				sceneAgent.addCommand( args );
		}
	}

	private class ComponentContextListener implements MessageListener
	{
		@Override
		public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
		{
			Object[] args = ( Object[] )data;
			ComponentItem target = ( ComponentItem )addressableRegistry.lookup( ( String )args[0] );
			DualTerminal remoteTerminal = target.getContext().getRemoteTerminal();
			TerminalMessage message = target.getContext().newMessage();
			message.load( args[1] );
			target.handleTerminalEvent( remoteTerminal, new TerminalMessageEvent( remoteTerminal, message ) );
		}
	}
}
