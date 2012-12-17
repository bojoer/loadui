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

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.traits.Deletable;
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
	private final EventSupport eventSupport = new EventSupport( this );
	@SuppressWarnings( "unused" )
	private final DeleteListener releaseListener;
	private final Owner owner;

	public ChartImpl( ChartGroupImpl parent, ChartConfig config )
	{
		this.parent = parent;
		this.config = config;
		if( !config.isSetStatisticHolder() )
			throw new IllegalArgumentException( "No StatisticHolder defined in Chart config!" );

		owner = ( Owner )BeanInjector.getBean( AddressableRegistry.class ).lookup( config.getStatisticHolder() );

		if( owner == null )
			throw new IllegalArgumentException( "Chart.Owner for Chart doesn't exist!" );

		if( config.getAttributes() == null )
			config.addNewAttributes();
		attributeHolderSupport = new AttributeHolderSupport( config.getAttributes() );

		if( owner instanceof EventFirer )
		{
			( ( EventFirer )owner ).addEventListener( BaseEvent.class, releaseListener = new DeleteListener() );
		}
		else
		{
			releaseListener = null;
		}
	}

	@Override
	public ChartGroup getChartGroup()
	{
		return parent;
	}

	@Override
	public Owner getOwner()
	{
		return owner;
	}

	@Override
	public void delete()
	{
		for( String attr : new ArrayList<>( getAttributes() ) )
			removeAttribute( attr );
		release();
		parent.removeChild( this );
	}

	@Override
	public void release()
	{
		fireEvent( new BaseEvent( this, RELEASED ) );
		ReleasableUtils.releaseAll( eventSupport, attributeHolderSupport );
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
	public final <T extends EventObject> void addEventListener( Class<T> type, EventHandler<? super T> listener )
	{
		eventSupport.addEventListener( type, listener );
	}

	@Override
	public final <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<? super T> listener )
	{
		eventSupport.removeEventListener( type, listener );
	}

	@Override
	public void clearEventListeners()
	{
		eventSupport.clearEventListeners();
	}

	@Override
	public final void fireEvent( EventObject event )
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
		Set<String> sources = new HashSet<>();
		if( owner instanceof StatisticHolder )
		{
			for( String name : ( ( StatisticHolder )owner ).getStatisticVariableNames() )
				sources.addAll( ( ( StatisticHolder )owner ).getStatisticVariable( name ).getSources() );
		}

		return sources;
	}

	private class DeleteListener implements WeakEventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( Deletable.DELETED.equals( event.getKey() ) )
			{
				delete();
			}
		}
	}
}