/*
 * Copyright 2011 eviware software ab
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

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.counter.CounterSynchronizer;
import com.eviware.loadui.api.dispatch.ExecutorManager;
import com.eviware.loadui.api.events.TerminalMessageEvent;
import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.messaging.SceneCommunication;
import com.eviware.loadui.api.messaging.ServerEndpoint;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.property.PropertySynchronizer;
import com.eviware.loadui.api.terminal.DualTerminal;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.api.terminal.TerminalProxy;
import com.eviware.loadui.impl.statistics.StreamingExecutionManager;
import com.eviware.loadui.util.dispatch.CustomThreadPoolExecutor;

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
	private final StreamingExecutionManager streamingExecutionManager;

	private final Map<String, SceneAgent> sceneAgents = Collections.synchronizedMap( new HashMap<String, SceneAgent>() );
	private final Set<MessageEndpoint> clients = new HashSet<MessageEndpoint>();

	public ControllerImpl( ScheduledExecutorService scheduledExecutorService, ExecutorManager executorManager,
			ConversionService conversionService, ServerEndpoint serverEndpoint, TerminalProxy terminalProxy,
			AddressableRegistry addressableRegistry, PropertySynchronizer propertySynchronizer,
			CounterSynchronizer counterSynchronizer, StreamingExecutionManager streamingExecutionManager )
	{
		this.executorManager = executorManager;
		this.executorService = executorManager.getExecutor();
		this.conversionService = conversionService;
		this.terminalProxy = terminalProxy;
		this.addressableRegistry = addressableRegistry;
		this.propertySynchronizer = propertySynchronizer;
		this.counterSynchronizer = counterSynchronizer;
		this.streamingExecutionManager = streamingExecutionManager;

		serverEndpoint.addConnectionListener( new ConnectionListener()
		{
			@Override
			public void handleConnectionChange( MessageEndpoint endpoint, boolean connected )
			{
				if( connected )
				{
					clients.add( endpoint );
					ControllerImpl.this.streamingExecutionManager.addEndpoint( endpoint );
					AgentListener agentListener = new AgentListener();
					endpoint.addConnectionListener( agentListener );
					endpoint.addMessageListener( AgentItem.AGENT_CHANNEL, agentListener );
					endpoint.addMessageListener( SceneCommunication.CHANNEL, new SceneListener() );
					endpoint.addMessageListener( ComponentContext.COMPONENT_CONTEXT_CHANNEL, new ComponentContextListener() );
					endpoint.sendMessage( AgentItem.AGENT_CHANNEL, Collections.singletonMap( AgentItem.CONNECTED, null ) );
				}
				else
				{
					clients.remove( endpoint );
					ControllerImpl.this.streamingExecutionManager.removeEndpoint( endpoint );
					log.info( "Client disconnected" );
				}
			}
		} );
		for( MessageEndpoint endpoint : serverEndpoint.getConnectedEndpoints() )
		{
			if( !clients.contains( endpoint ) )
			{
				clients.add( endpoint );
				ControllerImpl.this.streamingExecutionManager.addEndpoint( endpoint );
				AgentListener agentListener = new AgentListener();
				endpoint.addConnectionListener( agentListener );
				endpoint.addMessageListener( AgentItem.AGENT_CHANNEL, agentListener );
				endpoint.addMessageListener( SceneCommunication.CHANNEL, new SceneListener() );
				endpoint.addMessageListener( ComponentContext.COMPONENT_CONTEXT_CHANNEL, new ComponentContextListener() );
				endpoint.sendMessage( AgentItem.AGENT_CHANNEL, Collections.singletonMap( AgentItem.CONNECTED, null ) );
			}
		}

		scheduledExecutorService.scheduleAtFixedRate( new Runnable()
		{

			@Override
			public void run()
			{
				if( executorService instanceof CustomThreadPoolExecutor )
				{
					int utilization = ( ( CustomThreadPoolExecutor )executorService ).getUtilization();
					for( MessageEndpoint endpoint : clients )
						endpoint.sendMessage( AgentItem.AGENT_CHANNEL,
								Collections.singletonMap( AgentItem.SET_UTILIZATION, utilization ) );
				}
			}
		}, 1, 1, TimeUnit.SECONDS );

		log.info( "Agent started and listening on cometd!" );
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
				String sceneId = message.get( AgentItem.ASSIGN );
				if( !sceneAgents.containsKey( sceneId ) )
				{
					log.info( "Loading SceneItem {}", sceneId );
					executorService.execute( new SceneAgent( sceneId, endpoint ) );
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
				if( addressableRegistry.lookup( message.get( AgentItem.SCENE_ID ) ) == null )
					sceneAgents.get( message.get( AgentItem.SCENE_ID ) ).sceneDef = message.get( AgentItem.SCENE_DEFINITION );
			}
		}

		@Override
		public void handleConnectionChange( MessageEndpoint endpoint, boolean connected )
		{
			if( !connected )
			{
				List<SceneAgent> stop = new ArrayList<SceneAgent>();
				synchronized( sceneAgents )
				{
					for( SceneAgent sceneAgent : sceneAgents.values() )
						if( endpoint == sceneAgent.getEndpoint() )
							stop.add( sceneAgent );
				}
				for( SceneAgent sceneAgent : stop )
					sceneAgent.addCommand( Collections.singletonList( ( String )null ) );
			}
		}
	}

	private class SceneAgent implements Runnable
	{
		private final String sceneId;
		private final MessageEndpoint endpoint;
		private final BlockingQueue<List<String>> commands = new LinkedBlockingQueue<List<String>>();
		private String sceneDef;
		private SceneItem scene;

		public SceneAgent( String sceneId, MessageEndpoint endpoint )
		{
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
			int tries = 10;
			while( sceneDef == null && tries > 0 )
			{
				log.debug( "REQUESTING DEFINITION: {} from: {}", sceneId, endpoint );
				endpoint.sendMessage( AgentItem.AGENT_CHANNEL, Collections.singletonMap( AgentItem.DEFINE_SCENE, sceneId ) );
				try
				{
					Thread.sleep( 1000 );
					tries-- ;
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
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

			// TODO: This is really ugly and should be fixed when there is time.
			try
			{
				scene.getClass().getMethod( "setMessageEndpoint", MessageEndpoint.class ).invoke( scene, endpoint );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}

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
						scene.release();
						synchronized( sceneAgents )
						{
							sceneAgents.remove( sceneId );
						}
						return;
					}
					else if( scene.getVersion() > Long.parseLong( args.get( 1 ) ) )
					{
						log.debug( "SceneItem out of sync with controller, restarting..." );
						scene.release();
						synchronized( sceneAgents )
						{
							sceneAgents.remove( sceneId );
						}
						executorService.execute( new SceneAgent( sceneId, endpoint ) );
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
