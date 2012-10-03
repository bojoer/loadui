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
package com.eviware.loadui.integration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.component.categories.AnalysisCategory;
import com.eviware.loadui.api.component.categories.GeneratorCategory;
import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.ui.ApplicationState;
import com.eviware.loadui.api.ui.WindowController;
import com.eviware.loadui.components.soapui.MockServiceComponent;
import com.eviware.loadui.components.soapui.SoapUISamplerComponent;
import com.eviware.loadui.components.soapui.layout.SoapUiProjectSelector;
import com.eviware.loadui.util.soapui.CajoClient;

public class LoadUIIntegrator
{
	private static volatile LoadUIIntegrator instance;

	public static LoadUIIntegrator getInstance()
	{
		if( instance == null )
		{
			return instance = new LoadUIIntegrator();
		}
		return instance;
	}

	private static final String OUTPUT = "output";
	private static final String SAMPLE_COUNT = "Sample Count";
	private static final String FIXED_LOAD = "Fixed Load";
	private static final String SOAPUI_RUNNER_LABEL = "soapuiRunnerLabel";
	private static final String LOADUI_TEST_CASE_NAME = "loaduiTestCaseName";
	private static final String LOADUI_PROJECT_NAME = "loaduiProjectName";
	private static final String MOCKSERVICE_RUNNER_LABEL = "mockRunnerLabel";
	public static final String SOAPUI_RUNNER_BASE_NAME = "soapUI Runner";
	public static final String MOCK_RUNNER_BASE_NAME = "soapUI MockService";

	private static final String TRIGGER_LABEL = "triggerLabel";
	private static final String TRIGGER_TYPE = "triggerType";
	private static final String TRIGGER_PROPERTIES = "triggerProperties";
	private static final String TRIGGER_CREATE_NEW = "triggerCreateNew";

	private static final String ASSERTION_LABEL = "assertionLabel";
	private static final String ASSERTION_TYPE = "assertionType";
	private static final String ASSERTION_PROPERTIES = "assertionProperties";
	private static final String ASSERTION_CREATE_NEW = "assertionCreateNew";

	private static final String STATISTICS_LABEL = "statisticsLabel";
	private static final String STATISTICS_TYPE = "statisticsType";
	private static final String STATISTICS_PROPERTIES = "statisticsProperties";
	private static final String STATISTICS_CREATE_NEW = "statisticsCreateNew";

	private WindowController windowController;
	private WorkspaceProvider workspaceProvider;
	private ComponentDescriptor soapuiRunnerDescriptor;
	private ComponentDescriptor mockServiceDescriptor;
	private ComponentRegistry componentRegistry;
	private ApplicationState applicationState;

	public static final Logger log = LoggerFactory.getLogger( LoadUIIntegrator.class );

	public void test()
	{
		CajoClient.getInstance().setSoapUIPath();
	}

	public String getLoadUIPath()
	{
		String os = System.getProperty( "os.name" );
		if( os == null )
			return null;

		String ext;
		if( os.indexOf( "Windows" ) >= 0 )
			ext = "bat";
		else if( os.indexOf( "Mac OS X" ) >= 0 )
			ext = "command";
		else
			ext = "sh";

		String path = LoadUI.getWorkingDir().getAbsolutePath();
		path += File.separator + "loadUI." + ext;

		File f = new File( path );
		if( f.exists() )
			return f.getAbsolutePath();
		else
			return null;
	}

	public void bringToFront()
	{
		windowController.bringToFront();
	}

	public HashMap<String, String> createSoapUIRunner( HashMap<String, Object> context ) throws IOException
	{
		ComponentContext compContext = createRunner( context, SOAPUI_RUNNER_BASE_NAME );
		createAndConnectComponents( context, compContext );

		HashMap<String, String> soapuiRunnerProperties = new HashMap<>();
		soapuiRunnerProperties.put( SOAPUI_RUNNER_LABEL, compContext.getLabel() );
		CanvasItem canvas = compContext.getCanvas();
		if( canvas instanceof ProjectItem )
		{
			soapuiRunnerProperties.put( LOADUI_PROJECT_NAME, canvas.getLabel() );
			soapuiRunnerProperties.put( LOADUI_TEST_CASE_NAME, null );
		}
		else if( canvas instanceof SceneItem )
		{
			soapuiRunnerProperties.put( LOADUI_TEST_CASE_NAME, canvas.getLabel() );
			soapuiRunnerProperties.put( LOADUI_PROJECT_NAME, ( ( SceneItem )canvas ).getProject().getLabel() );
		}
		return soapuiRunnerProperties;

	}

