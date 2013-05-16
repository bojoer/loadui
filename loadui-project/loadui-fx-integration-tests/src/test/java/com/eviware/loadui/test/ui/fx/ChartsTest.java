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
package com.eviware.loadui.test.ui.fx;

import static javafx.util.Duration.seconds;
import static org.junit.Assert.assertEquals;

import java.util.Set;

import javafx.scene.Node;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.GUI;
import com.eviware.loadui.test.ui.fx.states.SimpleWebTestState;
import com.eviware.loadui.ui.fx.util.test.LoadUiRobot;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.google.common.base.Predicate;

/**
 * @author henrik.olsson
 */
@Category( IntegrationTest.class )
public class ChartsTest
{
	private static final Predicate<Node> WEB_RUNNER = new Predicate<Node>()
	{
		@Override
		public boolean apply( Node input )
		{
			if( input.getClass().getSimpleName().equals( "StatisticHolderToolboxItem" ) )
			{
				return input.toString().equals( "Web Page Runner" );
			}
			return false;
		}
	};

	private TestFX controller;
	private LoadUiRobot robot;

	@BeforeClass
	public static void enterState() throws Exception
	{
		SimpleWebTestState.STATE.enter();
	}

	@AfterClass
	public static void leaveState() throws Exception
	{
	}

	@Before
	public void setup()
	{
		controller = GUI.getController();
		robot = LoadUiRobot.usingController( controller );
	}

	@Test
	public void shouldHaveTwoLines()
	{
		robot.runTestFor( seconds( 5 ) );
		controller.click( "#statsTab" );
		controller.drag( WEB_RUNNER ).by( 150, 150 ).drop().click( "#default" );

		assertEquals( 2, allChartLines().size() );
	}

	private Set<Node> allChartLines()
	{
		return TestFX.findAll( "LineSegmentView" );
	}
}
