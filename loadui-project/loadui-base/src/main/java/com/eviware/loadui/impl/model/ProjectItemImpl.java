/*
 * Copyright 2010 eviware software ab
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.SwingWorker;
import javax.swing.table.TableModel;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.RemoteActionEvent;
import com.eviware.loadui.api.events.CollectionEvent.Event;
import com.eviware.loadui.api.messaging.BroadcastMessageEndpoint;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.messaging.SceneCommunication;
import com.eviware.loadui.api.model.Assignment;
import com.eviware.loadui.api.model.CanvasObjectItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.RunnerItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.property.PropertySynchronizer;
import com.eviware.loadui.api.summary.Chapter;
import com.eviware.loadui.api.summary.MutableSummary;
import com.eviware.loadui.api.summary.Section;
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
import com.eviware.loadui.api.property.Property;

public class ProjectItemImpl extends CanvasItemImpl<ProjectItemConfig> implements ProjectItem
{
	public static final Logger log = LoggerFactory.getLogger( ProjectItemImpl.class );

	private final WorkspaceItem workspace;
	private final LoaduiProjectDocumentConfig doc;
	private final Set<Assignment> assignments = new HashSet<Assignment>();
	private final RunnerListener runnerListener = new RunnerListener();
	private final SceneListener sceneListener = new SceneListener();
	private final WorkspaceListener workspaceListener = new WorkspaceListener();
	private final SceneComponentListener sceneComponentListener = new SceneComponentListener();
	private final Map<SceneItem, BroadcastMessageEndpoint> sceneEndpoints = new HashMap<SceneItem, BroadcastMessageEndpoint>();
	private final ConversionService conversionService;
	private final PropertySynchronizer propertySynchronizer;
	private final CounterSynchronizer counterSynchronizer;
	private final TerminalProxy proxy;
	private final Set<SceneItem> scenes = new HashSet<SceneItem>();
	private final Set<SceneItem> awaitingScenes = new HashSet<SceneItem>();
	private final Property<Boolean>  saveReport;
	private final Property<String> reportFolder;
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
		proxy = BeanInjector.getBean( TerminalProxy.class );

		// addEventListener( ActionEvent.class, new SelfListener() );
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

		for( RunnerItem runner : workspace.getRunners() )
			runnerListener.attach( runner );

		for( SceneAssignmentConfig conf : getConfig().getSceneAssignmentArray() )
		{
			SceneItem scene = ( SceneItem )addressableRegistry.lookup( conf.getSceneRef() );
			RunnerItem runner = ( RunnerItem )addressableRegistry.lookup( conf.getRunnerRef() );
			if( runner != null )
			{
				sceneEndpoints.get( scene ).registerEndpoint( runner );
				AssignmentImpl assignment = new AssignmentImpl( scene, runner );
				if( assignments.add( assignment ) && runner.isReady() )
				{
					runner.sendMessage( RunnerItem.RUNNER_CHANNEL, Collections.singletonMap( RunnerItem.ASSIGN, scene
							.getId() ) );
				}
			}
		}
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
			for( RunnerItem runner : getRunnersAssignedTo( scene ) )
			{
				log.debug( "Telling {} to stop scene {}", runner, scene );
				runner.sendMessage( RunnerItem.RUNNER_CHANNEL, Collections
						.singletonMap( RunnerItem.UNASSIGN, scene.getId() ) );
			}

			scene.removeEventListener( BaseEvent.class, sceneListener );
			sceneEndpoints.remove( scene );
			awaitingScenes.remove( scene );
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

	@Override
	public void release()
	{
		getWorkspace().removeEventListener( BaseEvent.class, workspaceListener );
		runnerListener.release();

		for( SceneItem scene : new ArrayList<SceneItem>( getScenes() ) )
			scene.release();

		super.release();
	}

	@Override
	public void delete()
	{
		release();
		projectFile.delete();
		super.delete();
	}

	@Override
	public void assignScene( SceneItem scene, RunnerItem runner )
	{
		AssignmentImpl assignment = new AssignmentImpl( scene, runner );
		if( assignments.add( assignment ) )
		{
			sceneEndpoints.get( scene ).registerEndpoint( runner );
			SceneAssignmentConfig conf = getConfig().addNewSceneAssignment();
			String sceneId = scene.getId();
			conf.setSceneRef( sceneId );
			conf.setRunnerRef( runner.getId() );
			runner.sendMessage( RunnerItem.RUNNER_CHANNEL, Collections.singletonMap( RunnerItem.ASSIGN, sceneId ) );
			if( scene.isRunning() )
				runner.sendMessage( SceneCommunication.CHANNEL, Arrays.asList( sceneId,
						Long.toString( scene.getVersion() ), SceneCommunication.ACTION_EVENT, START_ACTION, sceneId ) );
			fireCollectionEvent( ASSIGNMENTS, Event.ADDED, assignment );
		}
	}

	@Override
	public Collection<RunnerItem> getRunnersAssignedTo( SceneItem scene )
	{
		Set<RunnerItem> runners = new HashSet<RunnerItem>();
		for( Assignment assignment : assignments )
			if( scene.equals( assignment.getScene() ) )
				runners.add( assignment.getRunner() );
		return runners;
	}

	@Override
	public Collection<SceneItem> getScenesAssignedTo( RunnerItem runner )
	{
		Set<SceneItem> scenes = new HashSet<SceneItem>();
		for( Assignment assignment : assignments )
			if( runner.equals( assignment.getRunner() ) )
				scenes.add( assignment.getScene() );
		return scenes;
	}

	@Override
	public Collection<Assignment> getAssignments()
	{
		return Collections.unmodifiableCollection( assignments );
	}

	@Override
	public void unassignScene( SceneItem scene, RunnerItem runner )
	{
		AssignmentImpl assignment = new AssignmentImpl( scene, runner );
		if( assignments.remove( assignment ) )
		{
			BroadcastMessageEndpoint bme = sceneEndpoints.get( scene );
			if( bme != null )
			{
				bme.deregisterEndpoint( runner );
				runner.sendMessage( RunnerItem.RUNNER_CHANNEL, Collections
						.singletonMap( RunnerItem.UNASSIGN, scene.getId() ) );
			}

			int size = getConfig().sizeOfSceneAssignmentArray();
			for( int i = 0; i < size; i++ )
			{
				SceneAssignmentConfig conf = getConfig().getSceneAssignmentArray()[i];
				if( scene.getId().equals( conf.getSceneRef() ) && runner.getId().equals( conf.getRunnerRef() ) )
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
		sceneEndpoints.get( scene ).sendMessage( channel, data );
	}

	@Override
	protected void onComplete( EventFirer source )
	{
		for( SceneItem scene : getScenes() )
			if( getRunnersAssignedTo( scene ).size() > 0 && scene.isFollowProject() && !getWorkspace().isLocalMode() )
				awaitingScenes.add( scene );
		if( awaitingScenes.isEmpty() )
			doGenerateSummary();
	}

	@Override
	protected void reset()
	{
		super.reset();
		awaitingScenes.clear();
	}

	@Override
	public void generateSummary( MutableSummary summary )
	{
		// add a project chapter first
		MutableChapterImpl projectChapter = ( MutableChapterImpl )summary.addChapter( getLabel() );
		// add and generate test case chapters
		for( SceneItem it : scenes )
		{
			it.generateSummary( summary );
		}
		// fill project chapter
		projectChapter.addSection( new ProjectDataSummarySection( this ) );
		projectChapter.addSection( new ProjectExecutionDataSection( this ) );
		projectChapter.addSection( new ProjectExecutionMetricsSection( this ) );
		projectChapter.addSection( new ProjectExecutionNotablesSection( this ) );
		projectChapter.addSection( new ProjectDataSection( this ) );
		projectChapter.setDescription( getDescription() );

		// We decided to wait a bit with this...
		if (saveReport.getValue())
			saveSummary( summary );
	}

	/**
	 * saving summary, in background, in xml format, quick and dirty.
	 * 
	 * @param summary
	 */
	private void saveSummary( final MutableSummary summary )
	{

		SwingWorker worker = new SwingWorker()
		{

			@Override
			protected Object doInBackground() throws Exception
			{

				try
				{
					XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
					// Create an XML stream writer
					File outputDir;
					if (reportFolder == null || reportFolder.getValue().length() < 1)
						outputDir = new File( System.getProperty( "loadui.home" ) );
					else 
						outputDir = new File( reportFolder.getValue() );

					String fileName = getLabel() + "-summary-" + System.currentTimeMillis() + ".xml";
					File out = new File( outputDir, fileName );
					XMLStreamWriter xmlw = xmlof.createXMLStreamWriter( new BufferedWriter( new FileWriter( out ) ) );
					xmlw.writeStartDocument();
					xmlw.writeCharacters( "\n" );
					xmlw.writeStartElement( "summary" );
					xmlw.writeCharacters( "\n" );
					for( String chapKey : summary.getChapters().keySet() )
					{
						Chapter chapter = summary.getChapters().get( chapKey );
						xmlw.writeCharacters( "\t" );
						xmlw.writeStartElement( "chapter" );
						xmlw.writeAttribute( "title", chapter.getTitle() );
						xmlw.writeAttribute( "date", chapter.getDate().toString() );
						xmlw.writeCharacters( "\n" );
						xmlw.writeCharacters( "\t\t" );
						xmlw.writeStartElement( "description" );
						xmlw.writeCharacters( chapter.getDescription() );
						xmlw.writeEndElement();
						xmlw.writeCharacters( "\n" );
						for( String valKey : chapter.getValues().keySet() )
						{
							xmlw.writeCharacters( "\t\t" );
							xmlw.writeStartElement( valKey.replace( " ", "_" ).replace( "(%)", "" ).toLowerCase() );
							xmlw.writeCharacters( chapter.getValues().get( valKey ) );
							xmlw.writeCharacters( "\t\t" );
							xmlw.writeEndElement();
							xmlw.writeCharacters( "\n" );
							/*
							 * xmlw.writeCharacters( "\n" ); xmlw.writeStartElement(
							 * "value-value" ); xmlw.writeCharacters(
							 * chapter.getValues().get( valKey ) );
							 * xmlw.writeEndElement(); // value-value
							 * xmlw.writeCharacters( "\n" ); xmlw.writeEndElement(); //
							 * value
							 */
						}
						for( Section section : chapter.getSections() )
						{
							xmlw.writeCharacters( "\t\t" );
							xmlw.writeStartElement( "section" );
							xmlw.writeAttribute( "title", section.getTitle() );
							xmlw.writeCharacters( "\n" );
							for( String valKey : section.getValues().keySet() )
							{
								xmlw.writeCharacters( "\t\t\t" );
								xmlw.writeStartElement( valKey.replace( " ", "_" ).replace( "(%)", "" ).toLowerCase() );
								xmlw.writeCharacters( section.getValues().get( valKey ) );
								xmlw.writeEndElement(); // value-name
								xmlw.writeCharacters( "\n" );
							}
							for( String tablekey : section.getTables().keySet() )
							{
								xmlw.writeCharacters( "\t\t\t" );
								xmlw.writeStartElement( tablekey.replace( " ", "_" ).toLowerCase() );
								// xmlw.writeAttribute( "name", tablekey );
								TableModel table = section.getTables().get( tablekey );
								xmlw.writeCharacters( "\n" );
								// xmlw.writeStartElement( "columns" );
								// xmlw.writeAttribute( "size", String.valueOf(
								// table.getColumnCount() ) );
								// xmlw.writeCharacters( "\n" );
								// StringBuffer columns = new StringBuffer();
								// String columns = new String [table.getColumnCount()]
								// for( int i = 0; i < table.getColumnCount(); i++ ) {
								// columns.append( table.getColumnName( i ) ).append(
								// "," );
								// columns.deleteCharAt( columns.length() - 1 );
								// xmlw.writeCharacters( columns.toString() );
								// xmlw.writeEndElement();// columns
								// xmlw.writeCharacters( "\n" );
								//
								// xmlw.writeStartElement( "rows" );
								// xmlw.writeAttribute( "size", String.valueOf(
								// table.getRowCount() ) );
								// xmlw.writeCharacters( "\n" );
								for( int j = 0; j < table.getRowCount(); j++ )
								{
									xmlw.writeCharacters( "\t\t\t" );
									xmlw.writeStartElement( "row" );
									xmlw.writeCharacters( "\n" );
									StringBuffer row = new StringBuffer();
									for( int i = 0; i < table.getColumnCount(); i++ )
									{
										xmlw.writeCharacters( "\t\t\t\t" );
										xmlw.writeStartElement( table.getColumnName( i ).replace( " ", "_" ).replace( "/", "_" )
												.toLowerCase() );
										if( table.getValueAt( j, i ) != null )
											xmlw.writeCharacters( table.getValueAt( j, i ).toString() );
										xmlw.writeEndElement();
										xmlw.writeCharacters( "\n" );
									}
									xmlw.writeCharacters( "\t\t\t" );
									xmlw.writeEndElement();// row
									xmlw.writeCharacters( "\n" );
								}
								xmlw.writeCharacters( "\t\t\t" );
								xmlw.writeEndElement(); // table
								xmlw.writeCharacters( "\n" );
							}
							xmlw.writeCharacters( "\t\t" );
							xmlw.writeEndElement();// section
							xmlw.writeCharacters( "\n" );
						}
						xmlw.writeCharacters( "\t" );
						xmlw.writeEndElement(); // chapter
						xmlw.writeCharacters( "\n" );
					}
					xmlw.writeEndElement(); //
					xmlw.writeCharacters( "\n" );
					xmlw.writeEndDocument();
					xmlw.flush();
					xmlw.close();
				}
				catch( XMLStreamException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch( FileNotFoundException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch( IOException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return null;
			}

		};
		worker.execute();
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

	private class AssignmentImpl implements Assignment
	{
		private final SceneItem scene;
		private final RunnerItem runner;

		public AssignmentImpl( SceneItem scene, RunnerItem runner )
		{
			this.scene = scene;
			this.runner = runner;
		}

		@Override
		public RunnerItem getRunner()
		{
			return runner;
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
			result = prime * result + ( ( runner == null ) ? 0 : runner.getId().hashCode() );
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

			return( runner.getId().equals( other.runner.getId() ) && scene.getId().equals( other.scene.getId() ) );
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
							for( RunnerItem runner : getRunnersAssignedTo( scene ) )
								unassignScene( scene, runner );
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
							sendSceneCommand( scene, SceneCommunication.ADD_COMPONENT, conversionService.convert( component,
									String.class ) );
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

	private class RunnerListener implements EventHandler<BaseEvent>, MessageListener
	{
		private final Set<RunnerItem> runners = new HashSet<RunnerItem>();
		private final RunnerContextListener subListener = new RunnerContextListener();

		public void attach( RunnerItem runner )
		{
			if( runners.add( runner ) )
			{
				runner.addEventListener( BaseEvent.class, this );
				runner.addMessageListener( RunnerItem.RUNNER_CHANNEL, this );
				runner.addMessageListener( ComponentContext.COMPONENT_CONTEXT_CHANNEL, subListener );
			}
		}

		public void detach( RunnerItem runner )
		{
			if( runners.remove( runner ) )
			{
				runner.removeEventListener( BaseEvent.class, this );
				runner.removeMessageListener( this );
				runner.removeMessageListener( subListener );
			}
		}

		public void release()
		{
			for( RunnerItem runner : runners )
			{
				runner.removeEventListener( BaseEvent.class, this );
				runner.removeMessageListener( this );
				runner.removeMessageListener( subListener );
			}
			runners.clear();
		}

		@Override
		public void handleEvent( BaseEvent event )
		{
			RunnerItem runner = ( RunnerItem )event.getSource();
			if( RunnerItem.READY.equals( event.getKey() ) && runner.isReady() )
			{
				log.debug( "Runner is ready!" );
				for( SceneItem scene : getScenesAssignedTo( runner ) )
				{
					log.debug( "Send message assign: {}", scene.getLabel() );
					runner.sendMessage( RunnerItem.RUNNER_CHANNEL, Collections.singletonMap( RunnerItem.ASSIGN, scene
							.getId() ) );
				}
			}
		}

		@Override
		@SuppressWarnings( "unchecked" )
		public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
		{
			Map<String, String> message = ( Map<String, String> )data;
			if( message.containsKey( RunnerItem.DEFINE_SCENE ) )
			{
				SceneItem scene = ( SceneItem )addressableRegistry.lookup( message.get( RunnerItem.DEFINE_SCENE ) );
				if( scene != null )
				{
					endpoint.sendMessage( channel, MapUtils.build( String.class, String.class ).put( RunnerItem.SCENE_ID,
							scene.getId() )
							.put( RunnerItem.SCENE_DEFINITION, conversionService.convert( scene, String.class ) )
							.getImmutable() );
				}
				else
				{
					log.info( "An Agent {} has requested a nonexistant TestCase: {}", endpoint, message
							.get( RunnerItem.DEFINE_SCENE ) );
				}
			}
			else if( message.containsKey( RunnerItem.SCENE_ID ) )
			{
				Map<Object, Object> map = ( Map<Object, Object> )data;
				SceneItem scene = ( SceneItem )addressableRegistry.lookup( ( String )map.remove( RunnerItem.SCENE_ID ) );
				if( scene instanceof SceneItemImpl )
				{
					( ( SceneItemImpl )scene ).handleStatisticsData( ( RunnerItem )endpoint, map );
					if( awaitingScenes.remove( scene ) && awaitingScenes.isEmpty() )
						doGenerateSummary();
				}
			}
		}
	}

	private class RunnerContextListener implements MessageListener
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
				target.sendRunnerMessage( ( RunnerItem )endpoint, message );
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
				if( WorkspaceItem.RUNNERS.equals( event.getKey() ) )
				{
					RunnerItem runner = ( RunnerItem )cEvent.getElement();
					if( CollectionEvent.Event.ADDED.equals( cEvent.getEvent() ) )
						runnerListener.attach( runner );
					else
						runnerListener.detach( runner );
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
	public boolean isSaveReport() {
		return saveReport.getValue();
	}
	
	@Override
	public void setSaveReport(boolean save) {
		saveReport.setValue(save);
	}
	
	@Override
	public String getReportFolder() {
		return reportFolder.getValue();
	}
	
	@Override
	public void setReportFolder(String path) {
		reportFolder.setValue(path);
	}

	// private class SelfListener implements EventHandler<ActionEvent>
	// {
	// @Override
	// public void handleEvent( ActionEvent event )
	// {
	// for( SceneItem scene : scenes )
	// if( scene.isFollowProject()
	// || !( START_ACTION.equals( event.getKey() ) || STOP_ACTION.equals(
	// event.getKey() ) ) )
	// sendSceneCommand( scene, SceneCommunication.ACTION_EVENT, event.getKey(),
	// scene.getId() );
	// }
	// }
}