	public HashMap<String, String> createMockServiceRunner( HashMap<String, Object> context ) throws IOException
	{
		ComponentContext compContext = createRunner( context, MOCK_RUNNER_BASE_NAME );
		HashMap<String, String> componentProperties = new HashMap<>();
		componentProperties.put( MOCKSERVICE_RUNNER_LABEL, compContext.getLabel() );
		CanvasItem canvas = compContext.getCanvas();
		if( canvas instanceof ProjectItem )
		{
			componentProperties.put( LOADUI_PROJECT_NAME, canvas.getLabel() );
			componentProperties.put( LOADUI_TEST_CASE_NAME, null );
		}
		else if( canvas instanceof SceneItem )
		{
			componentProperties.put( LOADUI_TEST_CASE_NAME, canvas.getLabel() );
			componentProperties.put( LOADUI_PROJECT_NAME, ( ( SceneItem )canvas ).getProject().getLabel() );
		}
		return componentProperties;

	}

	/**
	 * Creates a specific runner, i.e. soapUI Runner or soapUI MockService based
	 * on context and baseComponentName
	 * 
	 * @param context
	 * @param baseComponentName
	 * @return
	 * @throws IOException
	 * @throws ComponentCreationException
	 */
	@SuppressWarnings( "unchecked" )
	private ComponentContext createRunner( HashMap<String, Object> context, String baseComponentName )
			throws IOException
	{
		HashMap<String, String> soapuiRunnerProperties = new HashMap<>();
		String componentLabel = "";
		String loadUIProjectName = ( String )context.get( LOADUI_PROJECT_NAME );
		String loadUITestCaseName = ( String )context.get( LOADUI_TEST_CASE_NAME );
		if( baseComponentName.equals( SOAPUI_RUNNER_BASE_NAME ) )
		{
			soapuiRunnerProperties = ( HashMap<String, String> )context.get( SoapUISamplerComponent.PROPERTIES );
			componentLabel = ( String )context.get( SOAPUI_RUNNER_LABEL );

		}
		else if( baseComponentName.equals( MOCK_RUNNER_BASE_NAME ) )
		{
			soapuiRunnerProperties = ( HashMap<String, String> )context.get( MockServiceComponent.PROPERTIES );
			componentLabel = ( String )context.get( MOCKSERVICE_RUNNER_LABEL );

		}

		Collection<? extends ProjectRef> projectList = workspaceProvider.getWorkspace().getProjectRefs();

		CanvasItem canvasItem = null;
		ProjectRef projectRef = findProject( loadUIProjectName, projectList );
		if( projectRef == null )
		{
			loadUIProjectName = createProjectName( projectList, context );
			canvasItem = LoadUIUtils.createProject( loadUIProjectName, workspaceProvider );
		}
		else
		{
			try
			{
				projectRef.setEnabled( true );
			}
			catch( IOException e )
			{
				log.error( "Error in opening loadUI project from soapUI" );
				throw e;
			}
			canvasItem = projectRef.getProject();
		}
		applicationState.setActiveCanvas( canvasItem );

		if( loadUITestCaseName != null )
		{
			if( loadUITestCaseName.equals( LoadUIUtils.CREATE_NEW_OPTION ) )
			{
				// loadUITestCaseName = createNewTestCaseName( canvasItem, context
				// );
				// name it as soapUI TestCase in case of soapUI Runner ar as a
				// MockService in case of soapUI MockService
				String soapUIItemName = "";
				if( baseComponentName.equals( SOAPUI_RUNNER_BASE_NAME ) )
				{
					soapUIItemName = soapuiRunnerProperties.get( SoapUiProjectSelector.TEST_CASE );
				}
				else if( baseComponentName.equals( MOCK_RUNNER_BASE_NAME ) )
				{
					soapUIItemName = soapuiRunnerProperties.get( MockServiceComponent.MOCK_SERVICE );
				}
				String[] parts = soapUIItemName.split( "@" );
				String value = null;
				if( parts.length == 2 )
				{
					value = parts[1];
				}
				if( value == null )
				{
					log.error( "Error: SoapUI TestCase name is empty" );
					return null;
				}
				loadUITestCaseName = createNewTestCaseName( canvasItem, value, context );
			}
			SceneItem testCaseItem = LoadUIUtils.findTestCase( loadUITestCaseName, ( ProjectItem )canvasItem );
			if( testCaseItem == null )
			{
				testCaseItem = LoadUIUtils.createTestCase( loadUITestCaseName, ( ProjectItem )canvasItem );
			}
			if( testCaseItem != null )
			{
				canvasItem = testCaseItem;
			}
			applicationState.setActiveCanvas( canvasItem );
		}

		ComponentItem componentItem = LoadUIUtils.findComponent( componentLabel, canvasItem );

		if( componentItem == null )
		{
			componentLabel = createNewComponentName( canvasItem, context, baseComponentName );
			if( SOAPUI_RUNNER_BASE_NAME.equals( baseComponentName ) )
			{
				componentItem = LoadUIUtils.createComponent( componentLabel, getComponentDescriptor(), canvasItem );
			}
			else
			{
				componentItem = LoadUIUtils.createComponent( componentLabel, getMockServiceDescriptor(), canvasItem );
			}
		}

		ComponentContext componentContext = componentItem.getContext();
		LoadUIUtils.extractProperties( soapuiRunnerProperties, componentContext );

		return componentContext;
	}

