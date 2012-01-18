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
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticsWriter;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.ReleasableUtils;

/**
 * Support class for Objects implementing StatisticHolder. Handles creating and
 * listing StatisticVariables, firing events, instantiating StatisticsWriters,
 * etc.
 * 
 * @author dain.nilsson
 */
public class StatisticHolderSupport implements Releasable
{
	public static Logger log = LoggerFactory.getLogger( StatisticHolderSupport.class );

	private final StatisticsManager manager;
	private final StatisticHolder owner;
	private final AddressableRegistry addressableRegistry;
	private final HashMap<String, StatisticVariableImpl> variables = new HashMap<String, StatisticVariableImpl>();

	public StatisticHolderSupport( StatisticHolder owner )
	{
		this.owner = owner;
		manager = BeanInjector.getBean( StatisticsManager.class );
		addressableRegistry = BeanInjector.getBean( AddressableRegistry.class );
	}

	/**
	 * Initialized the StatisticHolderSupport and registers the StatisticHolder.
	 * Should be called once the StatisticHolder has been fully initialized.
	 */
	public void init()
	{
		manager.registerStatisticHolder( owner );
	}

	/**
	 * Instantiates a StatisticsWriter for the given type, and adds it to the
	 * given StatisticVariable.
	 * 
	 * @param type
	 * @param variable
	 * @return
	 */
	public void addStatisticsWriter( String type, StatisticVariable variable, Map<String, Object> config )
	{
		if( !variables.containsValue( variable ) )
			throw new IllegalArgumentException(
					"Attempt made to add a StatisticsWriter for a StatisticVariable not contained in the parent StatisticHolder." );

		// TODO: Fire CollectionEvent about Statistics exposed by the
		// StatisticsWriter.
		StatisticsWriter writer = ( ( StatisticsManagerImpl )manager ).createStatisticsWriter( type, variable, config );
		( ( StatisticVariableImpl )variable ).addStatisticsWriter( writer );
	}

	public void addStatisticsWriter( String type, StatisticVariable variable )
	{
		addStatisticsWriter( type, variable, Collections.<String, Object> emptyMap() );
	}

	/**
	 * Adds a StatisticVariable to the StatisticHolder, and returns it.
	 * 
	 * @param statisticVariableName
	 * @return
	 */
	public StatisticVariable.Mutable addStatisticVariable( String statisticVariableName, String description,
			boolean listenable )
	{
		if( variables.containsKey( statisticVariableName ) )
			return variables.get( statisticVariableName );

		StatisticVariableImpl variable = listenable ? new ListenableStatisticVariableImpl( manager.getExecutionManager(),
				owner, statisticVariableName, addressableRegistry, description ) : new StatisticVariableImpl(
				manager.getExecutionManager(), owner, statisticVariableName, addressableRegistry, description );
		variables.put( statisticVariableName, variable );
		owner.fireEvent( new CollectionEvent( owner, StatisticHolder.STATISTIC_VARIABLES, CollectionEvent.Event.ADDED,
				variable ) );

		return variable;
	}

	public StatisticVariable.Mutable addStatisticVariable( String statisticVariableName, String description )
	{
		return addStatisticVariable( statisticVariableName, description, false );
	}

	public StatisticVariable.Mutable addStatisticVariable( String statisticVariableName )
	{
		return addStatisticVariable( statisticVariableName, "NO DESCRIPTION", false );
	}

	/**
	 * Removes a StatisticVariable from the StatisticHolder.
	 * 
	 * @param statisticVariableName
	 */
	public void removeStatisticVariable( String statisticVariableName )
	{
		if( !variables.containsKey( statisticVariableName ) )
			throw new NoSuchElementException(
					"Attempt made to remove a non-existing StatisticVarible from a StatisticHolder." );

		StatisticVariableImpl removedVariable = variables.remove( statisticVariableName );
		ReleasableUtils.release( removedVariable );
		owner.fireEvent( new CollectionEvent( owner, StatisticHolder.STATISTIC_VARIABLES, CollectionEvent.Event.REMOVED,
				removedVariable ) );
		log.debug( "Fired CollectionEvent: removed statistic variable!" );
	}

	public StatisticVariable getStatisticVariable( String statisticVariableName )
	{
		return variables.get( statisticVariableName );
	}

	public Set<String> getStatisticVariableNames()
	{
		return Collections.unmodifiableSet( variables.keySet() );
	}

	/**
	 * Removes any StatisticVariables and Statistics for the StatisticHolder, and
	 * removes the StatisticHolder from the registry. Should be called when the
	 * StatisticHolder is destroyed.
	 */
	@Override
	public void release()
	{
		manager.deregisterStatisticHolder( owner );
		ReleasableUtils.releaseAll( variables.values() );
	}

}