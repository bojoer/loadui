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

import javax.swing.SwingUtilities;

import com.eviware.loadui.api.charting.line.LineSegmentModel;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.jidesoft.chart.model.ChartModelListener;
import com.jidesoft.chart.style.ChartStyle;
import com.jidesoft.chart.util.ColorFactory;

public class ComparedLineSegmentChartModel extends AbstractLineSegmentModel
{
	private final LineSegmentChartModel baseModel;
	private final ChartGroupListener chartGroupListener = new ChartGroupListener();

	public ComparedLineSegmentChartModel( LineSegmentChartModel baseModel )
	{
		super( new ChartStyle( baseModel.getChartStyle() ) );

		this.baseModel = baseModel;
		chartStyle.setLineColor( ColorFactory.transitionColor( chartStyle.getLineColor(), Color.BLACK, 0.5 ) );
		baseModel.getChartGroup().addEventListener( PropertyChangeEvent.class, chartGroupListener );
		baseModel.addChartModelListener( new ChartModelListener()
		{
			@Override
			public void chartModelChanged()
			{
				redraw();
			}
		} );
	}

	@Override
	protected void redraw()
	{
		doRedraw( baseModel.getLineSegment().getStatistic(), baseModel.getXRangeMin(), baseModel.getXRangeMax(),
				baseModel.getLevel() );
	}

	@Override
	public String getName()
	{
		return "Compared " + baseModel.getName();
	}

	private class ChartGroupListener implements WeakEventHandler<PropertyChangeEvent>
	{
		@Override
		public void handleEvent( final PropertyChangeEvent event )
		{
			if( event.getSource() == baseModel.getLineSegment() )
			{
				SwingUtilities.invokeLater( new Runnable()
				{
					@Override
					public void run()
					{
						if( LineSegmentModel.COLOR.equals( event.getPropertyName() ) )
						{
							Color color = ColorFactory.transitionColor( ( Color )event.getNewValue(), Color.BLACK, 0.5 );
							chartStyle.setLineColor( color );
						}
						else if( LineSegmentModel.SCALE.equals( event.getPropertyName() ) )
						{
							scalar = Math.pow( 10, baseModel.getScale() );
							redraw();
						}
						else if( LineSegmentModel.STROKE.equals( event.getPropertyName() )
								|| LineSegmentModel.WIDTH.equals( event.getPropertyName() ) )
						{
							chartStyle.setLineStroke( baseModel.getStrokeStyle().getStroke( baseModel.getStrokeWidth() ) );
							fireModelChanged();
						}
					}
				} );
			}
		}
	}
}