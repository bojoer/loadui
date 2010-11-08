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
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticsWriter;
import com.eviware.loadui.api.statistics.StatisticsWriterFactory;
import com.eviware.loadui.util.events.EventSupport;

/**
 * Implementation for the StatisticsManager.
 * 
 * @author dain.nilsson
 */
public class StatisticsManagerImpl implements StatisticsManager
{
	private static StatisticsManagerImpl instance;

	private final EventSupport eventSupport = new EventSupport();
	private Set<StatisticHolder> holders = new HashSet<StatisticHolder>();
	private Map<String, StatisticsWriterFactory> factories = new HashMap<String, StatisticsWriterFactory>();

	static StatisticsManagerImpl getInstance()
	{
		return instance;
	}

	public StatisticsManagerImpl()
	{
		instance = this;
	}

	@Override
	public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<T> listener )
	{
		eventSupport.addEventListener( type, listener );
	}

	@Override
	public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<T> listener )
	{
		eventSupport.removeEventListener( type, listener );
	}

	@Override
	public void clearEventListeners()
	{
		eventSupport.clearEventListeners();
	}

	@Override
	public void fireEvent( EventObject event )
	{
		eventSupport.fireEvent( event );
	}

	@Override
	public void registerStatisticHolder( StatisticHolder statisticHolder )
	{
		if( holders.add( statisticHolder ) )
			fireEvent( new CollectionEvent( this, STATISTIC_HOLDERS, CollectionEvent.Event.ADDED, statisticHolder ) );
	}

	@Override
	public void deregisterStatisticHolder( StatisticHolder statisticHolder )
	{
		if( holders.remove( statisticHolder ) )
			fireEvent( new CollectionEvent( this, STATISTIC_HOLDERS, CollectionEvent.Event.REMOVED, statisticHolder ) );
	}

	@Override
	public Collection<StatisticHolder> getStatisticHolders()
	{
		return Collections.unmodifiableSet( holders );
	}

	@Override
	public long getMinimumWriteDelay()
	{
		return 1000;
	}

	public void registerStatisticsWriterFactory( StatisticsWriterFactory factory, Map<String, String> properties )
	{
		factories.put( factory.getType(), factory );
	}

	public void unregisterStatisticsWriterFactory( StatisticsWriterFactory factory, Map<String, String> properties )
	{
		factories.remove( factory.getType() );
	}

	public StatisticsWriter createStatisticsWriter( String type, StatisticVariable variable )
	{
		StatisticsWriterFactory factory = factories.get( type );
		if( factory != null )
			return factory.createStatisticsWriter( this, variable );

		return null;
	}
}