/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.impl.statistics;

import java.util.Collection;
import java.util.EventObject;

import org.junit.*;

import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class StatisticsTest
{
	StatisticHolder holderMock;
	StatisticHolderSupport holderSupport;

	@Before
	public void setup()
	{
		holderMock = mock( StatisticHolder.class );
		holderSupport = new StatisticHolderSupport( holderMock );
	}

	@Test
	public void canAddStatisticVariables()
	{
		String statisticVariableName = "My Statistic";
		StatisticVariable variable = holderSupport.addStatisticVariable( statisticVariableName );

		verify( holderMock ).fireEvent( ( EventObject )any() );

		assertThat( holderSupport.getStatisticVariableNames().contains( statisticVariableName ), is( true ) );
		assertThat( holderSupport.getStatisticVariable( statisticVariableName ), is( variable ) );
	}
}