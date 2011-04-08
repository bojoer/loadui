/*
 * Copyright 2011 eviware software ab
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

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.config.ChartGroupConfig;
import com.eviware.loadui.config.StatisticsPageConfig;
import com.eviware.loadui.impl.XmlBeansUtils;
import com.eviware.loadui.impl.model.OrderedCollectionSupport;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.events.EventSupport;

public class StatisticPageImpl implements StatisticPage
{
	private final StatisticPagesImpl parent;
	private StatisticsPageConfig config;
	private final OrderedCollectionSupport<ChartGroup> collectionSupport;
	private final EventSupport eventSupport = new EventSupport();

	public StatisticPageImpl( StatisticPagesImpl parent, StatisticsPageConfig config )
	{
		this.parent = parent;
		this.config = config;

		collectionSupport = new OrderedCollectionSupport<ChartGroup>( this );

		for( ChartGroupConfig chartGroupConfig : config.getChartGroupArray() )
			collectionSupport.addChild( new ChartGroupImpl( this, chartGroupConfig ) );
	}

	@Override
	public Collection<ChartGroup> getChildren()
	{
		return collectionSupport.getChildren();
	}

	@Override
	public int getChildCount()
	{
		return collectionSupport.getChildCount();
	}

	@Override
	public int indexOf( ChartGroup child )
	{
		return collectionSupport.indexOf( child );
	}

	@Override
	public ChartGroup getChildAt( int index )
	{
		return collectionSupport.getChildAt( index );
	}

	@Override
	public void moveChartGroup( ChartGroup chartGroup, int index )
	{
		ChartGroupConfig[] chartGroupArray = XmlBeansUtils.moveArrayElement( config.getChartGroupArray(),
				indexOf( chartGroup ), index );
		config.setChartGroupArray( chartGroupArray );
		collectionSupport.moveChild( chartGroup, index );
		for( int i = 0; i < chartGroupArray.length; i++ )
			( ( ChartGroupImpl )collectionSupport.getChildAt( i ) ).setConfig( config.getChartGroupArray( i ) );
	}

	@Override
	public String getTitle()
	{
		return config.isSetTitle() ? config.getTitle() : "";
	}

	@Override
	public void setTitle( String title )
	{
		config.setTitle( title );
	}

	@Override
	public ChartGroup createChartGroup( String type, String title )
	{
		ChartGroupConfig chartGroupConfig = config.addNewChartGroup();
		chartGroupConfig.setType( type );
		chartGroupConfig.setTitle( title );
		ChartGroupImpl chartGroup = new ChartGroupImpl( this, chartGroupConfig );
		collectionSupport.addChild( chartGroup );
		return chartGroup;
	}

	@Override
	public void delete()
	{
		parent.removeChild( this );
		release();
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

	void removeChild( ChartGroup child )
	{
		int index = collectionSupport.indexOf( child );
		if( index >= 0 )
		{
			collectionSupport.removeChild( child );
			config.removeChartGroup( index );
		}
	}

	void setConfig( StatisticsPageConfig statisticsPageConfig )
	{
		config = statisticsPageConfig;
		for( int i = 0; i < getChildCount(); i++ )
			( ( ChartGroupImpl )getChildAt( i ) ).setConfig( config.getChartGroupArray( i ) );
	}

	@Override
	public void release()
	{
		fireEvent( new BaseEvent( this, RELEASED ) );
		ReleasableUtils.releaseAll( collectionSupport, eventSupport );
	}
}