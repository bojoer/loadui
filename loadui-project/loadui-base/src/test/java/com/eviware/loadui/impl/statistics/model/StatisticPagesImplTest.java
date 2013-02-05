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
package com.eviware.loadui.impl.statistics.model;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.*;

import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.config.LoaduiProjectDocumentConfig;
import com.eviware.loadui.config.StatisticsConfig;

public class StatisticPagesImplTest
{
	StatisticPagesImpl statisticPages;

	@Before
	public void setup()
	{
		LoaduiProjectDocumentConfig doc = LoaduiProjectDocumentConfig.Factory.newInstance();
		StatisticsConfig config = doc.addNewLoaduiProject().addNewStatistics();
		statisticPages = new StatisticPagesImpl( config );
	}

	@Test
	public void shouldHandleMovingChildren()
	{
		StatisticPage first = statisticPages.createPage( "first" );
		StatisticPage second = statisticPages.createPage( "second" );
		StatisticPage third = statisticPages.createPage( "third" );

		assertThat( statisticPages.getChildCount(), is( 3 ) );
		assertThat( statisticPages.getChildAt( 0 ), is( first ) );
		assertThat( statisticPages.getChildAt( 1 ), is( second ) );
		assertThat( statisticPages.getChildAt( 2 ), is( third ) );

		statisticPages.movePage( second, 2 );

		assertThat( statisticPages.getChildCount(), is( 3 ) );
		assertThat( statisticPages.getChildAt( 0 ), is( first ) );
		assertThat( statisticPages.getChildAt( 1 ), is( third ) );
		assertThat( statisticPages.getChildAt( 2 ), is( second ) );

		assertThat( first.getTitle(), is( "first" ) );
		assertThat( second.getTitle(), is( "second" ) );
		assertThat( third.getTitle(), is( "third" ) );
	}

	@Test
	public void shouldHandleMovingAfterRename()
	{
		StatisticPage first = statisticPages.createPage( "first" );
		StatisticPage second = statisticPages.createPage( "second" );
		StatisticPage third = statisticPages.createPage( "third" );
		StatisticPage fourth = statisticPages.createPage( "fourth" );

		assertThat( statisticPages.getChildCount(), is( 4 ) );
		assertThat( statisticPages.getChildAt( 0 ), is( first ) );
		assertThat( statisticPages.getChildAt( 1 ), is( second ) );
		assertThat( statisticPages.getChildAt( 2 ), is( third ) );
		assertThat( statisticPages.getChildAt( 3 ), is( fourth ) );

		statisticPages.movePage( first, 1 );

		assertThat( statisticPages.getChildAt( 0 ), is( second ) );
		assertThat( statisticPages.getChildAt( 1 ), is( first ) );
		assertThat( statisticPages.getChildAt( 2 ), is( third ) );
		assertThat( statisticPages.getChildAt( 3 ), is( fourth ) );

		third.setTitle( "newThree" );

		assertThat( third.getTitle(), is( "newThree" ) );

		statisticPages.movePage( first, 3 );

		assertThat( third.getTitle(), is( "newThree" ) );
	}
}
