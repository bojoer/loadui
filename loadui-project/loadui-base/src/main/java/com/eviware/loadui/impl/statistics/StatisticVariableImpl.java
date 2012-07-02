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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.addressable.AddressableRegistry.DuplicateAddressException;
import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.CanvasObjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsWriter;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.CacheMap;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.statistics.StatisticImpl;

/**
 * Implementation of a StatisticVariable.
 * 
 * @author dain.nilsson
 */
public class StatisticVariableImpl implements StatisticVariable.Mutable, Releasable
{
	private final Logger log = LoggerFactory.getLogger( StatisticVariableImpl.class );

	private final ExecutionManager manager;
	private final AddressableRegistry addressableRegistry;
	private final String name;
	private final StatisticHolder parent;
	private final Set<StatisticsWriter> writers = new HashSet<>();
	private final Set<TrackDescriptor> descriptors = new HashSet<>();
	private final Set<String> statisticNames = new HashSet<>();
	private final CacheMap<String, StatisticImpl<?>> statisticCache = new CacheMap<>();
	private final ActionListener actionListener = new ActionListener();

	private String description;

	public StatisticVariableImpl( ExecutionManager executionManager, StatisticHolder parent, String name,
			AddressableRegistry addressableRegistry, String description )
	{
		this.manager = executionManager;
		this.addressableRegistry = addressableRegistry;
		this.name = name;
		this.parent = parent;
		this.description = description;

		parent.addEventListener( ActionEvent.class, actionListener );
	}

	@Override
	public String getLabel()
	{
		return name;
	}

	@Override
	public StatisticHolder getStatisticHolder()
	{
		return parent;
	}

	public void addStatisticsWriter( @Nonnull StatisticsWriter writer )
	{
		if( writers.add( writer ) )
		{
			TrackDescriptor descriptor = writer.getTrackDescriptor();
			descriptors.add( descriptor );
			statisticNames.addAll( descriptor.getValueNames().keySet() );
			try
			{
				addressableRegistry.register( writer );
			}
			catch( DuplicateAddressException e )
			{
				log.error( "Duplicate address detected:", e );
			}
		}
	}

	@Override
	public Set<String> getSources()
	{
		Set<String> sources = new HashSet<>();
		sources.add( MAIN_SOURCE );

		// Add labels of assigned agents.
		// TODO: Share this information per SceneItem instead of recomputing it
		// each time.
		StatisticHolder statisticHolder = getStatisticHolder();
		if( LoadUI.isController() && statisticHolder instanceof CanvasObjectItem )
		{
			CanvasItem canvas = ( statisticHolder instanceof SceneItem ) ? ( SceneItem )statisticHolder
					: ( ( CanvasObjectItem )statisticHolder ).getCanvas();
			if( canvas instanceof SceneItem )
			{
				// TODO: Is there a better way to do this? How can I add a Statistic
				// for an agent that I used in an earlier run but am not using now?
				// for( AgentItem agent : canvas.getProject().getAgentsAssignedTo( (
				// SceneItem )canvas ) )
				for( AgentItem agent : canvas.getProject().getWorkspace().getAgents() )
					sources.add( agent.getLabel() );
			}
		}

		return sources;
	}

	@Override
	public Set<String> getStatisticNames()
	{
		return Collections.unmodifiableSet( statisticNames );
	}

	@Override
	public Statistic<?> getStatistic( final String statisticName, final String source )
	{
		return statisticCache.getOrCreate( statisticName.length() + ":" + statisticName + source,
				new Callable<StatisticImpl<?>>()
				{
					@Override
					@SuppressWarnings( { "unchecked", "rawtypes" } )
					public StatisticImpl<?> call() throws Exception
					{
						for( TrackDescriptor descriptor : descriptors )
							for( Entry<String, Class<? extends Number>> entry : descriptor.getValueNames().entrySet() )
								if( statisticName.equals( entry.getKey() ) )
									return new StatisticImpl( manager, descriptor.getId(), StatisticVariableImpl.this,
											statisticName, source, entry.getValue() );
						throw new IllegalArgumentException();
					}
				} );
	}

	@Override
	public void update( long timestamp, Number value )
	{
		for( StatisticsWriter writer : writers )
		{
			writer.update( timestamp, value );
		}
	}

	@Override
	public void release()
	{
		parent.removeEventListener( ActionEvent.class, actionListener );

		for( StatisticsWriter writer : writers )
			addressableRegistry.unregister( writer );
		ReleasableUtils.releaseAll( writers );
		writers.clear();
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public String getDescriptionForStatistic( String statisticName )
	{
		for( StatisticsWriter writer : writers )
		{
			String metricDescription = writer.getDescriptionForMetric( statisticName );
			if( metricDescription != null )
			{
				if( metricDescription.contains( "%v" ) )
					return metricDescription.replace( "%v", getDescription() );
				else
					return metricDescription + " of " + getDescription();
			}
		}

		return getDescription();

	}

	@Override
	public void setDescription( String description )
	{
		this.description = description;
	}

	@Override
	public String toString()
	{
		return getLabel();
	}

	private class ActionListener implements WeakEventHandler<ActionEvent>
	{
		@Override
		public void handleEvent( ActionEvent event )
		{
			if( CounterHolder.COUNTER_RESET_ACTION.equals( event.getKey() ) )
			{
				for( StatisticsWriter writer : writers )
					writer.reset();
			}
		}
	}
}
