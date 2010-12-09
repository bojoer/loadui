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
package com.eviware.loadui.impl.statistics.model.chart;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.model.OrderedCollection;
import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.ChartViewProvider;

/**
 * Abstract base implementation of a ChartViewProvider. Subclasses need to
 * override methods to create ChartViews for the ChartGroup, Charts and sources.
 * 
 * @author dain.nilsson
 * 
 * @param <ChartViewType>
 */
public abstract class AbstractChartViewProvider<ChartViewType extends ChartView> implements
		ChartViewProvider<ChartViewType>
{
	private final Map<Chart, ChartViewType> chartChartViews = new HashMap<Chart, ChartViewType>();
	private final Map<String, ChartViewType> sourceChartViews = new HashMap<String, ChartViewType>();
	private final ChartGroupListener listener = new ChartGroupListener();
	protected final ChartGroup chartGroup;
	private ChartViewType groupChartView;

	public AbstractChartViewProvider( ChartGroup chartGroup )
	{
		this.chartGroup = chartGroup;

		chartGroup.addEventListener( CollectionEvent.class, listener );
	}

	protected void init()
	{
		groupChartView = buildChartViewForGroup( chartGroup );

		for( Chart chart : chartGroup.getChildren() )
			chartChartViews.put( chart, buildChartViewForChart( chart ) );

		for( String source : chartGroup.getSources() )
			sourceChartViews.put( source, buildChartViewForSource( source ) );
	}

	/**
	 * Create a ChartView for the ChartGroup.
	 * 
	 * @param chartGroup
	 * @return
	 */
	protected abstract ChartViewType buildChartViewForGroup( ChartGroup chartGroup );

	/**
	 * Create a ChartView for a Chart.
	 * 
	 * @param chart
	 * @return
	 */
	protected abstract ChartViewType buildChartViewForChart( Chart chart );

	/**
	 * Create a ChartView for a source.
	 * 
	 * @param source
	 * @return
	 */
	protected abstract ChartViewType buildChartViewForSource( String source );

	@Override
	public ChartViewType getChartViewForChartGroup()
	{
		return groupChartView;
	}

	@Override
	public ChartViewType getChartViewForChart( Chart chart )
	{
		return chartChartViews.get( chart );
	}

	@Override
	public ChartViewType getChartViewForSource( String source )
	{
		return sourceChartViews.get( source );
	}

	@Override
	public Collection<ChartViewType> getChartViewsForCharts()
	{
		return Collections.unmodifiableCollection( chartChartViews.values() );
	}

	@Override
	public Collection<ChartViewType> getChartViewsForSources()
	{
		return Collections.unmodifiableCollection( sourceChartViews.values() );
	}

	@Override
	public void release()
	{
		chartGroup.removeEventListener( CollectionEvent.class, listener );

		for( ChartViewType chartView : chartChartViews.values() )
			if( chartView instanceof Releasable )
				( ( Releasable )chartView ).release();
		for( ChartViewType chartView : sourceChartViews.values() )
			if( chartView instanceof Releasable )
				( ( Releasable )chartView ).release();

		if( groupChartView instanceof Releasable )
			( ( Releasable )groupChartView ).release();

		chartChartViews.clear();
		sourceChartViews.clear();
	}

	private class ChartGroupListener implements EventHandler<CollectionEvent>
	{
		@Override
		public void handleEvent( CollectionEvent event )
		{
			if( OrderedCollection.CHILDREN.equals( event.getKey() ) )
			{
				Chart chart = ( Chart )event.getElement();
				if( CollectionEvent.Event.ADDED.equals( event.getEvent() ) )
				{
					chartChartViews.put( chart, buildChartViewForChart( chart ) );
				}
				else
				{
					chartChartViews.remove( chart );
				}
			}
		}
	}
}