	/**
	 * Saves loadUI project specified by name. Workspace is searched for the
	 * project by the given name and if found, project is saved. If project does
	 * not exist in the workspace nothing happens.
	 * 
	 * @param projectName
	 *           Name of the project that has to be saved.
	 */
	private void saveLoadUIProject( String projectName )
	{
		// search for the project by name
		ProjectRef projectRef = findProject( projectName, workspaceProvider.getWorkspace().getProjectRefs() );
		if( projectRef != null )
		{
			projectRef.getProject().save();
		}
	}

	public HashMap<String, Object> exportSoapUILoadTestToLoadUI( HashMap<String, Object> context ) throws IOException
	{
		ComponentContext soapuiRunnerContext = createRunner( context, SOAPUI_RUNNER_BASE_NAME );

		createAndConnectComponents( context, soapuiRunnerContext );

		//at this point project is initialized completely so save it
		String loadUIProjectName = ( String )context.get( LOADUI_PROJECT_NAME );
		saveLoadUIProject( loadUIProjectName );

		return context;
	}

	private void createAndConnectComponents( HashMap<String, Object> context, ComponentContext componentContext )
	{
		CanvasItem canvasItem = componentContext.getCanvas();
		InputTerminal soapUIRunnerInputTerminal = ( InputTerminal )LoadUIUtils.findTerminalByName(
				componentContext.getTerminals(), GeneratorCategory.TRIGGER_TERMINAL );
		OutputTerminal soapUIRunnerOutputTerminal = ( OutputTerminal )LoadUIUtils.findTerminalByName(
				componentContext.getTerminals(), RunnerCategory.RESULT_TERMINAL );
		ComponentItem statisticsComponentItem = null;
		ComponentItem triggerComponentItem = null;
		// first create the components in this particular order so they can be
		// positioned in a way to all be visible
		if( context.get( STATISTICS_TYPE ) != null )
		{
			ComponentDescriptor statisticsComponentDescriptor = LoadUIUtils.findComponentDescriptor(
					( String )context.get( STATISTICS_TYPE ), getComponentRegistry() );
			statisticsComponentItem = LoadUIUtils.getComponent( context, canvasItem, statisticsComponentDescriptor,
					STATISTICS_LABEL, STATISTICS_CREATE_NEW );
		}

		List<ComponentItem> assertionsList = new ArrayList<>();
		for( String assertion : getAssertionTypeKeys( context ) )
		{
			String i = assertion.substring( ASSERTION_TYPE.length() );

			if( context.get( ASSERTION_TYPE + i ) != null )
			{
				ComponentDescriptor assertionComponentDescriptor = LoadUIUtils.findComponentDescriptor(
						( String )context.get( ASSERTION_TYPE + i ), getComponentRegistry() );
				ComponentItem assertionComponentItem = LoadUIUtils.getComponent( context, canvasItem,
						assertionComponentDescriptor, ASSERTION_LABEL + i, ASSERTION_CREATE_NEW + i );
				assertionsList.add( assertionComponentItem );
			}
		}
		if( context.get( TRIGGER_TYPE ) != null )
		{
			ComponentDescriptor triggerComponentDescriptor = LoadUIUtils.findComponentDescriptor(
					( String )context.get( TRIGGER_TYPE ), getComponentRegistry() );
			triggerComponentItem = LoadUIUtils.getComponent( context, canvasItem, triggerComponentDescriptor,
					TRIGGER_LABEL, TRIGGER_CREATE_NEW );
		}
		// if( context.get( DELAY_TYPE ) != null )
		// {
		// ComponentDescriptor delayComponentDescriptor =
		// LoadUIUtils.findComponenetDescriptor(
		// ( String )context.get( DELAY_TYPE ), getComponentRegistry() );
		// delayComponentItem = LoadUIUtils.getComponent( context, canvasItem,
		// delayComponentDescriptor, DELAY_LABEL,
		// DELAY_CREATE_NEW );
		// }

		// after creation is done connect all components to each other as
		// specified
		OutputTerminal statisticsOutputTerminal = null;
		if( statisticsComponentItem != null )
		{
			InputTerminal statisticsInputTerminal = ( InputTerminal )LoadUIUtils.findTerminalByName(
					statisticsComponentItem.getTerminals(), AnalysisCategory.INPUT_TERMINAL );
			statisticsOutputTerminal = ( OutputTerminal )LoadUIUtils.findTerminalByName(
					statisticsComponentItem.getTerminals(), OUTPUT );
			canvasItem.connect( soapUIRunnerOutputTerminal, statisticsInputTerminal );
			ComponentContext statisticsComponentContext = statisticsComponentItem.getContext();
			HashMap<String, String> statisticsProperties = ( HashMap<String, String> )context.get( STATISTICS_PROPERTIES );
			LoadUIUtils.extractProperties( statisticsProperties, statisticsComponentContext );
			LoadUIUtils.savesBackProperties( context, STATISTICS_PROPERTIES, statisticsComponentContext );
		}
		for( String assertion : getAssertionTypeKeys( context ) )
		{
			String i = assertion.substring( ASSERTION_TYPE.length() );
			int j = Integer.parseInt( i );
			ComponentItem assertionComponentItem = assertionsList.get( j );

			if( context.get( ASSERTION_TYPE + i ) != null )
			{
				InputTerminal assertionInputTerminal = ( InputTerminal )LoadUIUtils.findTerminalByName(
						assertionComponentItem.getTerminals(), AnalysisCategory.INPUT_TERMINAL );
				canvasItem.connect( statisticsOutputTerminal, assertionInputTerminal );
				ComponentContext assertionComponentContext = assertionComponentItem.getContext();
				HashMap<String, String> assertionProperties = ( HashMap<String, String> )context.get( ASSERTION_PROPERTIES
						+ i );
				LoadUIUtils.extractProperties( assertionProperties, assertionComponentContext );
				LoadUIUtils.savesBackProperties( context, ASSERTION_PROPERTIES + i, assertionComponentContext );
			}
		}
		// if( context.get( DELAY_TYPE ) != null )
		// {
		// InputTerminal delayInputTerminal = ( InputTerminal
		// )LoadUIUtils.findTerminal(
		// delayComponentItem.getTerminals(), FlowCategory.INCOMING_TERMINAL );
		// OutputTerminal delayOutputTerminal = ( OutputTerminal
		// )LoadUIUtils.findTerminal(
		// delayComponentItem.getTerminals(), OUTPUT );
		// InputTerminal fixedLoadInputTerminal = fixedLoadSampleCountTerminal(
		// context, canvasItem );
		//
		// canvasItem.connect( soapUIRunnerOutputTerminal, delayInputTerminal );
		// canvasItem.connect( delayOutputTerminal, fixedLoadInputTerminal );
		//
		// ComponentContext delayComponentContext =
		// delayComponentItem.getContext();
		// HashMap<String, String> delayProperties = ( HashMap<String, String>
		// )context.get( DELAY_PROPERTIES );
		// LoadUIUtils.extractProperties( delayProperties, delayComponentContext
		// );
		// LoadUIUtils.savesBackProperties( context, DELAY_PROPERTIES,
		// delayComponentContext );
		// }
		if( triggerComponentItem != null )
		{
			OutputTerminal triggerOutputTerminal = ( OutputTerminal )LoadUIUtils.findTerminalByName(
					triggerComponentItem.getTerminals(), GeneratorCategory.TRIGGER_TERMINAL );
			canvasItem.connect( triggerOutputTerminal, soapUIRunnerInputTerminal );

			if( context.get( TRIGGER_TYPE ).equals( FIXED_LOAD ) )
			{
				InputTerminal fixedLoadInputTerminal = fixedLoadSampleCountTerminal( context, canvasItem );
				OutputTerminal soapUIRunnerRunningTerminal = ( OutputTerminal )LoadUIUtils.findTerminalByName(
						componentContext.getTerminals(), RunnerCategory.CURRENLY_RUNNING_TERMINAL );
				canvasItem.connect( soapUIRunnerRunningTerminal, fixedLoadInputTerminal );
			}

			ComponentContext triggerComponentContext = triggerComponentItem.getContext();
			HashMap<String, String> triggerProperties = ( HashMap<String, String> )context.get( TRIGGER_PROPERTIES );
			LoadUIUtils.extractProperties( triggerProperties, triggerComponentContext );
			LoadUIUtils.savesBackProperties( context, TRIGGER_PROPERTIES, triggerComponentContext );
		}
	}

