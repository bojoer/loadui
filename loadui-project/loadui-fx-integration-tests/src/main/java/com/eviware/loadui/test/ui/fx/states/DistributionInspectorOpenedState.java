package com.eviware.loadui.test.ui.fx.states;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.GUI;

public class DistributionInspectorOpenedState extends TestState
{
	public static final DistributionInspectorOpenedState STATE = new DistributionInspectorOpenedState();

	private DistributionInspectorOpenedState()
	{
		super( "Distribution inpector opened", TwoScenariosCreatedState.STATE );
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		log.debug( "Opening Distribution inspecor" );
		GUI.getController().click( "#Distribution" );
	}

	@Override
	protected void exitToParent() throws Exception
	{
		log.debug( "Closing Distribution inspector" );

		GUI.getController().click( "#Distribution" ).click( "#Distribution" );
	}

}
