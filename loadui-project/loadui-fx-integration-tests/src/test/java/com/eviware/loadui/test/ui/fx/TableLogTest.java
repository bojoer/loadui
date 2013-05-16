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
package com.eviware.loadui.test.ui.fx;

import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.FIXED_RATE_GENERATOR;
import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.TABLE_LOG;
import static com.eviware.loadui.ui.fx.util.test.TestFX.findAll;
import static javafx.util.Duration.seconds;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import javafx.scene.Node;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.eviware.loadui.ui.fx.util.test.ComponentHandle;
import com.eviware.loadui.ui.fx.util.test.LoadUiRobot;
import com.eviware.loadui.ui.fx.util.test.TestFX;

/**
 * @author henrik.olsson
 */
@Category( IntegrationTest.class )
public class TableLogTest
{
	TestFX controller;
	LoadUiRobot robot;

	@BeforeClass
	public static void enterState() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.enter();
	}

	@AfterClass
	public static void leaveState() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.getParent().enter();
	}

	@Before
	public void setup()
	{
		controller = GUI.getController();
		robot = LoadUiRobot.usingController( controller );
	}

	@Test
	public void shouldHaveRows() throws Exception
	{
		ComponentHandle fixedRate = robot.createComponent( FIXED_RATE_GENERATOR );
		ComponentHandle tableLog = robot.createComponent( TABLE_LOG );

		fixedRate.connectTo( tableLog );

		assertTrue( numberOfTableRows().isEmpty() );

		robot.runTestFor( seconds( 3 ) );

		assertFalse( numberOfTableRows().isEmpty() );
	}

	private Set<Node> numberOfTableRows()
	{
		return findAll( ".table-row-cell" );
	}
}
