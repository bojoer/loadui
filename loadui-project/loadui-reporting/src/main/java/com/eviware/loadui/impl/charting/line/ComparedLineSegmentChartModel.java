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

package com.eviware.loadui.impl.charting.line;

import java.awt.Color;
import java.beans.PropertyChangeEvent;

import com.eviware.loadui.api.charting.line.LineSegmentModel;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.impl.charting.line.ScaledPointScale.ScaledChartPoint;
import com.jidesoft.chart.model.ChartModelListener;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.style.ChartStyle;
import com.jidesoft.chart.util.ColorFactory;

public class ComparedLineSegmentChartModel extends DefaultChartModel
{
	private final LineSegmentChartModel baseModel;
	private final ChartStyle chartStyle;
	private final BaseModelListener baseModelListener = new BaseModelListener();
	private final ChartGroupListener chartGroupListener = new ChartGroupListener();

	private Execution execution;

	public ComparedLineSegmentChartModel( LineSegmentChartModel baseModel )
	{
		this.baseModel = baseModel;
		chartStyle = new ChartStyle( baseModel.getChartStyle() );
		baseModel.addChartModelListener( baseModelListener );
		baseModel.getChartGroup().addEventListener( PropertyChangeEvent.class, chartGroupListener );
	}

	public ChartStyle getChartStyle()
	{
		return chartStyle;
	}

	public void setExecution( Execution execution )
	{
		if( this.execution != execution )
		{
			this.execution = execution;
			refresh();
		}
	}

	private void refresh()
	{
		clearPoints();
		Statistic<?> statistic = baseModel.getLineSegment().getStatistic();
		for( DataPoint<?> dataPoint : statistic.getPeriod( baseModel.getXRangeMin(), baseModel.getXRangeMax(),
				baseModel.getLevel(), execution ) )
		{
			ScaledChartPoint point = baseModel.getScaler().createPoint( dataPoint.getTimestamp(),
					dataPoint.getValue().doubleValue() );
			addPoint( point, false );
		}
		update();
	}

	private class BaseModelListener implements ChartModelListener
	{
		@Override
		public void chartModelChanged()
		{
			refresh();
		}
	}

	private class ChartGroupListener implements WeakEventHandler<PropertyChangeEvent>
	{
		@Override
		public void handleEvent( PropertyChangeEvent event )
		{
			if( event.getSource() == baseModel.getLineSegment() )
			{
				if( LineSegmentModel.COLOR.equals( event.getPropertyName() ) )
				{
					Color color = ColorFactory.transitionColor( ( Color )event.getNewValue(), Color.BLACK, 0.5 );
					chartStyle.setLineColor( color );
				}
				else if( LineSegmentModel.STROKE.equals( event.getPropertyName() )
						|| LineSegmentModel.WIDTH.equals( event.getPropertyName() ) )
				{
					chartStyle.setLineStroke( baseModel.getStrokeStyle().getStroke( baseModel.getStrokeWidth() ) );
					fireModelChanged();
				}
			}
		}
	}
}