	private InputTerminal fixedLoadSampleCountTerminal( HashMap<String, Object> context, CanvasItem canvasItem )
	{
		ComponentDescriptor fixedRateComponentDescriptor = LoadUIUtils.findComponentDescriptor(
				( String )context.get( TRIGGER_TYPE ), getComponentRegistry() );
		ComponentItem fixedLoadComponentItem = LoadUIUtils.getComponent( context, canvasItem,
				fixedRateComponentDescriptor, TRIGGER_LABEL, null );
		InputTerminal fixedLoadInputTerminal = ( InputTerminal )LoadUIUtils.findTerminalByName(
				fixedLoadComponentItem.getTerminals(), SAMPLE_COUNT );
		return fixedLoadInputTerminal;
	}

	private List<String> getAssertionTypeKeys( HashMap<String, Object> context )
	{
		List<String> assertions = new ArrayList<>();
		for( String key : context.keySet() )
		{
			if( key.contains( ASSERTION_TYPE ) )
			{
				assertions.add( key );
			}
		}
		return assertions;
	}

	private ProjectRef findProject( String loadUIProjectName, Collection<? extends ProjectRef> projectList )
	{
		if( projectList != null )
		{
			for( ProjectRef proj : projectList )
			{
				if( proj.getLabel().equalsIgnoreCase( loadUIProjectName ) )
				{
					return proj;
				}
			}
		}
		return null;
	}

