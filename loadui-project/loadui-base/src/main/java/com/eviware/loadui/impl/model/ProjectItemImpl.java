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
package com.eviware.loadui.impl.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.counter.CounterSynchronizer;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.CollectionEvent.Event;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.RemoteActionEvent;
import com.eviware.loadui.api.messaging.BroadcastMessageEndpoint;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.messaging.SceneCommunication;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.Assignment;
import com.eviware.loadui.api.model.CanvasObjectItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.property.PropertySynchronizer;
import com.eviware.loadui.api.statistics.model.StatisticPages;
import com.eviware.loadui.api.summary.MutableSummary;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.RoutedConnection;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.api.terminal.TerminalProxy;
import com.eviware.loadui.config.ComponentItemConfig;
import com.eviware.loadui.config.LoaduiProjectDocumentConfig;
import com.eviware.loadui.config.ProjectItemConfig;
import com.eviware.loadui.config.RoutedConnectionConfig;
import com.eviware.loadui.config.SceneAssignmentConfig;
import com.eviware.loadui.config.SceneItemConfig;
import com.eviware.loadui.impl.XmlBeansUtils;
import com.eviware.loadui.impl.counter.AggregatedCounterSupport;
import com.eviware.loadui.impl.statistics.model.StatisticPagesImpl;
import com.eviware.loadui.impl.summary.MutableChapterImpl;
import com.eviware.loadui.impl.summary.sections.ProjectDataSection;
import com.eviware.loadui.impl.summary.sections.ProjectDataSummarySection;
import com.eviware.loadui.impl.summary.sections.ProjectExecutionDataSection;
import com.eviware.loadui.impl.summary.sections.ProjectExecutionMetricsSection;
import com.eviware.loadui.impl.summary.sections.ProjectExecutionNotablesSection;
import com.eviware.loadui.impl.terminal.ConnectionImpl;
import com.eviware.loadui.impl.terminal.RoutedConnectionImpl;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.MapUtils;
import com.eviware.loadui.util.messaging.BroadcastMessageEndpointImpl;
import com.google.common.collect.ImmutableMap;

public class ProjectItemImpl extends CanvasItemImpl<ProjectItemConfig> implements ProjectItem
{
	public static final Logger log = LoggerFactory.getLogger( ProjectItemImpl.class );

	private final WorkspaceItem workspace;
	private final LoaduiProjectDocumentConfig doc;
	private final Set<Assignment> assignments = new HashSet<Assignment>();
	private final AgentListener agentListener = new AgentListener();
	private final SceneListener sceneListener = new SceneListener();
	private final WorkspaceListener workspaceListener = new WorkspaceListener();
	private final SceneComponentListener sceneComponentListener = new SceneComponentListener();
	private final Map<SceneItem, BroadcastMessageEndpoint> sceneEndpoints = new HashMap<SceneItem, BroadcastMessageEndpoint>();
	private final ConversionService conversionService;
	private final PropertySynchronizer propertySynchronizer;
	private final CounterSynchronizer counterSynchronizer;
	private final TerminalProxy proxy;
	private final Set<SceneItem> scenes = new HashSet<SceneItem>();
	private final StatisticPagesImpl statisticPages;
	private final Property<Boolean> saveReport;
	private final Property<String> reportFolder;
	private final Property<String> reportFormat;
	private final Property<Long> numberOfAutosaves;
	private File projectFile;

	public static ProjectItemImpl loadProject( WorkspaceItem workspace, File projectFile ) throws XmlException,
			IOException
	{
		ProjectItemImpl project = new ProjectItemImpl( workspace, projectFile,
				projectFile.exists() ? LoaduiProjectDocumentConfig.Factory.parse( projectFile )
						: LoaduiProjectDocumentConfig.Factory.newInstance() );
		project.init();

		return project;
	}

