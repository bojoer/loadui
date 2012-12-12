package com.eviware.loadui.test.ui.fx.states;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.GUI;

public class LastResultOpenedState extends TestState
{
	public static final LastResultOpenedState STATE = new LastResultOpenedState();

	private LastResultOpenedState()
	{
		super( "Last Result Opened", ProjectLoadedState.STATE );
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		GUI.getController().click( ".project-playback-panel #play-button" ).sleep( 5000 )
				.click( ".project-playback-panel #play-button" ).click( "#resultTab" ).click( "#result-0" );
	}

	@Override
	protected void exitToParent() throws Exception
	{
		GUI.getController().click( "#close-analysis-view" );
	}

}
