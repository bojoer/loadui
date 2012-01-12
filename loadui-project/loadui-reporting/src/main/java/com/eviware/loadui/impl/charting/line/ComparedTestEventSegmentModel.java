package com.eviware.loadui.impl.charting.line;

import java.awt.BasicStroke;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.util.HashSet;
import java.util.concurrent.Callable;

import javax.swing.SwingUtilities;

import com.eviware.loadui.api.charting.line.LineSegmentModel;
import com.eviware.loadui.api.charting.line.SegmentModel;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.model.chart.line.TestEventSegment;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.charting.LineChartUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.jidesoft.chart.LineMarker;
import com.jidesoft.chart.Orientation;
import com.jidesoft.chart.model.ChartModelListener;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.style.ChartStyle;
import com.jidesoft.chart.util.ColorFactory;

public class ComparedTestEventSegmentModel extends AbstractSegmentModel implements Releasable
{
	private final ChartGroupListener chartGroupListener = new ChartGroupListener();
	private final HashSet<LineMarker> lineMarkers = Sets.newHashSet();
	private final TestEventSegmentModel baseModel;
	private final LineChartImpl chart;
	private final ChartModelListener listener = new ChartModelListener()
	{
		@Override
		public void chartModelChanged()
		{
			redraw();
		}
	};

	public ComparedTestEventSegmentModel( LineChartImpl chart, TestEventSegmentModel baseModel )
	{
		super( baseModel.getSegment(), "Compared " + baseModel.getName(), new ChartStyle( baseModel.getChartStyle() ) );

		this.chart = chart;
		this.baseModel = baseModel;
		chartStyle.setLineColor( ColorFactory.transitionColor( chartStyle.getLineColor(), Color.BLACK, 0.5 ) );
		baseModel.getChartGroup().addEventListener( PropertyChangeEvent.class, chartGroupListener );
		baseModel.addChartModelListener( listener );
	}

	@Override
	public TestEventSegment getSegment()
	{
		return ( TestEventSegment )super.getSegment();
	}

	@Override
	public DefaultChartModel addPoint( double x, double y, boolean update )
	{
		LineMarker line = new LineMarker( chart, Orientation.vertical, x, chartStyle.getLineColor() );
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
	protected void redraw()
	{
		appendRead( new Callable<Iterable<DataPoint<?>>>()
		{
			@Override
			public Iterable<DataPoint<?>> call() throws Exception
			{
				return execution == null ? ImmutableList.<DataPoint<?>> of() : Iterables.transform( getSegment()
						.getPointsInRange( execution, baseModel.getXRangeMin(), baseModel.getXRangeMax() ),
						TestEventSegmentModel.longToDataPoint );
			}
		}, 1 );
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
	public void release()
	{
		clearPoints();
		baseModel.getChartGroup().removeEventListener( PropertyChangeEvent.class, chartGroupListener );
		baseModel.removeChartModelListener( listener );
	}

	private class ChartGroupListener implements WeakEventHandler<PropertyChangeEvent>
	{
		@Override
		public void handleEvent( final PropertyChangeEvent event )
		{
			if( event.getSource() == baseModel.getSegment() )
			{
				SwingUtilities.invokeLater( new Runnable()
				{
					@Override
					public void run()
					{
						if( SegmentModel.COLOR.equals( event.getPropertyName() ) )
						{
							Color color = ColorFactory.transitionColor( ( Color )event.getNewValue(), Color.BLACK, 0.5 );
							chartStyle.setLineColor( color );
							for( LineMarker line : ImmutableList.copyOf( lineMarkers ) )
							{
								line.setColor( color );
							}
						}
						else if( LineSegmentModel.STROKE.equals( event.getPropertyName() )
								|| LineSegmentModel.WIDTH.equals( event.getPropertyName() ) )
						{
							BasicStroke stroke = baseModel.getStrokeStyle().getStroke( baseModel.getStrokeWidth() );
							chartStyle.setLineStroke( stroke );
							for( LineMarker line : ImmutableList.copyOf( lineMarkers ) )
							{
								line.setStroke( stroke );
							}
						}
					}
				} );
			}
		}
	}
}