	/**
	 * Creates a new name for SoapUI Runner or MockService Runner based on
	 * baseComponentName in form of "SoapUI Runner (count+1)" or MockService
	 * Runner (count+1)
	 * 
	 * @param canvasItem
	 * @param context
	 * @param baseComponentName
	 * @return
	 */
	private String createNewComponentName( CanvasItem canvasItem, HashMap<String, Object> context,
			String baseComponentName )
	{
		String runnerLabel = "";
		int i = LoadUIUtils.getRunners( canvasItem, baseComponentName ).size();
		if( baseComponentName.equals( SOAPUI_RUNNER_BASE_NAME ) )
		{
			do
			{
				runnerLabel = SOAPUI_RUNNER_BASE_NAME + " (" + ( ++i ) + ")";
			}
			while( LoadUIUtils.findComponent( runnerLabel, canvasItem ) != null );
			context.put( SOAPUI_RUNNER_LABEL, runnerLabel );
		}
		else if( baseComponentName.equals( MOCK_RUNNER_BASE_NAME ) )
		{
			do
			{
				runnerLabel = MOCK_RUNNER_BASE_NAME + "(" + ( ++i ) + ")";
			}
			while( LoadUIUtils.findComponent( runnerLabel, canvasItem ) != null );
			context.put( MOCKSERVICE_RUNNER_LABEL, runnerLabel );
		}
		return runnerLabel;
	}

