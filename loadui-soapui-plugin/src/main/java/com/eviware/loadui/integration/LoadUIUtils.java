/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
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
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.components.soapui.MockServiceComponent;
import com.eviware.loadui.components.soapui.SoapUISamplerComponent;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class LoadUIUtils
{
	private static Logger logger = LoggerFactory.getLogger( "com.eviware.loadui.integration.LoadUIUtils" );

	public static final String CREATE_NEW_OPTION = "<Create New>";
	public static final String CREATE_ON_PROJECT_LEVEL = "<Project Level>";

	public static List<String> getProjectsRefsLabelsList( WorkspaceProvider workspaceProvider )
	{
		List<String> projectsRefsLabelsList = new ArrayList<>();

		List<ProjectRef> projectList = new ArrayList<>();
		projectList.addAll( workspaceProvider.getWorkspace().getProjectRefs() );
		for( ProjectRef projectRef : projectList )
		{
			projectsRefsLabelsList.add( projectRef.getLabel() );
		}
		return projectsRefsLabelsList;
	}

	public static List<ProjectRef> getProjectsRefsList( WorkspaceProvider workspaceProvider )
	{
		List<ProjectRef> projectsRefsList = new ArrayList<>();
		for( ProjectRef projectRef : workspaceProvider.getWorkspace().getProjectRefs() )
		{
			projectsRefsList.add( projectRef );
		}
		return projectsRefsList;
	}

	public static String getOpenedProjectName( WorkspaceProvider workspaceProvider )
	{
		ProjectItem projectItem = Iterables.getFirst( workspaceProvider.getWorkspace().getProjects(), null );
		String openedProjectName = projectItem != null ? projectItem.getLabel() : "";

		return openedProjectName;
	}

	/*
	 * gets TestCases(i.e. scenes ) for opened project
	 */
	public static List<SceneItem> getTestCases( WorkspaceProvider workspaceProvider )
	{
		Collection<? extends ProjectItem> projectList = workspaceProvider.getWorkspace().getProjects();
		if( !projectList.isEmpty() )
		{
			ProjectItem projectItem = Iterables.getFirst( projectList, null );
			return Lists.newArrayList( projectItem.getChildren() );
		}
		return Lists.newArrayList();
	}

	/*
	 * gets TestCase(i.e. sceen ) labels for opened project
	 */
	public static List<String> getTestCasesLabelsList( WorkspaceProvider workspaceProvider )
	{
		return Lists.<SceneItem, String> transform( getTestCases( workspaceProvider ), new Function<SceneItem, String>()
		{
			@Override
			public String apply( SceneItem scene )
			{
				return scene.getLabel();
			}
		} );
	}

	/**
	 * If sceneName != null returns a list of labels for all specific Runners for
	 * opened project otherwise for particular scene based on baseComponentName
	 * all SoapUI Runners labels or all MockService Runners labels are returned
	 * 
	 * @param workspaceProvider
	 * @param projectName
	 * @param sceneName
	 * @param baseComponentName
	 * @return
	 */
	public static List<String> getRunnersLabelsList( WorkspaceProvider workspaceProvider, String projectName,
			String sceneName, String baseComponentName )
	{
		List<String> runnersLablesList = Lists.newArrayList();
		Collection<? extends ProjectItem> projectList = workspaceProvider.getWorkspace().getProjects();
		ProjectItem theProject = null;
		if( projectList != null && !projectList.isEmpty() )
		{
			theProject = Iterables.getOnlyElement( projectList );
		}
		if( theProject != null && projectName.equals( theProject.getLabel() ) )
		{
			if( sceneName.equals( CREATE_ON_PROJECT_LEVEL ) )
			{
				Collection<? extends ComponentItem> components = theProject.getProject().getComponents();
				if( components != null && !components.isEmpty() )
				{
					for( ComponentItem componentItem : components )
					{
						if( ( baseComponentName.equals( LoadUIIntegrator.SOAPUI_RUNNER_BASE_NAME ) && componentItem
								.getBehavior() instanceof SoapUISamplerComponent )
								|| ( baseComponentName.equals( LoadUIIntegrator.MOCK_RUNNER_BASE_NAME ) && componentItem
										.getBehavior() instanceof MockServiceComponent ) )
						{
							runnersLablesList.add( componentItem.getLabel() );
						}
					}
				}
			}
			else if( !sceneName.equals( CREATE_NEW_OPTION ) )
			{
				SceneItem theScene = null;
				List<SceneItem> testCases = getTestCases( workspaceProvider );
				for( SceneItem sceneItem : testCases )
				{
					if( sceneItem.getLabel().equalsIgnoreCase( sceneName.toUpperCase() ) )
					{
						theScene = sceneItem;
					}
				}
				if( theScene == null )
				{
					throw new NoSuchElementException( "No such Scenario: " + sceneName );
				}
				Collection<? extends ComponentItem> components = theScene.getComponents();
				for( ComponentItem componentItem : components )
				{
					if( ( baseComponentName.equals( LoadUIIntegrator.SOAPUI_RUNNER_BASE_NAME ) && componentItem
							.getBehavior() instanceof SoapUISamplerComponent )
							|| ( baseComponentName.equals( LoadUIIntegrator.MOCK_RUNNER_BASE_NAME ) && componentItem
									.getBehavior() instanceof MockServiceComponent ) )
					{
						runnersLablesList.add( componentItem.getLabel() );
					}
				}
			}
		}

		return runnersLablesList;
	}

	/**
	 * Gets all specific runners in the Canvas based on baseComponentName i.e.
	 * all SoapUI Runners or all MockService Runners
	 * 
	 * @param canvasItem
	 * @param baseComponentName
	 * @return
	 */
	public static Set<ComponentItem> getRunners( CanvasItem canvasItem, String baseComponentName )
	{
		Collection<? extends ComponentItem> canvasComponents = canvasItem.getComponents();
		Set<ComponentItem> components = Sets.newHashSet();
		for( ComponentItem componentItem : canvasComponents )
		{
			if( baseComponentName.equals( LoadUIIntegrator.SOAPUI_RUNNER_BASE_NAME )
					&& componentItem.getBehavior() instanceof SoapUISamplerComponent )
			{
				components.add( componentItem );
			}
			else if( baseComponentName.equals( LoadUIIntegrator.MOCK_RUNNER_BASE_NAME )
					&& componentItem.getBehavior() instanceof MockServiceComponent )
			{
				components.add( componentItem );
			}
		}
		return components;

	}

	public static boolean isProjectOpened( WorkspaceProvider workspaceProvider, String projectName )
	{
		List<ProjectRef> projectsRefs = getProjectsRefsList( workspaceProvider );
		for( ProjectRef projetRef : projectsRefs )
		{
			if( projetRef.getLabel().equalsIgnoreCase( projectName.toUpperCase() ) )
			{
				return projetRef.isEnabled();
			}
		}
		return false;
	}

	/**
	 * Closes currently opened loadUI project.
	 * 
	 * @param workspaceProvider
	 */
	public static void closeOpenedProject( WorkspaceProvider workspaceProvider )
	{
		String projectName = getOpenedProjectName( workspaceProvider );
		List<ProjectRef> projectsRefs = getProjectsRefsList( workspaceProvider );
		for( ProjectRef projetRef : projectsRefs )
		{
			if( projetRef.getLabel().equalsIgnoreCase( projectName.toUpperCase() ) )
			{
				try
				{
					projetRef.setEnabled( false );
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Saves currently opened loadUI project.
	 * 
	 * @param workspaceProvider
	 */
	public static void saveOpenedProject( WorkspaceProvider workspaceProvider )
	{
		String projectName = getOpenedProjectName( workspaceProvider );
		List<ProjectRef> projectsRefs = getProjectsRefsList( workspaceProvider );
		for( ProjectRef projetRef : projectsRefs )
		{
			if( projetRef.getLabel().equalsIgnoreCase( projectName.toUpperCase() ) )
			{
				// project found, save and return
				projetRef.getProject().save();
				return;
			}
		}
	}

	/**
	 * Checks if currently opened project is dirty.
	 * 
	 * @param workspaceProvider
	 * @return true if currently opened project is dirty, false otherwise.
	 */
	public static Boolean isOpenedProjectDirty( WorkspaceProvider workspaceProvider )
	{
		String projectName = getOpenedProjectName( workspaceProvider );
		List<ProjectRef> projectsRefs = getProjectsRefsList( workspaceProvider );
		for( ProjectRef projetRef : projectsRefs )
		{
			if( projetRef.getLabel().equalsIgnoreCase( projectName.toUpperCase() ) )
			{
				return projetRef.getProject().isDirty();
			}
		}
		throw new IllegalStateException( "No project in workspace." );
	}

	public static ComponentDescriptor findComponentDescriptor( String label, ComponentRegistry componentRegistry )
	{
		for( ComponentDescriptor cd : componentRegistry.getDescriptors() )
		{
			if( cd.getLabel().equals( label ) )
				return cd;
		}
		logger.warn( "No component for label name =" + label + " found in registry !" );
		return null;
	}

	@Nonnull
	public static ComponentItem createComponent( String labelName, ComponentDescriptor componentDescriptor,
			CanvasItem canvasItem )
	{
		try
		{
			return canvasItem.createComponent( labelName, componentDescriptor );
		}
		catch( ComponentCreationException e )
		{
			throw new RuntimeException( e );
		}
	}

	public static Terminal findTerminalByName( Collection<Terminal> terminals, String name )
	{
		for( Terminal terminal : terminals )
		{
			if( terminal.getName().equalsIgnoreCase( name ) )
				return terminal;
		}
		logger.warn( "No terminal found  for name = " + name + " !" );

		return null;

	}

	public static SceneItem createTestCase( String loadUITestCaseName, ProjectItem projectItem )
	{
		return projectItem.createScene( loadUITestCaseName );
	}

	public static SceneItem findTestCase( String loadUITestCaseName, ProjectItem projectItem )
	{
		for( SceneItem sceneItem : projectItem.getChildren() )
		{
			if( sceneItem.getLabel().equals( loadUITestCaseName ) )
			{
				return sceneItem;
			}
		}
		return null;
	}

	public static ComponentItem findComponent( String labelName, CanvasItem canvasItem )
	{
		for( ComponentItem comItem : canvasItem.getComponents() )
		{
			if( comItem.getLabel().equals( labelName ) )
			{
				return comItem;
			}
		}
		return null;
	}

	public static ProjectItem createProject( String loadUIProjectName, WorkspaceProvider workspaceProvider )
	{
		String filename = loadUIProjectName.replace( " ", "-" ).toLowerCase().concat( ".xml" );
		File fileDir = new File( System.getProperty( "loadui.home" ) );

		File newProjectFile = tweakProjectFilename( loadUIProjectName, filename, fileDir );
		logger.debug( "project file name = " + newProjectFile.getAbsolutePath() );
		return workspaceProvider.getWorkspace().createProject( newProjectFile, loadUIProjectName, true ).getProject();
	}

	private static File tweakProjectFilename( String loadUIProjectName, String filename, File fileDir )
	{
		File newProjectFile = new File( fileDir, filename );
		int i = 1;
		while( newProjectFile.exists() )
		{
			filename = loadUIProjectName.replace( " ", "-" ).toLowerCase() + "_" + i + ".xml";
			newProjectFile = new File( fileDir, filename );
			i++ ;
		}
		return newProjectFile;
	}

	public static void extractProperties( HashMap<String, String> itemProperties, ComponentContext componentContext )
	{
		for( String key : itemProperties.keySet() )
		{
			String[] parts = itemProperties.get( key ).split( "@" );
			String clazz = parts[0];
			String value = "";
			if( parts.length == 2 )
			{
				value = parts[1];
			}
			try
			{
				componentContext.getProperty( key ).setValue(
						Class.forName( clazz ).getConstructor( String.class ).newInstance( value ) );
			}
			catch( Exception e )
			{
				logger.error( "extract property value error for  key=" + key + " and value=" + itemProperties.get( key ), e );
			}
		}
	}

	public static ComponentItem getComponent( HashMap<String, Object> context, CanvasItem canvasItem,
			ComponentDescriptor componentDescriptor, String labelKey, String createNewKey )
	{
		boolean forceCreateNew = checkCreateNew( context, createNewKey );
		String label = ( String )context.get( labelKey );
		ComponentItem componentItem = LoadUIUtils.findComponent( label, canvasItem );
		if( componentItem == null || forceCreateNew )
		{
			if( forceCreateNew )
			{
				label = createNewLabelName( canvasItem, label, context, labelKey );
			}
			componentItem = LoadUIUtils.createComponent( label, componentDescriptor, canvasItem );
		}
		return componentItem;
	}

	public static String createNewLabelName( CanvasItem canvasItem, String label, HashMap<String, Object> context,
			String labelKey )
	{
		String labelName = label;
		int i = 0;
		while( LoadUIUtils.findComponent( labelName, canvasItem ) != null )
		{
			labelName = label + " (" + ( ++i ) + ")";
		}

		context.put( labelKey, labelName );

		return labelName;
	}

	public static boolean checkCreateNew( HashMap<String, Object> context, String key )
	{
		boolean createNew = ( context.get( key ) != null && ( Boolean )context.get( key ) ) ? true : false;
		context.remove( key );
		return createNew;
	}

	public static void savesBackProperties( HashMap<String, Object> context, String propertiesKey,
			ComponentContext componentContext )
	{
		Collection<Property<?>> properties = componentContext.getProperties();
		HashMap<String, String> propertiesMap = new HashMap<>();
		for( Property<?> property : properties )
		{
			if( !property.getKey().contains( "_script" ) )
			{
				propertiesMap.put( property.getKey(), property.getType().getName() + "@" + "" + property.getStringValue() );
			}
		}
		context.put( propertiesKey, propertiesMap );
	}

}
