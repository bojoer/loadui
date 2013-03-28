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

import static com.eviware.loadui.ui.fx.util.test.FXTestUtils.getOrFail;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import javafx.scene.control.Label;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.test.categories.IntegrationTest;

@Category( IntegrationTest.class )
public class LastResultOpenedStateTest
{
	@BeforeClass
	public static void enterState() throws Exception
	{
		LastResultOpenedState.STATE.enter();
	}

	@AfterClass
	public static void leaveState() throws Exception
	{
		LastResultOpenedState.STATE.getParent().enter();
	}

	@Test
	public void shouldHaveAnalysisView() throws Exception
	{
		getOrFail( ".analysis-view" );
		Label lbl = getOrFail( "#current-execution-label" );
		int year = Calendar.getInstance().get( Calendar.YEAR );
		assertTrue( lbl.getText().contains( year + "" ) ); // default label text should contain current date

	}
}