	private ProjectItemImpl( WorkspaceItem workspace, File projectFile, LoaduiProjectDocumentConfig doc )
	{
		super( doc.getLoaduiProject(), new AggregatedCounterSupport() );
		this.doc = doc;
		this.projectFile = projectFile;
		this.workspace = workspace;
		conversionService = BeanInjector.getBean( ConversionService.class );
		propertySynchronizer = BeanInjector.getBean( PropertySynchronizer.class );
		counterSynchronizer = BeanInjector.getBean( CounterSynchronizer.class );
		saveReport = createProperty( SAVE_REPORT_PROPERTY, Boolean.class, false );
		reportFolder = createProperty( REPORT_FOLDER_PROPERTY, String.class, "" );
		reportFormat = createProperty( REPORT_FORMAT_PROPERTY, String.class, "" );
		numberOfAutosaves = createProperty( STATISTIC_NUMBER_OF_AUTOSAVES, Long.class, 5L );
		proxy = BeanInjector.getBean( TerminalProxy.class );
		statisticPages = new StatisticPagesImpl( getConfig().getStatistics() == null ? getConfig().addNewStatistics()
				: getConfig().getStatistics() );

		// statisticHolderSupport = new StatisticHolderSupport( this );
		// counterStatisticSupport = new CounterStatisticSupport( this,
		// statisticHolderSupport );
	}

	@Override
	public void init()
	{
		for( SceneItemConfig conf : getConfig().getSceneArray() )
		{
			SceneItemImpl scene = new SceneItemImpl( this, conf );
			scene.init();
			attachScene( scene );
		}

		super.init();

		for( RoutedConnectionConfig conf : getConfig().getRoutedConnectionArray() )
			connections.add( new RoutedConnectionImpl( proxy, conf ) );

		workspace.addEventListener( BaseEvent.class, workspaceListener );

		for( AgentItem agent : workspace.getAgents() )
			agentListener.attach( agent );

		for( SceneAssignmentConfig conf : getConfig().getSceneAssignmentArray() )
		{
			SceneItem scene = ( SceneItem )addressableRegistry.lookup( conf.getSceneRef() );
			AgentItem agent = ( AgentItem )addressableRegistry.lookup( conf.getAgentRef() );
			if( agent == null && conf.isSetAgentAddress() )
			{
				for( AgentItem r : workspace.getAgents() )
				{
					if( r.getUrl().equals( conf.getAgentAddress() ) )
					{
						agent = r;
						break;
					}
				}
				if( agent == null && conf.isSetAgentLabel()
						&& ( Boolean )workspace.getProperty( WorkspaceItem.IMPORT_MISSING_AGENTS_PROPERTY ).getValue() )
				{
					agent = workspace.createAgent( conf.getAgentAddress(), conf.getAgentLabel() );
				}
			}

			if( agent != null )
			{
				sceneEndpoints.get( scene ).registerEndpoint( agent );
				AssignmentImpl assignment = new AssignmentImpl( scene, agent );
				if( assignments.add( assignment ) )
				{
					agent.sendMessage( AgentItem.AGENT_CHANNEL, Collections.singletonMap( AgentItem.ASSIGN, scene.getId() ) );
					fireCollectionEvent( ASSIGNMENTS, Event.ADDED, assignment );
				}
			}
		}

		statisticPages.init();
		// statisticHolderSupport.init();
	}

	private boolean attachScene( SceneItem scene )
	{
		if( scenes.add( scene ) )
		{
			scene.addEventListener( BaseEvent.class, sceneListener );
			( ( AggregatedCounterSupport )counterSupport ).addChild( scene );
			sceneEndpoints.put( scene, new BroadcastMessageEndpointImpl() );
			BroadcastMessageEndpoint sceneEndpoint = sceneEndpoints.get( scene );
			propertySynchronizer.syncProperties( scene, sceneEndpoint );
			counterSynchronizer.syncCounters( scene, sceneEndpoint );
			for( ComponentItem component : scene.getComponents() )
			{
				propertySynchronizer.syncProperties( component, sceneEndpoint );
				counterSynchronizer.syncCounters( component, sceneEndpoint );
				component.addEventListener( RemoteActionEvent.class, sceneComponentListener );
			}
			return true;
		}
		return false;
	}

