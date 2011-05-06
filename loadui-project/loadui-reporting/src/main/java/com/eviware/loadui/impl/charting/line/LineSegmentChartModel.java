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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.charting.line.LineSegmentModel;
import com.eviware.loadui.api.charting.line.StrokeStyle;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.LineChartView.LineSegment;
import com.eviware.loadui.api.statistics.store.Execution;
import com.jidesoft.chart.style.ChartStyle;

public class LineSegmentChartModel extends AbstractLineSegmentModel implements LineSegmentModel
{
	public static final Logger log = LoggerFactory.getLogger( LineSegmentChartModel.class );

	private final ChartGroup chartGroup;
	private final LineSegment segment;
	private final StyleEventListener listener = new StyleEventListener();

	private long latestTime = 0;
	private long xRangeMin = 0;
	private long xRangeMax = 0;
	private int level = 0;
	private int scale = 0;
	private Color color = Color.decode( LineChartStyles.lineColors[0] );
	private StrokeStyle strokeStyle;
	private int strokeWidth = 1;

	public LineSegmentChartModel( LineChartView chartView, LineSegment segment )
	{
		super( segment.getStatistic().getName(), new ChartStyle() );
		this.segment = segment;

		chartGroup = chartView.getChartGroup();
		chartGroup.addEventListener( PropertyChangeEvent.class, listener );

		loadStyles();
	}

	public void poll()
	{
		DataPoint<?> dataPoint = segment.getStatistic().getLatestPoint( level );
		if( dataPoint != null )
		{
			long timestamp = dataPoint.getTimestamp();
			if( timestamp != latestTime && timestamp >= 0 )
			{
				latestTime = timestamp;
				if( xRangeMin <= timestamp && timestamp <= xRangeMax )
					addPoint( timestamp, scalar * dataPoint.getValue().doubleValue() );
			}
		}
	}

	@Override
	protected void redraw()
	{
		doRedraw( segment.getStatistic(), xRangeMin, xRangeMax, level );
	}

	private void loadStyles()
	{
		int scale = 0;
		try
		{
			scale = Integer.parseInt( segment.getAttribute( SCALE, "0" ) );
		}
		catch( NumberFormatException e )
		{
		}
		setScale( scale, false );

		String colorStr = segment.getAttribute( COLOR, null );
		if( colorStr == null )
		{
			colorStr = LineChartStyles.getLineColor( chartGroup, segment );
			segment.setAttribute( COLOR, colorStr );
		}
		setColor( Color.decode( colorStr ), false );
		chartStyle.setLineColor( color );

		try
		{
			strokeWidth = Integer.parseInt( segment.getAttribute( WIDTH, "1" ) );
		}
		catch( NumberFormatException e )
		{
			strokeWidth = 1;
		}

		try
		{
			strokeStyle = StrokeStyle.valueOf( segment.getAttribute( STROKE, StrokeStyle.SOLID.name() ) );
		}
		catch( IllegalArgumentException e )
		{
			strokeStyle = StrokeStyle.SOLID;
		}

		updateStroke();
	}

	private void updateStroke()
	{
		chartStyle.setLineStroke( strokeStyle.getStroke( strokeWidth ) );
		fireModelChanged();
	}

	@Override
	public LineSegment getLineSegment()
	{
		return segment;
	}

	public ChartGroup getChartGroup()
	{
		return chartGroup;
	}

	public Execution getExecution()
	{
		return execution;
	}

	@Override
	public void setExecution( Execution execution )
	{
		super.setExecution( execution );
		latestTime = execution.getLength();
	}

	public long getLatestTime()
	{
		return latestTime;
	}

	public long getXRangeMin()
	{
		return xRangeMin;
	}

	public long getXRangeMax()
	{
		return xRangeMax;
	}

	public void setXRange( long min, long max )
	{
		if( xRangeMin != min || xRangeMax != max )
		{
			xRangeMin = min;
			xRangeMax = max;
			redraw();
		}
	}

	public int getLevel()
	{
		return level;
	}

	public void setLevel( int level )
	{
		this.level = level;
	}

	@Override
	public int getScale()
	{
		return scale;
	}

	private void setScale( int scale, boolean fireEvent )
	{
		if( scale != this.scale )
		{
			int oldScale = this.scale;
			this.scale = scale;
			scalar = Math.pow( 10, scale );
			redraw();

			if( fireEvent )
			{
				segment.setAttribute( SCALE, String.valueOf( scale ) );
				chartGroup.fireEvent( new PropertyChangeEvent( segment, SCALE, oldScale, scale ) );
			}
		}
	}

	@Override
	public void setScale( int scale )
	{
		setScale( scale, true );
	}

	public double getScalar()
	{
		return scalar;
	}

	@Override
	public Color getColor()
	{
		return color;
	}

	private void setColor( Color color, boolean fireEvent )
	{
		if( !this.color.equals( color ) )
		{
			Color oldColor = this.color;
			this.color = color;
			chartStyle.setLineColor( color );
			fireModelChanged();
			if( fireEvent )
			{
				String partialColor = Integer.toHexString( color.getRGB() & 0xFFFFFF );
				String colorString = "#" + "000000".substring( partialColor.length() ) + partialColor.toUpperCase();
				segment.setAttribute( COLOR, colorString );
				chartGroup.fireEvent( new PropertyChangeEvent( segment, COLOR, oldColor, color ) );
			}
		}
	}

	@Override
	public void setColor( Color color )
	{
		setColor( color, true );
	}

	@Override
	public StrokeStyle getStrokeStyle()
	{
		return strokeStyle;
	}

	@Override
	public void setStrokeStyle( StrokeStyle strokeStyle )
	{
		if( this.strokeStyle != strokeStyle )
		{
			StrokeStyle oldStrokeStyle = strokeStyle;
			this.strokeStyle = strokeStyle;
			segment.setAttribute( STROKE, strokeStyle.name() );
			chartGroup.fireEvent( new PropertyChangeEvent( segment, STROKE, oldStrokeStyle, strokeStyle ) );
			updateStroke();
		}
	}

	@Override
	public int getStrokeWidth()
	{
		return strokeWidth;
	}

	@Override
	public void setStrokeWidth( int strokeWidth )
	{
		if( this.strokeWidth != strokeWidth )
		{
			int oldStrokeWidth = this.strokeWidth;
			this.strokeWidth = strokeWidth;
			segment.setAttribute( WIDTH, String.valueOf( strokeWidth ) );
			chartGroup.fireEvent( new PropertyChangeEvent( segment, WIDTH, oldStrokeWidth, strokeWidth ) );
			updateStroke();
		}
	}

	private class StyleEventListener implements WeakEventHandler<PropertyChangeEvent>
	{
		@Override
		public void handleEvent( final PropertyChangeEvent event )
		{
			if( event.getSource() == segment )
			{
				SwingUtilities.invokeLater( new Runnable()
				{
					@Override
					public void run()
					{
						if( SCALE.equals( event.getPropertyName() ) )
						{
							setScale( ( Integer )event.getNewValue(), false );
						}
						else if( COLOR.equals( event.getPropertyName() ) )
						{
							setColor( ( Color )event.getNewValue(), false );
						}
						else if( STROKE.equals( event.getPropertyName() ) )
						{
							strokeStyle = ( StrokeStyle )event.getNewValue();
							updateStroke();
						}
						else if( WIDTH.equals( event.getPropertyName() ) )
						{
							strokeWidth = ( Integer )event.getNewValue();
							updateStroke();
						}
					}
				} );
			}
		}
	}
}