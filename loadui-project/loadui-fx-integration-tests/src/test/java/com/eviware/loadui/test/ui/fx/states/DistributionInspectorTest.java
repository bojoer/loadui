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
package com.eviware.loadui.test.ui.fx.states;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import javafx.scene.Node;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.GUI;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.eviware.loadui.util.BeanInjector;

/**
 * Integration tests for testing the loadUI controller through its API.
 * 
 * @author henrik.olsson
 */
@Category( IntegrationTest.class )
public class DistributionInspectorTest
{
	private static ArrayList<Node> toolBoxItems;
	private static ArrayList<Node> agentViewBoxes;

	@BeforeClass
	public static void enterState() throws Exception
	{
		TwoScenariosCreatedState.STATE.enter();

		GUI.getController().click( "#Distribution" );

		toolBoxItems = new ArrayList<Node>( TestFX.findAll( ".distribution-view .scenario-toolbox-item .image",
				TestFX.find( ".distribution-view" ) ) );

		agentViewBoxes = new ArrayList<Node>( TestFX.findAll( ".distribution-view .agent-view .item-box",
				TestFX.find( ".distribution-view" ) ) );

		for( Node scenario : toolBoxItems )
		{
			GUI.getController().drag( scenario ).to( agentViewBoxes.get( 0 ) );
		}

		GUI.getController().drag( toolBoxItems.get( 0 ) ).to( agentViewBoxes.get( 1 ) );
		GUI.getController().drag( toolBoxItems.get( 1 ) ).to( agentViewBoxes.get( 2 ) );

	}

	@AfterClass
	public static void leaveState() throws Exception
	{
		GUI.getController().click( "#Distribution" ).click( "#Distribution" );

		TwoScenariosCreatedState.STATE.getParent().enter();
	}

	@Test
	public void scenariosShouldBeDeployed()
	{
		assertEquals( TestFX.findAll( ".assignment-view", agentViewBoxes.get( 0 ) ).size(), 2 );
		assertEquals( TestFX.findAll( ".assignment-view", agentViewBoxes.get( 1 ) ).size(), 1 );
		assertEquals( TestFX.findAll( ".assignment-view", agentViewBoxes.get( 2 ) ).size(), 1 );

		ProjectItem project = BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace().getProjects().iterator()
				.next();

		assertEquals( project.getAssignments().size(), 4 );
	}

	@Test
	public void assignmentShouldDragAndDrop()
	{
		ArrayList<Node> firstScenarioAssigments = new ArrayList<Node>( TestFX.findAll( ".assignment-view",
				agentViewBoxes.get( 0 ) ) );

		GUI.getController().drag( firstScenarioAssigments.get( 0 ) ).to( agentViewBoxes.get( 1 ) );

		assertEquals( TestFX.findAll( ".assignment-view", agentViewBoxes.get( 0 ) ).size(), 2 );
		assertEquals( TestFX.findAll( ".assignment-view", agentViewBoxes.get( 1 ) ).size(), 1 );

		GUI.getController().drag( firstScenarioAssigments.get( 1 ) ).to( agentViewBoxes.get( 1 ) );

		assertEquals( TestFX.findAll( ".assignment-view", agentViewBoxes.get( 0 ) ).size(), 1 );
		assertEquals( TestFX.findAll( ".assignment-view", agentViewBoxes.get( 1 ) ).size(), 2 );

		GUI.getController().drag( firstScenarioAssigments.get( 0 ) ).to( agentViewBoxes.get( 2 ) );

		assertEquals( TestFX.findAll( ".assignment-view", agentViewBoxes.get( 0 ) ).size(), 0 );
		assertEquals( TestFX.findAll( ".assignment-view", agentViewBoxes.get( 2 ) ).size(), 2 );

		ArrayList<Node> secoundScenarioAssigments = new ArrayList<Node>( TestFX.findAll( ".assignment-view",
				agentViewBoxes.get( 1 ) ) );

		ArrayList<Node> thirdScenarioAssigments = new ArrayList<Node>( TestFX.findAll( ".assignment-view",
				agentViewBoxes.get( 2 ) ) );

		GUI.getController().drag( thirdScenarioAssigments.get( 1 ) ).to( agentViewBoxes.get( 0 ) );
		GUI.getController().drag( secoundScenarioAssigments.get( 1 ) ).to( agentViewBoxes.get( 0 ) );

		// original state
		assertEquals( TestFX.findAll( ".assignment-view", agentViewBoxes.get( 0 ) ).size(), 2 );
		assertEquals( TestFX.findAll( ".assignment-view", agentViewBoxes.get( 1 ) ).size(), 1 );
		assertEquals( TestFX.findAll( ".assignment-view", agentViewBoxes.get( 2 ) ).size(), 1 );

	}

	@Test
	public void assignmentShouldBeAddableAndDeleteable()
	{
		ArrayList<Node> firstScenarioAssigments = new ArrayList<Node>( TestFX.findAll( ".assignment-view",
				agentViewBoxes.get( 0 ) ) );

		GUI.getController().drag( toolBoxItems.get( 0 ) ).to( agentViewBoxes.get( 0 ) );

		assertEquals( TestFX.findAll( ".assignment-view", agentViewBoxes.get( 0 ) ).size(), 2 );

		GUI.getController().click( TestFX.find( ".menu-button", firstScenarioAssigments.get( 0 ) ) ).click( "#delete" );

		assertEquals( TestFX.findAll( ".assignment-view", agentViewBoxes.get( 0 ) ).size(), 1 );

		GUI.getController().click( TestFX.find( ".menu-button", firstScenarioAssigments.get( 1 ) ) ).click( "#delete" );

		assertEquals( TestFX.findAll( ".assignment-view", agentViewBoxes.get( 0 ) ).size(), 0 );

		ProjectItem project = BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace().getProjects().iterator()
				.next();

		assertEquals( project.getAssignments().size(), 2 );

		// reset to original state
		for( Node scenario : toolBoxItems )
		{
			GUI.getController().drag( scenario ).to( agentViewBoxes.get( 0 ) );
		}

		// original state
		assertEquals( TestFX.findAll( ".assignment-view", agentViewBoxes.get( 0 ) ).size(), 2 );
	}
}
