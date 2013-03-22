package com.eviware.loadui.test.ui.fx.states;

import javafx.stage.Stage;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.GUI;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.eviware.loadui.ui.fx.util.test.FXTestUtils;

public class FXAppLoadedState extends TestState
{
	public static final TestState STATE = new FXAppLoadedState();

	private FXAppLoadedState()
	{
		super( "FX App Loaded", TestState.ROOT );
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		GUI.getBundleContext();
		final Stage dialog = TestFX.findStageByTitle( "Welcome to LoadUI" );
		FXTestUtils.invokeAndWait( new Runnable()
		{
			@Override
			public void run()
			{
				log.debug( "Closing 'Getting Started' dialog" );
				dialog.close();
			}
		}, 1 );

		GUI.getController().click( "#mainButton" ).click( "#mainButton" ).sleep( 500 );
	}

	@Override
	protected void exitToParent() throws Exception
	{
	}
}