	private void detachScene( SceneItem scene )
	{
		if( scenes.remove( scene ) )
		{
			log.debug( "Detaching {}", scene );
			( ( AggregatedCounterSupport )counterSupport ).removeChild( scene );
			for( AgentItem agent : getAgentsAssignedTo( scene ) )
			{
				log.debug( "Telling {} to stop scene {}", agent, scene );
				agent.sendMessage( AgentItem.AGENT_CHANNEL, Collections.singletonMap( AgentItem.UNASSIGN, scene.getId() ) );
			}

			scene.removeEventListener( BaseEvent.class, sceneListener );
			sceneEndpoints.remove( scene );
			fireCollectionEvent( SCENES, CollectionEvent.Event.REMOVED, scene );
		}
	}

	@Override
	public WorkspaceItem getWorkspace()
	{
		return workspace;
	}

	@Override
	public ProjectItem getProject()
	{
		return this;
	}

	@Override
	public Collection<SceneItem> getScenes()
	{
		return Collections.unmodifiableSet( scenes );
	}

	@Override
	public SceneItem getSceneByLabel( String label )
	{
		for( SceneItem scene : scenes )
			if( scene.getLabel().equals( label ) )
				return scene;

		return null;
	}

	@Override
	public SceneItem createScene( String label )
	{
		SceneItemConfig sceneConfig = getConfig().addNewScene();
		sceneConfig.setLabel( label );

		SceneItemImpl scene = new SceneItemImpl( this, sceneConfig );
		scene.init();
		if( attachScene( scene ) )
			fireCollectionEvent( SCENES, CollectionEvent.Event.ADDED, scene );
		return scene;
	}

	@Override
	protected Connection createConnection( OutputTerminal output, InputTerminal input )
	{
		return ( output.getTerminalHolder().getCanvas() == input.getTerminalHolder().getCanvas() ) ? new ConnectionImpl(
				getConfig().addNewConnection(), output, input ) : new RoutedConnectionImpl( proxy, getConfig()
				.addNewRoutedConnection(), output, input );
	}

	@Override
	public File getProjectFile()
	{
		return projectFile;
	}

	@Override
	public void save()
	{
		try
		{
			log.info( "Saving Project {}...", getLabel() );

			if( !projectFile.exists() )
				projectFile.createNewFile();

			XmlBeansUtils.saveToFile( doc, projectFile );
			lastSavedHash = DigestUtils.md5Hex( getConfig().xmlText() );
		}
		catch( IOException e )
		{
			log.error( "Unable to save project: " + getLabel(), e );
		}
	}

	public void saveAs( File saveAsFile )
	{
		try
		{
			log.info( "Saving Project {}...", getLabel() );

			if( !saveAsFile.exists() )
				saveAsFile.createNewFile();

			XmlBeansUtils.saveToFile( doc, saveAsFile );
		}
		catch( IOException e )
		{
			log.error( "Unable to save project: " + getLabel() + " to " + saveAsFile.getName(), e );
		}
	}

	@Override
	public void release()
	{
		getWorkspace().removeEventListener( BaseEvent.class, workspaceListener );
		agentListener.release();
		statisticPages.release();

		for( SceneItem scene : new ArrayList<SceneItem>( getScenes() ) )
		{
			scene.removeEventListener( BaseEvent.class, sceneListener );
			scene.release();
		}

		// statisticHolderSupport.release();

		super.release();
	}

	@Override
	public void delete()
	{
		for( SceneItem scene : new ArrayList<SceneItem>( getScenes() ) )
		{
			scene.delete();
		}
		release();
		projectFile.delete();
		super.delete();
	}

