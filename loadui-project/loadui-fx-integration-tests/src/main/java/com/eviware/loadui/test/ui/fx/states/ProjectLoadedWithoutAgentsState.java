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
package com.eviware.loadui.test.ui.fx.states;

import static com.eviware.loadui.ui.fx.util.test.TestFX.findAll;

import java.util.Collection;
import java.util.concurrent.Callable;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.GUI;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.test.TestUtils;

public class ProjectLoadedWithoutAgentsState extends TestState
{
	public static final ProjectLoadedWithoutAgentsState STATE = new ProjectLoadedWithoutAgentsState();

	private ProjectItem project = null;

	private ProjectLoadedWithoutAgentsState()
	{
		this( "Project Loaded", ProjectCreatedWithoutAgentsState.STATE );
	}
	
	protected ProjectLoadedWithoutAgentsState( String name, TestState parent ) {
		super( name, parent );
	}

	public ProjectItem getProject()
	{
		return project;
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		log.debug( "Opening project." );
		GUI.getController().click( ".project-ref-view #menuButton" ).click( "#open-item" );

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return !findAll( ".project-view" ).isEmpty();
			}
		} );

		Collection<? extends ProjectItem> projects = BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace()
				.getProjects();
		project = projects.iterator().next();
	}

	@Override
	protected void exitToParent() throws Exception
	{
		log.debug( "Closing project." );
		project = null;
		GUI.getController().click( "#closeProjectButton" );
		//If there is a save dialog, do not save:
		try
		{
			GUI.getController().click( "#no" ).target( TestFX.getWindowByIndex( 0 ) );
		}
		catch( Exception e )
		{
			//Ignore
		}

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return !findAll( ".workspace-view" ).isEmpty();
			}
		} );
	}
}
