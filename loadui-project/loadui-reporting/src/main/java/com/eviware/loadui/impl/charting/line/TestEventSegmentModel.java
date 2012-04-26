package com.eviware.loadui.impl.charting.line;

import java.awt.BasicStroke;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.swing.SwingUtilities;

import com.eviware.loadui.api.annotations.Strong;
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
import com.eviware.loadui.util.annotations.AnnotationUtils;
import com.eviware.loadui.util.charting.LineChartUtils;
import com.eviware.loadui.util.statistics.DataPointImpl;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.jidesoft.chart.Chart;
import com.jidesoft.chart.LineMarker;
import com.jidesoft.chart.Orientation;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.style.ChartStyle;

public class TestEventSegmentModel extends AbstractSegmentModel implements SegmentModel.MutableStrokeStyle, Releasable
{
	private static final TestEventListener testEventListener = new TestEventListener();
	public static final Function<TestEvent, DataPoint<?>> longToDataPoint = new Function<TestEvent, DataPoint<?>>()
	{
		@Override
		public DataPoint<?> apply( TestEvent input )
		{
			//Smuggle the style in the y field:
			int strong = AnnotationUtils.hasAnnotation( input, Strong.class ) ? 1 : 0;
			return new DataPointImpl<Number>( input.getTimestamp(), strong );
		}
	};

	private final LineChartImpl chart;
	private final ChartGroup chartGroup;
	private final Set<LineMarker> lineMarkers = Sets.newHashSet();

	private Color color = Color.decode( LineChartStyles.lineColors[0] );
	private StrokeStyle strokeStyle;
	private long xRangeMin = 0;
	private long xRangeMax = 0;
	private int level = 0;

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
		fireModelChanged();
		appendRead( new Callable<Iterable<DataPoint<?>>>()
		{
			@Override
			public Iterable<DataPoint<?>> call() throws Exception
			{
				return execution == null ? ImmutableList.<DataPoint<?>> of() : Iterables.transform( getSegment()
						.getTestEventsInRange( execution, xRangeMin, xRangeMax, level ), longToDataPoint );
			}
		}, 1 );
	}

	@Override
	public final TestEventSegment getSegment()
	{
		return ( TestEventSegment )super.getSegment();
	}

	public ChartGroup getChartGroup()
	{
		return chartGroup;
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
		LineMarker line;

		if( y > 0 )
		{
			line = new ThickLineMarker( chart, Orientation.vertical, x, getColor() );
			line.setStroke( strokeStyle.getStroke( 3 ) );
		}
		else
		{
			line = new LineMarker( chart, Orientation.vertical, x, getColor() );
			line.setStroke( strokeStyle.getStroke( 1 ) );
		}

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
		return 1;
	}

	@Override
	public void release()
	{
		testEventListener.removeModel( this );
		clearPoints();
	}

	public int getLevel()
	{
		return level;
	}

	public void setLevel( int level )
	{
		this.level = level;
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
		BasicStroke thinStroke = strokeStyle.getStroke( 1 );
		BasicStroke thickStroke = strokeStyle.getStroke( 3 );

		chartStyle.setLineStroke( thinStroke );

		for( LineMarker line : ImmutableList.copyOf( lineMarkers ) )
		{
			line.setStroke( AnnotationUtils.hasAnnotation( line, Strong.class ) ? thickStroke : thinStroke );
		}
	}

	private static class TestEventListener implements TestEventObserver
	{
		private final Set<TestEventSegmentModel> models = Sets.newHashSet();

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

	@Strong
	private static class ThickLineMarker extends LineMarker
	{
		public ThickLineMarker( Chart chart, Orientation orientation, double x, Color color )
		{
			super( chart, orientation, x, color );
		}
	}
}
