package com.eviware.loadui.test.ui.fx.states;

import java.util.concurrent.Callable;

import javafx.scene.Node;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.GUI;
import com.eviware.loadui.ui.fx.util.test.FXRobot;
import com.eviware.loadui.util.test.TestUtils;

public class ProjectLoadedState extends TestState
{
	public static final TestState STATE = new ProjectLoadedState();

	private ProjectLoadedState()
	{
		super( "Project Loaded", ProjectCreatedState.STATE );
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		FXRobot robot = GUI.getRobot();

		Node projectRefNode = GUI.getStage().getScene().lookup( ".project-ref-node" );
		robot.move( projectRefNode ).by( -50, 25 );
		robot.click();

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return GUI.getStage().getScene().lookup( ".project-view" ) != null;
			}
		} );
	}

	@Override
	protected void exitToParent() throws Exception
	{
		FXRobot robot = GUI.getRobot();

		Node closeButton = GUI.getStage().getScene().lookup( "#closeProjectButton" );
		robot.click( closeButton );

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return GUI.getStage().getScene().lookup( ".workspace-view" ) != null;
			}
		} );
	}
}
