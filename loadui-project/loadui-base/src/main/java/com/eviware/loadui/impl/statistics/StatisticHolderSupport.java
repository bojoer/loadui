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

import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsWriter;

/**
 * Support class for Objects implementing StatisticHolder. Handles creating and
 * listing StatisticVariables, firing events, instantiating StatisticsWriters,
 * etc.
 * 
 * @author dain.nilsson
 */
public class StatisticHolderSupport implements StatisticHolder
{
	private final EventFirer owner;

	public StatisticHolderSupport( EventFirer owner )
	{
		this.owner = owner;
	}

	/**
	 * Instantiates a StatisticsWriter for the given type, and adds it to the
	 * given StatisticVariable.
	 * 
	 * @param type
	 * @param variable
	 * @return
	 */
	public StatisticsWriter getStatisticsWriter( String type, StatisticVariable variable )
	{
		// TODO: Lookup factory using registry, instantiate StatisticsWriter.
		// Fire CollectionEvent about Statistics exposed by the StatisticsWriter.
		return null;
	}

	public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<T> listener )
	{
		owner.addEventListener( type, listener );
	}

	public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<T> listener )
	{
		owner.removeEventListener( type, listener );
	}

	public void clearEventListeners()
	{
		owner.clearEventListeners();
	}

	public void fireEvent( EventObject event )
	{
		owner.fireEvent( event );
	}

	@Override
	public StatisticVariable getStatisticVariable( String statisticVariableName )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getStatisticVariableNames()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addStatisticVariable( StatisticVariable statisticVariable )
	{
		// TODO Auto-generated method stub

	}
}