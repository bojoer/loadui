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
package com.eviware.loadui.impl.statistics;

import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticsWriter;
import com.eviware.loadui.api.statistics.StatisticsWriterFactory;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.events.EventSupport;

/**
 * Implementation for the StatisticsManager. Also used by the
 * StatisticsHolderSupport to access the available StatisticsWriterFactories,
 * which are imported using OSGi.
 * 
 * @author dain.nilsson
 */
public class StatisticsManagerImpl implements StatisticsManager, Releasable
{
	public static final Logger log = LoggerFactory.getLogger( StatisticsManagerImpl.class );

	private final ExecutionManager executionManager;
	private final EventSupport eventSupport = new EventSupport( this );
	private Set<StatisticHolder> holders = new HashSet<StatisticHolder>();
	private Map<String, StatisticsWriterFactory> factories = new HashMap<String, StatisticsWriterFactory>();

	private final StatisticHolderListener statisticHolderListener = new StatisticHolderListener();

	public StatisticsManagerImpl( ExecutionManager executionManager )
	{
		this.executionManager = executionManager;
	}

	@Override
	public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<? super T> listener )
	{
		eventSupport.addEventListener( type, listener );
	}

	@Override
	public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<? super T> listener )
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
	public void release()
	{
		ReleasableUtils.release( eventSupport );
	}

	@Override
	public void registerStatisticHolder( StatisticHolder statisticHolder )
	{
		if( holders.add( statisticHolder ) )
		{
			statisticHolder.addEventListener( BaseEvent.class, statisticHolderListener );
			fireEvent( new CollectionEvent( this, STATISTIC_HOLDERS, CollectionEvent.Event.ADDED, statisticHolder ) );
		}
	}

	@Override
	public void deregisterStatisticHolder( StatisticHolder statisticHolder )
	{
		if( holders.remove( statisticHolder ) )
		{
			statisticHolder.removeEventListener( BaseEvent.class, statisticHolderListener );
			fireEvent( new CollectionEvent( this, STATISTIC_HOLDERS, CollectionEvent.Event.REMOVED, statisticHolder ) );
		}
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

	@Override
	public ExecutionManager getExecutionManager()
	{
		return executionManager;
	}

	public void registerStatisticsWriterFactory( StatisticsWriterFactory factory, Map<String, String> properties )
	{
		factories.put( factory.getType(), factory );
	}

	public void unregisterStatisticsWriterFactory( StatisticsWriterFactory factory, Map<String, String> properties )
	{
		factories.remove( factory.getType() );
	}

	StatisticsWriter createStatisticsWriter( String type, StatisticVariable variable, Map<String, Object> config )
	{
		StatisticsWriterFactory factory = factories.get( type );
		if( factory != null )
			return factory.createStatisticsWriter( this, variable, config );

		throw new IllegalArgumentException( "No StatisticsWriter factory of type: " + type + " available!" );
	}

	private class StatisticHolderListener implements EventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( StatisticHolder.STATISTIC_VARIABLES.equals( event.getKey() ) )
			{
				fireEvent( new BaseEvent( event.getSource(), STATISTIC_HOLDER_UPDATED ) );
			}
		}
	}
}
