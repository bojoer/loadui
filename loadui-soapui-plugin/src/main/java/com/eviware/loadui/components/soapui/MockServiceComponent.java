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
package com.eviware.loadui.components.soapui;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.categories.MiscCategory;
import com.eviware.loadui.api.counter.Counter;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.layout.LayoutContainer;
import com.eviware.loadui.api.layout.OptionsProvider;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.impl.component.ActivityStrategies;
import com.eviware.loadui.impl.component.categories.OnOffBase;
import com.eviware.loadui.impl.layout.ActionLayoutComponentImpl;
import com.eviware.loadui.impl.layout.LayoutComponentImpl;
import com.eviware.loadui.impl.layout.LayoutContainerImpl;
import com.eviware.loadui.impl.layout.OptionsProviderImpl;
import com.eviware.loadui.impl.layout.PropertyLayoutComponentImpl;
import com.eviware.loadui.impl.layout.SeparatorLayoutComponentImpl;
import com.eviware.loadui.impl.layout.SettingsLayoutContainerImpl;
import com.eviware.loadui.integration.LoadUIIntegrator;
import com.eviware.loadui.integration.SoapUIProjectLoader;
import com.eviware.loadui.util.layout.DelayedFormattedString;
import com.eviware.loadui.util.soapui.CajoClient;
import com.eviware.soapui.SoapUIExtensionClassLoader;
import com.eviware.soapui.SoapUIExtensionClassLoader.SoapUIClassLoaderState;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlProjectPro;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunner;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockRunListener;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.google.common.collect.ImmutableMap;

public class MockServiceComponent extends OnOffBase implements MiscCategory, MockRunListener
{
	@SuppressWarnings( "hiding" )
	private static final Logger log = LoggerFactory.getLogger( MockServiceComponent.class );

	public static final String MOCK_SERVICE = "mockService";
	public static final String PATH = "path";
	public static final String SETTINGS_FILE = "settingsFile";
	public static final String PORT = "port";
	public static final String PROJECT_FILE = "projectFile";
	public static final String PROJECT_PASSWORD = "_projectPassword";
	public static final String ADD_REQUEST = "addRequest";
	public static final String ADD_RESPONSE = "addResponse";
	public static final String PROPERTIES = MockServiceComponent.class.getSimpleName() + "_properties";
	public static final String PROJECT_RELATIVE_PATH = "projectRelativePath";
	public static final String USE_PROJECT_RELATIVE_PATH = "useProjectRelativePath";

	public static final String TYPE = MockServiceComponent.class.getName();

	// Properties
	private final Property<File> projectFile;
	private final Property<String> projectPassword;
	private final Property<File> settingsFile;
	private final Property<String> projectgRelativePath;
	private final Property<Boolean> useProjectRelativePath;
	private final Property<String> mockService;
	private final Property<String> path;
	private final Property<String> port;
	private final Property<Boolean> addRequestProperty;
	private final Property<Boolean> addResponseProperty;

	private final Counter responseCounter;
	private final Counter requestCounter;

	private final OptionsProviderImpl<String> mockServiceOptions = new OptionsProviderImpl<>();

	private boolean reloadingProject;
	private OutputTerminal messageTerminal;
	private ActionLayoutComponentImpl openInSoapUIAction;
	private DelayedFormattedString displayRequests;

	private final SoapUIMockServiceRunner runner = new SoapUIMockServiceRunner();
	private WsdlMockService soapuiMockService;
	private WsdlMockRunner mockRunner;

	private ActionLayoutComponentImpl openInBrowserAction;
	private File loaduiProjectFolder;

	private PropertyLayoutComponentImpl<String> pathField;
	private PropertyLayoutComponentImpl<String> portField;
	private final StateListener stateListener = new StateListener();