	@Override
	public void assignScene( SceneItem scene, AgentItem agent )
	{
		AssignmentImpl assignment = new AssignmentImpl( scene, agent );
		if( assignments.add( assignment ) )
		{
			sceneEndpoints.get( scene ).registerEndpoint( agent );
			SceneAssignmentConfig conf = getConfig().addNewSceneAssignment();
			String sceneId = scene.getId();
			conf.setSceneRef( sceneId );
			conf.setAgentRef( agent.getId() );
			conf.setAgentLabel( agent.getLabel() );
			conf.setAgentAddress( agent.getUrl() );
			agent.sendMessage( AgentItem.AGENT_CHANNEL, Collections.singletonMap( AgentItem.ASSIGN, sceneId ) );
			fireCollectionEvent( ASSIGNMENTS, Event.ADDED, assignment );
		}
	}

	@Override
	public Collection<AgentItem> getAgentsAssignedTo( SceneItem scene )
	{
		Set<AgentItem> agents = new HashSet<AgentItem>();
		for( Assignment assignment : assignments )
			if( scene.equals( assignment.getScene() ) )
				agents.add( assignment.getAgent() );
		return agents;
	}

	@Override
	public Collection<SceneItem> getScenesAssignedTo( AgentItem agent )
	{
		Set<SceneItem> scenes = new HashSet<SceneItem>();
		for( Assignment assignment : assignments )
			if( agent.equals( assignment.getAgent() ) )
				scenes.add( assignment.getScene() );
		return scenes;
	}

	@Override
	public Collection<Assignment> getAssignments()
	{
		return Collections.unmodifiableCollection( assignments );
	}

	@Override
	public void unassignScene( SceneItem scene, AgentItem agent )
	{
		AssignmentImpl assignment = new AssignmentImpl( scene, agent );
		if( assignments.remove( assignment ) )
		{
			BroadcastMessageEndpoint bme = sceneEndpoints.get( scene );
			if( bme != null )
			{
				bme.deregisterEndpoint( agent );
				agent.sendMessage( AgentItem.AGENT_CHANNEL, Collections.singletonMap( AgentItem.UNASSIGN, scene.getId() ) );
			}

			int size = getConfig().sizeOfSceneAssignmentArray();
			for( int i = 0; i < size; i++ )
			{
				SceneAssignmentConfig conf = getConfig().getSceneAssignmentArray()[i];
				if( scene.getId().equals( conf.getSceneRef() ) && agent.getId().equals( conf.getAgentRef() ) )
				{
					getConfig().removeSceneAssignment( i );
					break;
				}
			}
			fireCollectionEvent( ASSIGNMENTS, Event.REMOVED, assignment );
		}
	}

	@Override
	protected void disconnect( Connection connection )
	{
		if( connection instanceof RoutedConnection )
		{
			if( connections.remove( connection ) )
			{
				for( int i = getConfig().sizeOfRoutedConnectionArray() - 1; i >= 0; i-- )
				{
					RoutedConnectionConfig connConfig = getConfig().getRoutedConnectionArray( i );
					if( connection.getOutputTerminal().getId().equals( connConfig.getOutputTerminalId() )
							&& connection.getInputTerminal().getId().equals( connConfig.getInputTerminalId() ) )
					{
						getConfig().removeRoutedConnection( i );
					}
				}
				fireCollectionEvent( CONNECTIONS, CollectionEvent.Event.REMOVED, connection );
			}
		}
		else
			super.disconnect( connection );
	}

	private void sendSceneCommand( SceneItem scene, String... args )
	{
		List<String> message = new ArrayList<String>();
		message.add( scene.getId() );
		message.add( Long.toString( scene.getVersion() ) );
		for( String arg : args )
			message.add( arg );

		broadcastMessage( scene, SceneCommunication.CHANNEL, message );
	}

	@Override
	public void broadcastMessage( SceneItem scene, String channel, Object data )
	{
		// log.debug( "BROADCASTING: " + scene + " " + channel + " " + data );
		sceneEndpoints.get( scene ).sendMessage( channel, data );
	}

	@Override
	protected void onComplete( EventFirer source )
	{
		// At this point all project components are finished. They are either
		// canceled or finished normally which depends on project 'abortOnFinish'
		// property. So here we have to wait for all test cases to finish which we
		// do by listening for ON_COMPLETE_DONE event which is fired after
		// 'onComplete' method was called in local mode and after controller
		// received test case data in distributed mode.
		new SceneCompleteAwaiter();
	}

