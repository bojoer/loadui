package com.eviware.loadui.impl.charting.line;

import java.awt.BasicStroke;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.util.HashSet;
import java.util.concurrent.Callable;

import javax.swing.SwingUtilities;

import com.eviware.loadui.api.charting.line.SegmentModel;
import com.eviware.loadui.api.charting.line.StrokeStyle;
import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.line.TestEventSegment;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.api.testevents.TestEventManager.TestEventObserver;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.charting.LineChartUtils;
import com.eviware.loadui.util.statistics.DataPointImpl;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.jidesoft.chart.LineMarker;
import com.jidesoft.chart.Orientation;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.style.ChartStyle;

public class TestEventSegmentModel extends AbstractSegmentModel implements SegmentModel, Releasable
{
	private static final TestEventListener testEventListener = new TestEventListener();

	private final LineChartImpl chart;
	private final ChartGroup chartGroup;
	private final HashSet<LineMarker> lineMarkers = Sets.newHashSet();
	private final Function<Long, DataPoint<?>> longToDataPoint = new Function<Long, DataPoint<?>>()
	{
		@Override
		public DataPoint<?> apply( Long input )
		{
			return new DataPointImpl<Number>( input, 0 );
		}
	};

	private Color color = Color.decode( LineChartStyles.lineColors[0] );
	private StrokeStyle strokeStyle;
	private int strokeWidth = 1;
	private long xRangeMin = 0;
	private long xRangeMax = 0;

	public TestEventSegmentModel( LineChartImpl chart, LineChartView chartView, TestEventSegment segment )
	{
		super( segment, Integer.toString( System.identityHashCode( segment ) ), new ChartStyle() );
		this.chart = chart;
		this.chartGroup = chartView.getChartGroup();
		testEventListener.addModel( this );

		loadStyles();
	}

	@Override
	protected void redraw()
	{
		appendRead( new Callable<Iterable<DataPoint<?>>>()
		{
			@Override
			public Iterable<DataPoint<?>> call() throws Exception
			{
				return execution == null ? ImmutableList.<DataPoint<?>> of() : Iterables.transform( getSegment()
						.getPointsInRange( execution, xRangeMin, xRangeMax ), longToDataPoint );
			}
		}, 1 );
	}

	@Override
	public TestEventSegment getSegment()
	{
		return ( TestEventSegment )super.getSegment();
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

	@Override
	public DefaultChartModel addPoint( double x, double y, boolean update )
	{
		LineMarker line = new LineMarker( chart, Orientation.vertical, x, getColor() );
		line.setStroke( chartStyle.getLineStroke() );
		chart.addDrawable( line );
		if( update )
		{
			update();
			chart.update();
		}
		lineMarkers.add( line );

		return null;
	}

	@Override
	public void clearPoints()
	{
		LineChartUtils.invokeInSwingLater( new Runnable()
		{
			@Override
			public void run()
			{
				for( LineMarker line : lineMarkers )
				{
					chart.removeDrawable( line );
				}

				lineMarkers.clear();
			}
		} );

		super.clearPoints();
	}

	@Override
	public Color getColor()
	{
		return color;
	}

	@Override
	public void setColor( Color color )
	{
		setColor( color, true );
	}

	private void setColor( Color color, boolean fireEvent )
	{
		if( !this.color.equals( color ) )
		{
			Color oldColor = this.color;
			this.color = color;
			chartStyle.setLineColor( color );
			for( LineMarker line : ImmutableList.copyOf( lineMarkers ) )
			{
				line.setColor( color );
			}

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

	@Override
	public void release()
	{
		testEventListener.removeModel( this );
		clearPoints();
	}

	private void loadStyles()
	{
		String colorStr = segment.getAttribute( COLOR, null );
		if( colorStr == null )
		{
			colorStr = LineChartStyles.getLineColor( chartGroup, getSegment() );
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
		BasicStroke stroke = strokeStyle.getStroke( strokeWidth );
		chartStyle.setLineStroke( stroke );

		for( LineMarker line : ImmutableList.copyOf( lineMarkers ) )
		{
			line.setStroke( stroke );
		}
	}

	private static class TestEventListener implements TestEventObserver
	{
		private final HashSet<TestEventSegmentModel> models = Sets.newHashSet();

		public TestEventListener()
		{
			BeanInjector.getBean( TestEventManager.class ).registerObserver( this );
		}

		@Override
		public synchronized void onTestEvent( final TestEvent.Entry eventEntry )
		{
			for( final TestEventSegmentModel model : models )
			{
				TestEventSegment segment = model.getSegment();
				if( Objects.equal( segment.getTypeLabel(), eventEntry.getTypeLabel() )
						&& Objects.equal( segment.getSourceLabel(), eventEntry.getSourceLabel() ) )
				{
					log.debug( "Notifying model: {} of event: {}", model, eventEntry.getTestEvent() );
					SwingUtilities.invokeLater( new Runnable()
					{
						@Override
						public void run()
						{
							//TODO: For some reason lines don't become visible unless a complete redraw is done. If anyone figures out why, feel free to just add the new line.
							model.redraw();
							//model.addPoint( eventEntry.getTestEvent().getTimestamp(), 0, true );
							//model.update();
						}
					} );
				}
			}
		}

		private synchronized void addModel( TestEventSegmentModel model )
		{
			models.add( model );
		}

		private synchronized void removeModel( TestEventSegmentModel model )
		{
			models.remove( model );
		}
	}
}