	private static String createNewTestCaseName( CanvasItem canvasItem, String nameToCheck, Map<String, Object> context )
	{
		String loadUITestCaseName;
		int i = 0;
		for( SceneItem sceneItem : canvasItem.getChildren() )
		{
			if( sceneItem.getLabel().equals( nameToCheck ) )
			{
				i++ ;
			}
		}
		do
		{
			loadUITestCaseName = nameToCheck + "(" + ( ++i ) + ")";
		}
		while( LoadUIUtils.findTestCase( loadUITestCaseName, ( ProjectItem )canvasItem ) != null );
		context.put( LOADUI_TEST_CASE_NAME, loadUITestCaseName );
		return loadUITestCaseName;
	}

	private String createProjectName( Collection<? extends ProjectRef> projectList, HashMap<String, Object> context )
	{

		int i = projectList.size();
		String loadUIProjectName = "Project " + ( ++i );
		while( findProject( loadUIProjectName, projectList ) != null )
		{
			loadUIProjectName = "Project " + ( ++i );
		}

		context.put( LOADUI_PROJECT_NAME, loadUIProjectName );
		return loadUIProjectName;
	}

	public void setWindowController( WindowController windowController )
	{
		this.windowController = windowController;
	}

	public void setWorkspaceProvider( WorkspaceProvider workspaceProvider )
	{
		this.workspaceProvider = workspaceProvider;
	}

	public ComponentDescriptor getComponentDescriptor()
	{
		return soapuiRunnerDescriptor;
	}

	public void setComponentDescriptor( ComponentDescriptor componentDescriptor )
	{
		this.soapuiRunnerDescriptor = componentDescriptor;
	}

	public ComponentDescriptor getMockServiceDescriptor()
	{
		return mockServiceDescriptor;
	}

	public void setMockServiceDescriptor( ComponentDescriptor componentDescriptor )
	{
		this.mockServiceDescriptor = componentDescriptor;
	}

	public List<String> getProjects()
	{
		return LoadUIUtils.getProjectsRefsLabelsList( workspaceProvider );
	}

	public List<String> getTestCases()
	{
		return LoadUIUtils.getTestCasesLabelsList( workspaceProvider );
	}

	public List<String> getSoapUIRunners( String projectName, String sceneName )
	{
		return LoadUIUtils.getRunnersLabelsList( workspaceProvider, projectName, sceneName, SOAPUI_RUNNER_BASE_NAME );
	}

	public List<String> getMockServiceRunners( String projectName, String sceneName )
	{
		return LoadUIUtils.getRunnersLabelsList( workspaceProvider, projectName, sceneName, MOCK_RUNNER_BASE_NAME );
	}

	public boolean isProjectOpened( String projectName )
	{
		return LoadUIUtils.isProjectOpened( workspaceProvider, projectName );
	}

	public String getOpenedProjectName()
	{
		return LoadUIUtils.getOpenedProjectName( workspaceProvider );
	}

	public void closeOpenedProject()
	{
		LoadUIUtils.closeOpenedProject( workspaceProvider );
	}

	public void saveOpenedProject()
	{
		LoadUIUtils.saveOpenedProject( workspaceProvider );
	}

	public Boolean isOpenedProjectDirty()
	{
		return LoadUIUtils.isOpenedProjectDirty( workspaceProvider );
	}

	public ComponentRegistry getComponentRegistry()
	{
		return componentRegistry;
	}

	public void setComponentRegistry( ComponentRegistry componentRegistry )
	{
		this.componentRegistry = componentRegistry;
	}

	public WorkspaceProvider getWorkspaceProvider()
	{
		return workspaceProvider;
	}

	public void setApplicationState( ApplicationState applicationState )
	{
		this.applicationState = applicationState;
	}

}