	public MockServiceComponent( ComponentContext context )
	{
		super( context );

		responseCounter = context.getCounter( "MockResponses" );
		requestCounter = context.getCounter( "MockRequest Counter" );

		context.setHelpUrl( "http://www.loadui.org/Misc/soapui-mockservice-component.html" );
		messageTerminal = context.createOutput( "messages", "Messages", "Outputs data about each received request." );
		Map<String, Class<?>> resultSignature = new HashMap<>();
		resultSignature.put( "Timestamp", Long.class );
		resultSignature.put( "Request", String.class );
		resultSignature.put( "Request Size", Long.class );
		resultSignature.put( "Response", String.class );
		resultSignature.put( "Response Size", Long.class );
		context.setSignature( messageTerminal, resultSignature );

		projectFile = context.createProperty( PROJECT_FILE, File.class );
		projectPassword = context.createProperty( PROJECT_PASSWORD, String.class );
		settingsFile = context.createProperty( SETTINGS_FILE, File.class );
		projectgRelativePath = context.createProperty( PROJECT_RELATIVE_PATH, String.class );
		useProjectRelativePath = context.createProperty( USE_PROJECT_RELATIVE_PATH, Boolean.class, false );
		mockService = context.createProperty( MOCK_SERVICE, String.class );
		path = context.createProperty( PATH, String.class );
		port = context.createProperty( PORT, String.class );
		addRequestProperty = context.createProperty( ADD_REQUEST, Boolean.class, false );
		addResponseProperty = context.createProperty( ADD_RESPONSE, Boolean.class, false );

		ProjectItem project = context.getCanvas().getProject();
		loaduiProjectFolder = project.getProjectFile().getParentFile();

		if( useProjectRelativePath.getValue() )
		{
			File relativeFile = new File( loaduiProjectFolder, projectgRelativePath.getValue() );
			if( relativeFile.exists() )
				projectFile.setValue( relativeFile );
		}

		runner.setProject( projectFile.getValue() );
		runner.initGeneralSettings();

		context.addEventListener( ActionEvent.class, new EventHandler<ActionEvent>()
		{
			@Override
			public void handleEvent( ActionEvent event )
			{
				if( event.getKey().equals( "RESET" ) )
				{
					projectgRelativePath.setValue( SoapUIComponentActivator.findRelativePath( loaduiProjectFolder,
							projectFile.getValue() ) );
					runner.setProject( projectFile.getValue() );
					runner.setMockService( mockService.getValue() );
				}

				if( event.getKey().equals( "STOP" ) )
				{
					internalStop();
				}

				if( event.getKey().equals( "START" ) )
				{
					internalStart();
				}
			}
		} );

		mockServiceOptions.setNullString( "<Select MockService>" );

		LayoutContainer layout = new LayoutContainerImpl( "wrap 2", "", "align top", "" );
		LayoutContainer leftBox = new LayoutContainerImpl( "", "", "align top", "" );

		leftBox.add( new PropertyLayoutComponentImpl<File>( ImmutableMap.of( //
				PropertyLayoutComponentImpl.PROPERTY, projectFile, //
				PropertyLayoutComponentImpl.LABEL, "soapUI Project", //
				PropertyLayoutComponentImpl.CONSTRAINTS, "w 300!, spanx 2, wrap" ) //
				) );

		leftBox.add( new PropertyLayoutComponentImpl<String>( ImmutableMap.<String, Object> builder()
				.put( PropertyLayoutComponentImpl.PROPERTY, mockService ) //
				.put( PropertyLayoutComponentImpl.LABEL, "soapUI MockService" ) //
				.put( PropertyLayoutComponentImpl.CONSTRAINTS, "w 300!, spanx 2, wrap" ) //
				.put( "style", "-fx-font-size: 17pt" ).put( "widget", "comboBox" ) //
				.put( OptionsProvider.OPTIONS, mockServiceOptions ) //
				.build() ) );

		LayoutContainer connect = new LayoutContainerImpl( "wrap 2, ins 0", "", "align top", "" );
		pathField = new PropertyLayoutComponentImpl<>( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.PROPERTY, path ) //
				.put( PropertyLayoutComponentImpl.LABEL, "path" ) //
				.put( PropertyLayoutComponentImpl.CONSTRAINTS, "w 200!, spanx 1" ) //
				.build() );
		leftBox.add( pathField );

