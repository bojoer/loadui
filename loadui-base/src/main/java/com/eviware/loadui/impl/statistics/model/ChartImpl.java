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
package com.eviware.loadui.impl.statistics.model;

import java.util.Collection;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.config.ChartConfig;
import com.eviware.loadui.impl.property.AttributeHolderSupport;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.events.EventSupport;

public class ChartImpl implements Chart
{
	private final ChartGroupImpl parent;
	private ChartConfig config;
	private AttributeHolderSupport attributeHolderSupport;
	private final EventSupport eventSupport = new EventSupport();
	private final StatisticHolder statisticHolder;

	public ChartImpl( ChartGroupImpl parent, ChartConfig config )
	{
		this.parent = parent;
		this.config = config;
		if( !config.isSetStatisticHolder() )
			throw new IllegalArgumentException( "No StatisticHolder defined in Chart config!" );

		statisticHolder = ( StatisticHolder )BeanInjector.getBean( AddressableRegistry.class ).lookup(
				config.getStatisticHolder() );

		if( statisticHolder == null )
			throw new IllegalArgumentException( "StatisticHolder for Chart doesn't exist!" );
		statisticHolder.addEventListener( CollectionEvent.class, new StatisticHolderListener() );

		if( config.getAttributes() == null )
			config.addNewAttributes();
		attributeHolderSupport = new AttributeHolderSupport( config.getAttributes() );
	}

	@Override
	public ChartGroup getChartGroup()
	{
		return parent;
	}

	@Override
	public StatisticHolder getStatisticHolder()
	{
		return statisticHolder;
	}

	@Override
	public void delete()
	{
		parent.removeChild( this );
		release();
	}

	@Override
	public void release()
	{
		fireEvent( new BaseEvent( this, RELEASED ) );
		ReleasableUtils.release( eventSupport );
	}

	@Override
	public String getAttribute( String key, String defaultValue )
	{
		return attributeHolderSupport.getAttribute( key, defaultValue );
	}

	@Override
	public void setAttribute( String key, String value )
	{
		attributeHolderSupport.setAttribute( key, value );
	}

	@Override
	public void removeAttribute( String key )
	{
		attributeHolderSupport.removeAttribute( key );
	}

	@Override
	public Collection<String> getAttributes()
	{
		return attributeHolderSupport.getAttributes();
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

	void setConfig( ChartConfig chartConfig )
	{
		config = chartConfig;
		attributeHolderSupport = new AttributeHolderSupport( config.getAttributes() );
	}

	Set<String> getSources()
	{
		Set<String> sources = new HashSet<String>();
		for( String name : statisticHolder.getStatisticVariableNames() )
			sources.addAll( statisticHolder.getStatisticVariable( name ).getSources() );

		return sources;
	}

	private class StatisticHolderListener implements EventHandler<CollectionEvent>
	{
		@Override
		public void handleEvent( CollectionEvent event )
		{
			if( CollectionEvent.Event.REMOVED == event.getEvent() && event.getElement() == statisticHolder )
				delete();
		}
	}
}