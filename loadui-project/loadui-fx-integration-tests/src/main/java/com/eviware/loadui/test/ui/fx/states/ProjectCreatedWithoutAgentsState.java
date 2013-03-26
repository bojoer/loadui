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

import static com.eviware.loadui.ui.fx.util.test.TestFX.find;
import static com.eviware.loadui.util.test.TestUtils.awaitCondition;

import java.util.concurrent.Callable;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.GUI;

public class ProjectCreatedWithoutAgentsState extends TestState
{

	public static final ProjectCreatedWithoutAgentsState STATE = new ProjectCreatedWithoutAgentsState();

	private ProjectCreatedWithoutAgentsState()
	{
		this( "Project without agents created", FXAppLoadedState.STATE );
	}

	protected ProjectCreatedWithoutAgentsState( String name, TestState parent )
	{
		super( name, parent );
	}

	// This method randomly throws an IndexOutOfBoundsException which breaks the test.
	// TODO: We should look into it once the source code for ObservableList is released.
	@Override
	protected void enterFromParent() throws Exception
	{
		final Node projectCarousel = find( "#projectRefCarousel" );
		log.debug( "Creating new project." );

		GUI.getController().sleep( 2000 ).drag( "#newProjectIcon" ).to( projectCarousel ).type( "Project 1" )
				.type( KeyCode.TAB ).type( "project-1.xml" ).click( ".check-box" ).click( "#default" );

		awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return projectCarousel.lookup( ".project-ref-view" ) != null;
			}
		} );
	}

	@Override
	protected void exitToParent() throws Exception
	{
		log.debug( "Deleting project." );
		GUI.getController().click( "#projectRefCarousel .project-ref-view .menu-button" ).click( "#delete-item" )
				.click( ".confirmation-dialog #default" );
		final Node projectCarousel = find( "#projectRefCarousel" );

		awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return projectCarousel.lookup( ".project-ref-view" ) == null;
			}
		} );
	}

}