		portField = new PropertyLayoutComponentImpl<>( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.PROPERTY, port ) //
				.put( PropertyLayoutComponentImpl.LABEL, "port" ) //
				.put( PropertyLayoutComponentImpl.CONSTRAINTS, "w 100!, spanx 1" ) //
				.build() );
		connect.add( portField );
		leftBox.add( connect );
		leftBox.add( new SeparatorLayoutComponentImpl( false, "" ) );

		openInSoapUIAction = new ActionLayoutComponentImpl( ImmutableMap.<String, Object> builder() //
				.put( ActionLayoutComponentImpl.LABEL, "Open in soapUI" ) //
				.put( ActionLayoutComponentImpl.ACTION, new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							String[] parameters = new String[] { projectFile.getStringValue() };
							if( !CajoClient.getInstance().testConnection() )
							{
								String soapUIPath = LoadUIIntegrator.getInstance().getWorkspaceProvider().getWorkspace()
										.getProperty( WorkspaceItem.SOAPUI_PATH_PROPERTY ).getStringValue();
								if( soapUIPath == null || soapUIPath.trim().equals( "" ) )
								{
									UISupport.showInfoMessage( "You have not specified soapui.bat(sh) in workspace settings!" );
									return;
								}
								CajoClient.getInstance().startSoapUI();
								try
								{
									Thread.sleep( 2000 );
								}
								catch( InterruptedException e )
								{
									// Ignore
								}
							}
							CajoClient.getInstance().invoke( "openProject", parameters );
						}
						catch( Exception e )
						{
							e.printStackTrace();
						}
					}
				} ).build() );

		openInBrowserAction = new ActionLayoutComponentImpl( ImmutableMap.<String, Object> builder() //
				.put( ActionLayoutComponentImpl.LABEL, "Open in Browser" ) //
				.put( ActionLayoutComponentImpl.ACTION, new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							if( mockRunner != null )
							{
								Tools.openURL( mockRunner.getMockService().getLocalMockServiceEndpoint() );
							}
						}
						catch( Exception e )
						{
							e.printStackTrace();
						}
					}
				} ).build() );

		openInBrowserAction.setEnabled( false );

		connect = new LayoutContainerImpl( "wrap 2, ins 0", "", "align top", "" );
		connect.add( openInSoapUIAction );
		connect.add( openInBrowserAction );
		leftBox.add( connect );

		layout.add( leftBox );

		LayoutContainerImpl box = new LayoutContainerImpl( ImmutableMap.<String, Object> builder()
				.put( LayoutContainerImpl.LAYOUT_CONSTRAINTS, "wrap 3, align right" ) //
				.put( "widget", "display" ) //
				.build() );

		displayRequests = new DelayedFormattedString( "%d", 0 )
		{
			@Override
			public void update()
			{
				// setValue( String.valueOf( responseCounter.get() ) );
				setValue( String.valueOf( requestCounter.get() ) );
			}
		};

		box.add( new LayoutComponentImpl( ImmutableMap.<String, Object> builder()
				.put( PropertyLayoutComponentImpl.LABEL, "Requests" )
				.put( PropertyLayoutComponentImpl.CONSTRAINTS, "w 50!" ).put( "fString", displayRequests ).build() ) ); //
		layout.add( box );

		LayoutContainer compactLayout = new LayoutContainerImpl( Collections.<String, Object> emptyMap() );
		compactLayout.add( box );
		context.setCompactLayout( compactLayout );

		SettingsLayoutContainerImpl settingsLayoutTab = new SettingsLayoutContainerImpl( "General", "", "", "align top",
				"" );

		settingsLayoutTab.add( new PropertyLayoutComponentImpl<String>( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.PROPERTY, addRequestProperty ) //
				.put( PropertyLayoutComponentImpl.LABEL, "Add request to outgoing message" ) //
				.build() ) );

		settingsLayoutTab.add( new PropertyLayoutComponentImpl<String>( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.PROPERTY, addResponseProperty ) //
				.put( PropertyLayoutComponentImpl.LABEL, "Add response to outgoing message" ) //
				.build() ) );

		settingsLayoutTab.add( new PropertyLayoutComponentImpl<File>( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.PROPERTY, settingsFile ) //
				.put( PropertyLayoutComponentImpl.LABEL, "soapUI settings" ) //
				.put( PropertyLayoutComponentImpl.CONSTRAINTS, "w 200!, spanx 2" ) //
				.build() ) );

		settingsLayoutTab.add( new PropertyLayoutComponentImpl<String>( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.PROPERTY, useProjectRelativePath ) //
				.put( PropertyLayoutComponentImpl.LABEL, "Use relative path for project" ) //
				.build() ) );
		settingsLayoutTab.add( new PropertyLayoutComponentImpl<File>( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.PROPERTY, projectPassword ) //
				.put( PropertyLayoutComponentImpl.LABEL, "project password" ) //
				.put( PropertyLayoutComponentImpl.CONSTRAINTS, "w 200!, spanx 2" ) //
				.put( "widget", "password" ) //
				.build() ) );

		context.addSettingsTab( settingsLayoutTab );
		context.addSettingsTab( settingsLayoutTab );
		context.setLayout( layout );

		context.addEventListener( PropertyEvent.class, stateListener );

		if( getContext().isRunning() )
			internalStart();
	}

	@Override
	public String getColor()
	{
		return COLOR;
	}

	@Override
	public String getCategory()
	{
		return CATEGORY;
	}

	protected void restart()
	{
		if( mockRunner != null )
			internalStop();

		internalStart();
	}

	@Override
	public void onRelease()
	{
		internalStop();
		super.onRelease();
		displayRequests.release();
		runner.release();
	}

	@Override
	public MockResult onMockRequest( MockRunner runner, javax.servlet.http.HttpServletRequest request,
			javax.servlet.http.HttpServletResponse response )
	{
		requestCounter.increment();
		return null;
	}

	@Override
	public void onMockResult( MockResult result )
	{
		responseCounter.increment();
		TerminalMessage message = getContext().newMessage();
		message.put( "Timestamp", result.getTimestamp() );
		if( addRequestProperty.getValue() )
			message.put( "Request", result.getMockRequest().getRequestContent() );
		message.put( "Request Size", result.getMockRequest().getRequestContent() == null ? 0 : result.getMockRequest()
				.getRequestContent().length() );
		if( addResponseProperty.getValue() )
			message.put( "Response", result.getResponseContent() );
		message.put( "Response Size", result.getResponseContent() == null ? 0 : result.getResponseContent().length() );
		getContext().send( messageTerminal, message );
	}

	@Override
	public void onMockRunnerStart( MockRunner mockRunner )
	{
	}

	@Override
	public void onMockRunnerStop( MockRunner mockRunner )
	{
	}

	private void fixActivityStrategy()
	{
		if( getStateProperty().getValue() && soapuiMockService != null )
		{
			getContext().setActivityStrategy(
					getContext().isRunning() ? ActivityStrategies.BLINKING : ActivityStrategies.ON );
		}
		else
		{
			getContext().setActivityStrategy( ActivityStrategies.OFF );
		}
	}

	private void internalStop()
	{
		if( mockRunner != null )
		{
			mockRunner.stop();
			mockRunner = null;
			openInBrowserAction.setEnabled( false );
			fixActivityStrategy();
		}
	}

	private void internalStart()
	{
		try
		{
			if( getStateProperty().getValue() && soapuiMockService != null )
			{
				if( mockRunner != null )
					mockRunner.stop();

				mockRunner = soapuiMockService.start();
				openInBrowserAction.setEnabled( true );
				fixActivityStrategy();
			}
		}
		catch( Exception e )
		{
			log.error( "Error starting MockService.", e );
		}
	}

	private class StateListener implements WeakEventHandler<PropertyEvent>
	{
		@Override
		public void handleEvent( PropertyEvent event )
		{
			if( event.getEvent() == PropertyEvent.Event.VALUE )
			{
				try
				{
					if( event.getProperty() == getStateProperty() )
					{
						if( getStateProperty().getValue() )
							internalStart();
						else
							internalStop();
					}
					else if( event.getProperty() == projectFile && !reloadingProject )
					{
						projectgRelativePath.setValue( SoapUIComponentActivator.findRelativePath( loaduiProjectFolder,
								projectFile.getValue() ) );
						runner.setProject( projectFile.getValue() );
					}
					else if( event.getProperty() == mockService )
					{
						runner.setMockService( mockService.getValue() );
						if( getContext().isRunning() )
							restart();
					}
					else if( event.getProperty() == settingsFile )
					{
						File current = SoapUIComponentActivator.loadSettings( settingsFile.getValue() );
						if( !current.equals( settingsFile.getValue() ) && current.exists() )
						{
							settingsFile.setValue( current );
						}
						runner.initGeneralSettings();
						if( getContext().isRunning() )
							restart();
					}
					else if( soapuiMockService != null && event.getProperty() == path )
					{
						soapuiMockService.setPath( path.getValue() );
						if( getContext().isRunning() )
							restart();
					}
					else if( soapuiMockService != null && event.getProperty() == port )
					{
						soapuiMockService.setPort( Integer.parseInt( port.getValue() ) );
						if( getContext().isRunning() )
							restart();
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		}
	}

	private class SoapUIMockServiceRunner implements SoapUIProjectLoader.ProjectUpdateListener
	{
		private WsdlProject project;

		public SoapUIMockServiceRunner()
		{
			SoapUIProjectLoader.getInstance().addProjectUpdateListener( this );
		}

		public void initGeneralSettings()
		{
			File current = SoapUIComponentActivator.loadSettings( settingsFile.getValue() );
			if( current.exists() )
				settingsFile.setValue( current );
		}

		public void release()
		{
			SoapUIProjectLoader loader = SoapUIProjectLoader.getInstance();
			loader.removeProjectUpdateListener( this );

			if( project != null )
				loader.releaseProject( project );

			if( soapuiMockService != null )
			{
				soapuiMockService.removeMockRunListener( MockServiceComponent.this );
			}

			if( mockRunner != null )
			{
				mockRunner.stop();
			}
		}

		public void setProject( File projectFile )
		{
			if( projectFile == null )
				return;

			log.debug( "Setting soapUI project to {}", projectFile );

			reloadProject( projectFile );
		}

		private void reloadProject( File projectFile )
		{
			SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();
			try
			{
				if( !SoapUIProjectLoader.getInstance().isProjectLoaded( projectFile.getAbsolutePath() ) )
				{
					project = SoapUIProjectLoader.getInstance().getProject( projectFile.getAbsolutePath() );
					if( project != null )
					{
						projectPassword.setValue( project.getShadowPassword() );
					}
				}
				else
				{
					project = SoapUIProjectLoader.getInstance().getProject( projectFile.getAbsolutePath(),
							projectPassword.getValue() );
					if( project != null )
					{
						projectPassword.setValue( project.getShadowPassword() );
					}
				}
				if( project != null )
				{
					compositeProjectDecompose();
					initProject();
				}
				else
				{
					unsetProject();
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
			finally
			{
				state.restore();
			}
		}

		private void unsetProject()
		{
			projectFile.setValue( null );
			mockService.setValue( null );
			setMockService( null );
			port.setValue( null );
			path.setValue( null );
		}

		private void compositeProjectDecompose() throws IOException
		{
			if( project instanceof WsdlProjectPro )
			{
				Boolean composite = ( ( WsdlProjectPro )project ).isComposite();
				if( composite )
				{
					( ( WsdlProjectPro )project ).setComposite( false );
					project.save();
					projectFile.setValue( new File( project.getPath() ) );
					projectgRelativePath.setValue( SoapUIComponentActivator.findRelativePath( loaduiProjectFolder,
							projectFile.getValue() ) );
				}
			}
		}

		@Override
		public void onProjectRelease( WsdlProject project )
		{
		}

		@Override
		public void projectUpdated( String file, WsdlProject oldProject, WsdlProject newProject )
		{
			// our project?
			if( oldProject == project )
			{
				reloadingProject = true;

				SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();
				try
				{
					SoapUIProjectLoader.getInstance().releaseProject( oldProject );

					project = newProject;

					if( newProject != null )
					{
						initProject();
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
				finally
				{
					state.restore();
				}

				if( newProject != null && !newProject.isWrongPasswordSupplied() )
				{
					projectFile.setValue( new File( file ) );
					projectgRelativePath.setValue( SoapUIComponentActivator.findRelativePath( loaduiProjectFolder,
							projectFile.getValue() ) );
					projectPassword.setValue( newProject.getShadowPassword() );
				}
				else
				{
					unsetProject();
				}
				reloadingProject = false;
			}
		}

		private void initProject()
		{
			String[] mockServices = ModelSupport.getNames( project.getMockServiceList() );
			mockServiceOptions.setOptions( mockServices );
		}

		public synchronized void setMockService( String mockServiceName )
		{
			if( mockServiceName == null )
			{
				// mockServiceOptions.setNullString( "<Select MockService>" );
				mockServiceOptions.setOptions( new String[0] );
				return;
			}

			log.debug( "Setting soapUI mockService to {}", mockServiceName );

			SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();
			try
			{
				if( soapuiMockService != null )
					soapuiMockService.removeMockRunListener( MockServiceComponent.this );

				if( mockRunner != null )
				{
					mockRunner.stop();
					mockRunner = null;
				}

				soapuiMockService = project.getMockServiceByName( mockServiceName );
				if( soapuiMockService == null )
					return;
				soapuiMockService.addMockRunListener( MockServiceComponent.this );
				port.setValue( soapuiMockService.getPort() );
				path.setValue( soapuiMockService.getPath() );

				if( getContext().isRunning() && getStateProperty().getValue() )
				{
					internalStart();
				}
				else
				{
					openInBrowserAction.setEnabled( false );
				}
				fixActivityStrategy();
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
			finally
			{
				state.restore();
			}
		}
	}
}
