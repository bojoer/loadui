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
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.CanvasObjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;
import com.eviware.loadui.util.CacheMap;

/**
 * Implementation of a StatisticVariable.
 * 
 * @author dain.nilsson
 */
public class StatisticVariableImpl implements StatisticVariable
{
	private final ExecutionManager manager;
	private final String name;
	private final StatisticHolder parent;
	private final Set<TrackDescriptor> descriptors = new HashSet<TrackDescriptor>();
	private final Set<String> statisticNames = new HashSet<String>();
	private final CacheMap<String, StatisticImpl<?>> statisticCache = new CacheMap<String, StatisticImpl<?>>();

	public StatisticVariableImpl( ExecutionManager executionManager, StatisticHolder parent, String name )
	{
		this.manager = executionManager;
		this.name = name;
		this.parent = parent;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public StatisticHolder getStatisticHolder()
	{
		return parent;
	}

	public void addTrackDescriptor( TrackDescriptor trackDescriptor )
	{
		if( descriptors.add( trackDescriptor ) )
			statisticNames.addAll( trackDescriptor.getValueNames().keySet() );
	}

	@Override
	public Collection<String> getSources()
	{
		Set<String> sources = new HashSet<String>();
		sources.add( "local" );

		// Add labels of assigned agents.
		// TODO: Share this information per SceneItem instead of recomputing it
		// each time.
		if( "controller".equals( System.getProperty( "loadui.instance" ) )
				&& getStatisticHolder() instanceof CanvasObjectItem )
		{
			CanvasItem canvas = ( ( CanvasObjectItem )getStatisticHolder() ).getCanvas();
			if( canvas instanceof SceneItem )
				for( AgentItem agent : canvas.getProject().getAgentsAssignedTo( ( SceneItem )canvas ) )
					sources.add( agent.getLabel() );
		}

		return sources;
	}

	@Override
	public Collection<String> getStatisticNames()
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
						throw new NullPointerException();
					}
				} );
	}
}
