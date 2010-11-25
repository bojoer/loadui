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

import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.model.StatisticPages;
import com.eviware.loadui.config.ChartGroupConfig;
import com.eviware.loadui.config.StatisticsConfig;
import com.eviware.loadui.config.StatisticsPageConfig;
import com.eviware.loadui.impl.XmlBeansUtils;
import com.eviware.loadui.impl.model.OrderedCollectionSupport;
import com.eviware.loadui.util.events.EventSupport;

public class StatisticPagesImpl implements StatisticPages
{
	private final StatisticsConfig config;
	private final EventSupport eventSupport = new EventSupport();
	private final OrderedCollectionSupport<StatisticPage> collectionSupport;

	public StatisticPagesImpl( StatisticsConfig config )
	{
		this.config = config;

		collectionSupport = new OrderedCollectionSupport<StatisticPage>( this );

		for( StatisticsPageConfig statisticPageConfig : config.getPageArray() )
			collectionSupport.addChild( new StatisticPageImpl( this, statisticPageConfig ) );
	}

	@Override
	public Collection<StatisticPage> getChildren()
	{
		return collectionSupport.getChildren();
	}

	@Override
	public int getChildCount()
	{
		return collectionSupport.getChildCount();
	}

	@Override
	public int indexOf( StatisticPage child )
	{
		return collectionSupport.indexOf( child );
	}

	@Override
	public StatisticPage getChildAt( int index )
	{
		return collectionSupport.getChildAt( index );
	}

	@Override
	public StatisticPage createPage( String title )
	{
		StatisticsPageConfig statisticPageConfig = config.addNewPage();
		statisticPageConfig.setTitle( title );
		StatisticPageImpl statisticPage = new StatisticPageImpl( this, statisticPageConfig );
		collectionSupport.addChild( statisticPage );
		return statisticPage;
	}

	@Override
	public void movePage( StatisticPage page, int index )
	{
		StatisticsPageConfig[] pageArray = XmlBeansUtils.moveArrayElement( config.getPageArray(), indexOf( page ), index );
		config.setPageArray( pageArray );
		collectionSupport.moveChild( page, index );
		for( int i = 0; i < pageArray.length; i++ )
			( ( StatisticPageImpl )collectionSupport.getChildAt( i ) ).setConfig( pageArray[i] );
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

	void removeChild( StatisticPage child )
	{
		// TODO: Remove from XML config
		collectionSupport.removeChild( child );
	}
}