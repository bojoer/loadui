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
package com.eviware.loadui.impl.statistics.model.chart.line;

import java.util.Collections;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.line.Segment;
import com.eviware.loadui.impl.statistics.model.chart.AbstractChartViewProvider;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.events.EventSupport;

/**
 * ChartViewProvider for LineChartViews.
 * 
 * @author dain.nilsson
 */
public class LineChartViewProvider extends AbstractChartViewProvider<LineChartView> implements EventFirer
{
	public static final String LINE_SEGMENTS = LineChartViewProvider.class.getName() + "@lineSegments";

	private final EventSupport eventSupport = new EventSupport( this );
	private final Set<Segment> segments = new HashSet<>();

	public LineChartViewProvider( ChartGroup chartGroup )
	{
		super( chartGroup );

		init();
	}

	@Override
	protected LineChartView buildChartViewForGroup( ChartGroup chartGroup )
	{
		return new ChartGroupLineChartView( this, chartGroup );
	}

	@Override
	protected LineChartView buildChartViewForChart( Chart chart )
	{
		return new ChartLineChartView( this, chart );
	}

	@Override
	protected LineChartView buildChartViewForSource( String source )
	{
		return new SourceLineChartView( this, chartGroupOwner, source );
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
		super.release();
		ReleasableUtils.release( eventSupport );
	}

	void fireSegmentAdded( Segment segment )
	{
		segments.add( segment );
		fireEvent( new CollectionEvent( this, LINE_SEGMENTS, CollectionEvent.Event.ADDED, segment ) );
	}

	void fireSegmentRemoved( Segment segment )
	{
		segments.remove( segment );
		fireEvent( new CollectionEvent( this, LINE_SEGMENTS, CollectionEvent.Event.REMOVED, segment ) );
	}

	Set<Segment> getSegments()
	{
		return Collections.unmodifiableSet( segments );
	}
}