	@Override
	protected void reset()
	{
		super.reset();
	}

	@Override
	public void generateSummary( MutableSummary summary )
	{
		// add a project chapter first
		MutableChapterImpl projectChapter = ( MutableChapterImpl )summary.addChapter( getLabel() );

		// add and generate TestCase chapters if the TestCase has run at least
		// once.
		for( SceneItem scene : scenes )
			if( ( ( SceneItemImpl )scene ).getEndTime() != null && ( ( SceneItemImpl )scene ).getStartTime() != null )
				scene.generateSummary( summary );

		// fill project chapter
		projectChapter.addSection( new ProjectDataSummarySection( this ) );
		projectChapter.addSection( new ProjectExecutionDataSection( this ) );
		projectChapter.addSection( new ProjectExecutionMetricsSection( this ) );
		projectChapter.addSection( new ProjectExecutionNotablesSection( this ) );
		projectChapter.addSection( new ProjectDataSection( this ) );
		projectChapter.setDescription( getDescription() );

		for( ComponentItem component : getComponents() )
			component.generateSummary( projectChapter );
	}

	@Override
	public CanvasObjectItem duplicate( CanvasObjectItem obj )
	{
		if( !( obj instanceof SceneItem ) )
			return super.duplicate( obj );

		if( !( obj instanceof SceneItemImpl ) )
			throw new IllegalArgumentException( obj + " needs to be an instance of: " + SceneItemImpl.class.getName() );

		SceneItemConfig config = getConfig().addNewScene();

		config.set( ( ( SceneItemImpl )obj ).getConfig() );
		config.setLabel( "Copy of " + config.getLabel() );
		Map<String, String> addresses = new HashMap<String, String>();
		addresses.put( config.getId(), addressableRegistry.generateId() );
		for( ComponentItemConfig component : config.getComponentArray() )
			addresses.put( component.getId(), addressableRegistry.generateId() );
		String data = config.xmlText();
		for( Entry<String, String> e : addresses.entrySet() )
			data = data.replaceAll( e.getKey(), e.getValue() );
		try
		{
			config.set( XmlObject.Factory.parse( data ) );
		}
		catch( XmlException e )
		{
			throw new RuntimeException( e );
		}

		SceneItemImpl scene = new SceneItemImpl( this, config );
		scene.init();
		if( attachScene( scene ) )
			fireCollectionEvent( SCENES, CollectionEvent.Event.ADDED, scene );

		return scene;
	}

	@Override
	public boolean isSaveReport()
	{
		return saveReport.getValue();
	}

	@Override
	public void setSaveReport( boolean save )
	{
		saveReport.setValue( save );
	}

	@Override
	public long getNumberOfAutosaves()
	{
		return numberOfAutosaves.getValue();
	}

	@Override
	public void setNumberOfAutosaves( long n )
	{
		numberOfAutosaves.setValue( n );
	}

	@Override
	public String getReportFolder()
	{
		return reportFolder.getValue();
	}

	@Override
	public void setReportFolder( String path )
	{
		reportFolder.setValue( path );
	}

	@Override
	public String getReportFormat()
	{
		return reportFormat.getValue();
	}

	@Override
	public void setReportFormat( String format )
	{
		reportFormat.setValue( format );
	}

	@Override
	public StatisticPages getStatisticPages()
	{
		return statisticPages;
	}

	@Override
	public void setAbortOnFinish( boolean abort )
	{
		// when property changes on project set new value to all test cases
		super.setAbortOnFinish( abort );
		for( SceneItem s : getScenes() )
		{
			s.setAbortOnFinish( abort );
		}
	}

	private static class AssignmentImpl implements Assignment
	{
		private final SceneItem scene;
		private final AgentItem agent;

		public AssignmentImpl( SceneItem scene, AgentItem agent )
		{
			this.scene = scene;
			this.agent = agent;
		}

