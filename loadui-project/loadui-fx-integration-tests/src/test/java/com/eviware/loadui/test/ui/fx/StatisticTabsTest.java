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

import static com.eviware.loadui.ui.fx.util.test.FXTestUtils.failIfExists;
import static com.eviware.loadui.ui.fx.util.test.FXTestUtils.getOrFail;
import static org.junit.Assert.assertEquals;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.api.statistics.model.StatisticPages;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.LastResultOpenedState;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.eviware.loadui.ui.fx.util.test.TestFX;

@Category( IntegrationTest.class )
public class StatisticTabsTest
{
	private static TestFX controller;

	@BeforeClass
	public static void enterState() throws Exception
	{
		LastResultOpenedState.STATE.enter();
		controller = GUI.getController();
	}

	@AfterClass
	public static void leaveState() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.getParent().enter();
	}

	@Test
	public void testTabs() throws Exception
	{
		StatisticPages pages = ProjectLoadedWithoutAgentsState.STATE.getProject().getStatisticPages();
		assertEquals( 1, pages.getChildCount() );

		controller.click( "#plus-button" ).click( "#untitled-page-2" ).click( "#untitled-page-1" ).click( "#plus-button" );
		assertEquals( 3, pages.getChildCount() );

		controller.click( "#untitled-page-2" ).click( "#untitled-page-2 .tab-close-button" ).click( "#plus-button" )
				.click( "#untitled-page-1" ).click( "#untitled-page-1 .tab-close-button" );
		assertEquals( 2, pages.getChildCount() );
		assertEquals( "Untitled Page 3", pages.getChildAt( 0 ).getLabel() );
		assertEquals( "Untitled Page 4", pages.getChildAt( 1 ).getLabel() );

		// the tests below are placed here to avoid having to find out the ID of the tabs currently being shown,
		// which could change depending on which tests have run first

		// test tab can be renamed
		controller.click( "#untitled-page-3", MouseButton.SECONDARY ).click( "#tab-rename" ).type( "tabnewname" )
				.type( KeyCode.ENTER ).sleep( 250 );

		// tab ID cannot be changed
		Node tabPaneHeaderSkin = getOrFail( "#untitled-page-3" );
		Label label = ( Label )tabPaneHeaderSkin.lookup( "Label" );
		assertEquals( "tabnewname", label.getText() );
		assertEquals( "tabnewname", pages.getChildAt( 0 ).getLabel() );

		// test tab can be closed through the menu
		controller.click( "#untitled-page-4", MouseButton.SECONDARY ).click( "#tab-delete" ).sleep( 500 );
		failIfExists( "#untitled-page-4" );
		getOrFail("#untitled-page-3");
		assertEquals( 1, pages.getChildCount() );
		assertEquals( "tabnewname", pages.getChildAt( 0 ).getLabel() );

	}

}
