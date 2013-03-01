package com.eviware.loadui.test.ui.fx.states;

import java.util.Set;

import javafx.scene.Node;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.GUI;
import com.eviware.loadui.ui.fx.util.test.TestFX;

public class LastResultOpenedState extends TestState
{
	public static final LastResultOpenedState STATE = new LastResultOpenedState();

	private LastResultOpenedState()
	{
		super( "Last Result Opened", ProjectLoadedWithoutAgentsState.STATE );
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		GUI.getController().click( ".project-playback-panel .play-button" ).sleep( 500 )
				.click( ".project-playback-panel .play-button" ).sleep( 1000 ).click( "#statsTab" )
				.click( "#open-execution" ).doubleClick( "#result-0" );
	}

	@Override
	protected void exitToParent() throws Exception
	{
		Set<Node> resultViewSet = TestFX.findAll( ".result-view" );
		if( !resultViewSet.isEmpty() )
		{
			GUI.getController().closeCurrentWindow();
		}
	}
}
