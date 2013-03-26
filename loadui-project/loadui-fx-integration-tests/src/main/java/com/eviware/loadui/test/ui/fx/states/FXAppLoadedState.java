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
