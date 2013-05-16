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

import static com.eviware.loadui.ui.fx.util.test.TestFX.findAll;

import java.util.concurrent.Callable;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.GUI;
import com.eviware.loadui.ui.fx.util.test.ComponentHandle;
import com.eviware.loadui.ui.fx.util.test.LoadUiRobot;
import com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.eviware.loadui.util.test.TestUtils;

public class SimpleWebTestState extends TestState
{
	public static final SimpleWebTestState STATE = new SimpleWebTestState();

	private SimpleWebTestState()
	{
		this( "Simple Web Test", ProjectLoadedWithoutAgentsState.STATE );
	}

	protected SimpleWebTestState( String name, TestState parent )
	{
		super( name, parent );
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		TestFX controller = GUI.getController();
		LoadUiRobot robot = LoadUiRobot.usingController( controller );

		ComponentHandle webRunner = robot.createComponent( Component.WEB_PAGE_RUNNER );
		controller.click( ".component-view .text-field" ).type( "www.google.com" );

		ComponentHandle fixedRate = robot.createComponent( Component.FIXED_RATE_GENERATOR );

		fixedRate.connectTo( webRunner );
	}

	@Override
	protected void exitToParent() throws Exception
	{
		GUI.getController().click( "#closeProjectButton" );
		//If there is a save dialog, do not save:
		try
		{
			GUI.getController().click( "#no" ).target( TestFX.getWindowByIndex( 0 ) );
		}
		catch( Exception e )
		{
			//Ignore
		}

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return !findAll( ".workspace-view" ).isEmpty();
			}
		} );
	}
}
