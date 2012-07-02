package com.eviware.loadui.test.ui.fx.states;

import java.util.concurrent.Callable;

import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.GUI;
import com.eviware.loadui.ui.fx.util.test.FXRobot;
import com.eviware.loadui.util.test.TestUtils;
import com.google.common.collect.Iterables;

public class ProjectCreatedState extends TestState
{
	public static final TestState STATE = new ProjectCreatedState();

	private ProjectCreatedState()
	{
		super( "Project Created", TestState.ROOT );
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		FXRobot robot = GUI.getRobot();
		@SuppressWarnings( "unchecked" )
		final ListView<Node> projectList = ( ListView<Node> )GUI.getStage().getScene().lookup( "#projectRefNodeList" );

		robot.click( projectList, MouseButton.SECONDARY );
		robot.mouseMoveBy( 15, 10 );
		robot.click();

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return projectList.getItems().size() == 1;
			}
		} );
	}

	@Override
	protected void exitToParent() throws Exception
	{
		FXRobot robot = GUI.getRobot();

		@SuppressWarnings( "unchecked" )
		final ListView<Node> projectList = ( ListView<Node> )GUI.getStage().getScene().lookup( "#projectRefNodeList" );

		System.out.println( "ProjectList items: " + projectList.getItems() );

		Node projectRef = Iterables.getOnlyElement( projectList.getItems() );

		robot.click( projectRef.lookup( ".button" ) );

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return projectList.getItems().size() == 0;
			}
		} );
	}
}
