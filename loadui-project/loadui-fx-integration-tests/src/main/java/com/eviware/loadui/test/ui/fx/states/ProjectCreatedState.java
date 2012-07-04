package com.eviware.loadui.test.ui.fx.states;

import static com.eviware.loadui.ui.fx.util.test.ControllerApi.find;

import java.util.concurrent.Callable;

import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.GUI;
import com.eviware.loadui.util.test.TestUtils;
import com.google.common.collect.Iterables;

public class ProjectCreatedState extends TestState
{
	public static final TestState STATE = new ProjectCreatedState();

	private ProjectCreatedState()
	{
		super( "Project Created", FXAppLoadedState.STATE );
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		final ListView<Node> projectList = find( "#projectRefNodeList" );
		GUI.getController().click( projectList, MouseButton.SECONDARY ).moveBy( 15, 10 ).click();

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
		final ListView<Node> projectList = find( "#projectRefNodeList" );
		Node projectRef = Iterables.getOnlyElement( projectList.getItems() );

		GUI.getController().click( find( ".button", projectRef ) );

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