		@Override
		public AgentItem getAgent()
		{
			return agent;
		}

		@Override
		public SceneItem getScene()
		{
			return scene;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ( ( agent == null ) ? 0 : agent.getId().hashCode() );
			result = prime * result + ( ( scene == null ) ? 0 : scene.getId().hashCode() );
			return result;
		}

		@Override
		public boolean equals( Object obj )
		{
			if( this == obj )
				return true;
			if( obj == null )
				return false;
			if( getClass() != obj.getClass() )
				return false;
			AssignmentImpl other = ( AssignmentImpl )obj;

			return( agent.getId().equals( other.agent.getId() ) && scene.getId().equals( other.scene.getId() ) );
		}
	}

	private class SceneListener implements EventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( event instanceof RemoteActionEvent )
			{
				SceneItem scene = ( SceneItem )event.getSource();
				sendSceneCommand( scene, SceneCommunication.ACTION_EVENT, event.getKey(), scene.getId() );
			}
			else if( event.getSource() instanceof SceneItem )
			{
				SceneItem scene = ( SceneItem )event.getSource();
				if( DELETED.equals( event.getKey() ) )
				{
					for( int i = 0; i < getConfig().sizeOfSceneArray(); i++ )
					{
						if( scene.getId().equals( getConfig().getSceneArray( i ).getId() ) )
						{
							for( AgentItem agent : getAgentsAssignedTo( scene ) )
								unassignScene( scene, agent );
							getConfig().removeScene( i );
							break;
						}
					}
				}
				else if( RELEASED.equals( event.getKey() ) )
					detachScene( scene );
				else if( LABEL.equals( event.getKey() ) )
					sendSceneCommand( scene, SceneCommunication.LABEL, scene.getLabel() );
				if( event instanceof CollectionEvent )
				{
					CollectionEvent cEvent = ( CollectionEvent )event;
					if( CONNECTIONS.equals( cEvent.getKey() ) )
					{
						Connection connection = ( Connection )cEvent.getElement();
						String command = cEvent.getEvent() == CollectionEvent.Event.ADDED ? SceneCommunication.CONNECT
								: SceneCommunication.DISCONNECT;
						sendSceneCommand( scene, command, connection.getOutputTerminal().getId(), connection
								.getInputTerminal().getId() );
					}
					else if( SceneItem.EXPORTS.equals( cEvent.getKey() ) )
					{
						String command = cEvent.getEvent() == CollectionEvent.Event.ADDED ? SceneCommunication.EXPORT
								: SceneCommunication.UNEXPORT;
						sendSceneCommand( scene, command, ( ( Terminal )cEvent.getElement() ).getId() );
					}
					else if( COMPONENTS.equals( cEvent.getKey() ) )
					{
						ComponentItem component = ( ComponentItem )cEvent.getElement();
						if( cEvent.getEvent() == CollectionEvent.Event.ADDED )
						{
							propertySynchronizer.syncProperties( component, sceneEndpoints.get( scene ) );
							component.addEventListener( RemoteActionEvent.class, sceneComponentListener );
							sendSceneCommand( scene, SceneCommunication.ADD_COMPONENT,
									conversionService.convert( component, String.class ) );
						}
						else
						{
							sendSceneCommand( scene, SceneCommunication.REMOVE_COMPONENT, component.getId() );
						}
					}
				}
			}
		}
	}

	private class AgentListener implements EventHandler<BaseEvent>, MessageListener, Releasable
	{
		private final Set<AgentItem> agents = new HashSet<AgentItem>();
		private final AgentContextListener subListener = new AgentContextListener();

		public void attach( AgentItem agent )
		{
			if( agents.add( agent ) )
			{
				agent.addEventListener( BaseEvent.class, this );
				agent.addMessageListener( AgentItem.AGENT_CHANNEL, this );
				agent.addMessageListener( ComponentContext.COMPONENT_CONTEXT_CHANNEL, subListener );
			}
		}

		public void detach( AgentItem agent )
		{
			if( agents.remove( agent ) )
			{
				agent.removeEventListener( BaseEvent.class, this );
				agent.removeMessageListener( this );
				agent.removeMessageListener( subListener );
			}
		}

		@Override
		public void release()
		{
			for( AgentItem agent : agents )
			{
				agent.removeEventListener( BaseEvent.class, this );
				agent.removeMessageListener( this );
				agent.removeMessageListener( subListener );
			}
			agents.clear();
		}

		@Override
		public void handleEvent( BaseEvent event )
		{
			AgentItem agent = ( AgentItem )event.getSource();
			if( AgentItem.READY.equals( event.getKey() ) && agent.isReady() )
			{
				log.debug( "Agent is ready!" );
				for( SceneItem scene : getScenesAssignedTo( agent ) )
				{
					log.debug( "Send message assign: {}", scene.getLabel() );
					agent.sendMessage( AgentItem.AGENT_CHANNEL, Collections.singletonMap( AgentItem.ASSIGN, scene.getId() ) );
				}
			}
		}

		@Override
		@SuppressWarnings( "unchecked" )
		public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
		{
			Map<String, String> message = ( Map<String, String> )data;
			if( message.containsKey( AgentItem.DEFINE_SCENE ) )
			{
				SceneItem scene = ( SceneItem )addressableRegistry.lookup( message.get( AgentItem.DEFINE_SCENE ) );
				if( scene != null )
				{
					log.debug( "Agent {} has requested a TestCase: {}, sending...", endpoint,
							message.get( AgentItem.DEFINE_SCENE ) );
					endpoint.sendMessage(
							channel,
							ImmutableMap.of( AgentItem.SCENE_ID, scene.getId(), AgentItem.SCENE_DEFINITION,
									conversionService.convert( scene, String.class ) ) );
				}
				else
				{
					log.warn( "An Agent {} has requested a nonexistant TestCase: {}", endpoint,
							message.get( AgentItem.DEFINE_SCENE ) );
				}
			}
			else if( message.containsKey( AgentItem.SCENE_ID ) )
			{
				Map<Object, Object> map = ( Map<Object, Object> )data;
				SceneItem scene = ( SceneItem )addressableRegistry.lookup( ( String )map.remove( AgentItem.SCENE_ID ) );
				if( scene instanceof SceneItemImpl )
				{
					synchronized( scene )
					{
						( ( SceneItemImpl )scene ).handleStatisticsData( ( AgentItem )endpoint, map );
					}
				}
			}
			else if( message.containsKey( AgentItem.STARTED ) )
			{
				SceneItem scene = ( SceneItem )addressableRegistry.lookup( message.get( AgentItem.STARTED ) );
				if( scene.isRunning() && !workspace.isLocalMode() )
					endpoint
							.sendMessage( SceneCommunication.CHANNEL, Arrays.asList( scene.getId(),
									Long.toString( scene.getVersion() ), SceneCommunication.ACTION_EVENT, START_ACTION,
									scene.getId() ) );
			}
		}
	}

	private class AgentContextListener implements MessageListener
	{
		@Override
		public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
		{
			Object[] args = ( Object[] )data;
			ComponentItemImpl target = ( ComponentItemImpl )addressableRegistry.lookup( ( String )args[0] );
			if( target != null )
			{
				TerminalMessage message = target.getContext().newMessage();
				message.load( args[1] );
				target.sendAgentMessage( ( AgentItem )endpoint, message );
			}
		}
	}

	private class WorkspaceListener implements EventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( event instanceof CollectionEvent )
			{
				CollectionEvent cEvent = ( CollectionEvent )event;
				if( WorkspaceItem.AGENTS == event.getKey() )
				{
					AgentItem agent = ( AgentItem )cEvent.getElement();
					if( CollectionEvent.Event.ADDED == cEvent.getEvent() )
						agentListener.attach( agent );
					else
						agentListener.detach( agent );
				}
			}
			else if( event instanceof ActionEvent )
			{
				fireEvent( event );
			}
		}
	}

	private class SceneComponentListener implements EventHandler<RemoteActionEvent>
	{
		@Override
		public void handleEvent( RemoteActionEvent event )
		{
			ComponentItem component = ( ComponentItem )event.getSource();
			sendSceneCommand( ( SceneItem )component.getCanvas(), SceneCommunication.ACTION_EVENT, event.getKey(),
					component.getId() );
		}
	}

	@Override
	public boolean isLoadingError()
	{
		for( SceneItem scene : getScenes() )
		{
			if( scene.isLoadingError() )
				return true;
		}
		return super.isLoadingError();
	}

	@Override
	public void cancelScenes( boolean linkedOnly )
	{
		for( SceneItem s : getScenes() )
		{
			if( !linkedOnly || linkedOnly && s.isFollowProject() )
			{
				s.cancelComponents();
			}
		}
	}

	/**
	 * Waits for ON_COMPLETE_DONE event from all scenes and calls
	 * 'doGenerateSummary' method. This event is fired after 'onComplete' method
	 * of test case is executed in local mode, and when controller receives agent
	 * data in distributed mode.
	 * 
	 * @author predrag.vucetic
	 * 
	 */
	private class SceneCompleteAwaiter implements EventHandler<BaseEvent>
	{
		// Counts how many test cases didn't send ON_COMPLETE_DONE event.
		private AtomicInteger a = new AtomicInteger( 0 );

		// timeout scheduler. this is used when all test cases have property
		// abortOnFinish set to true, so since they should return immediately,
		// they will be discarded if they do not return in timeout period. if
		// there is a test case with this property set to false, respond time is
		// not known and there is no timeout.
		private ScheduledFuture<?> awaitingSummaryTimeout;

		public SceneCompleteAwaiter()
		{
			startTimeoutScheduler();
			tryComplete();
		}

		@Override
		public void handleEvent( BaseEvent event )
		{
			if( event.getKey().equals( ON_COMPLETE_DONE ) )
			{
				event.getSource().removeEventListener( BaseEvent.class, this );
				tryComplete();
			}
		}

		private void tryComplete()
		{
			a.set( 0 );
			// increase counter for all non completed linked test cases. if count
			// is zero call doGenerateSummary()
			for( SceneItem scene : getScenes() )
			{
				synchronized( scene )
				{
					if( ( ( SceneItemImpl )scene ).getStartTime() != null && !scene.isCompleted() )
					{
						// add this as a listener to a test case
						scene.addEventListener( BaseEvent.class, this );
						// increment counter
						a.incrementAndGet();
					}
				}
			}
			if( a.get() == 0 )
			{
				if( awaitingSummaryTimeout != null )
					awaitingSummaryTimeout.cancel( true );

				setCompleted( true );
				doGenerateSummary();
			}
		}

		// if abort is true for all test cases, set timer to wait 15 seconds and
		// then on each scene call setCompleted(true) which will throw
		// ON_COMPLETE_DONE event on every test case which will then call the
		// handleEvent method of this class which will call tryComplete() and
		// generate summary when all test cases are finished.
		// TODO add another, longer timeout when there are test cases with
		// abortOnFinish = false?
		private void startTimeoutScheduler()
		{
			// if at least one of the waiting scenes has abort property set to
			// false don't start the timeout scheduler.
			boolean abort = true;
			for( SceneItem scene : getScenes() )
			{
				if( scene.isFollowProject() && !scene.isAbortOnFinish() )
				{
					abort = false;
					break;
				}
			}
			if( abort )
			{
				awaitingSummaryTimeout = scheduler.schedule( new Runnable()
				{
					@Override
					public void run()
					{
						log.error( "Failed to get statistics from all expected Agents within timeout period!" );
						for( SceneItem scene : getScenes() )
						{
							synchronized( scene )
							{
								if( !scene.isCompleted() )
								{
									( ( SceneItemImpl )scene ).setCompleted( true );
								}
							}
						}
					}
				}, 15, TimeUnit.SECONDS );
			}
		}
	}
}