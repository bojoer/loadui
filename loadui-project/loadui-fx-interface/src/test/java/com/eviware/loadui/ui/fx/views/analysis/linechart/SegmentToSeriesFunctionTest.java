package com.eviware.loadui.ui.fx.views.analysis.linechart;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import org.junit.Test;

import com.eviware.loadui.api.charting.line.ZoomLevel;
import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.model.chart.line.LineSegment;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.api.analysis.ExecutionChart;
import com.eviware.loadui.util.statistics.DataPointImpl;

public class SegmentToSeriesFunctionTest
{


	@Test
	public void testSegmentToSeriesWithZoomLevelALL()
	{
		Execution execution = mock( Execution.class );
		ObservableValue<Execution> executionObs = new SimpleObjectProperty<Execution>( execution );

		Observable obs1 = new SimpleObjectProperty<String>( "Hi" );
		ObservableList<Observable> observables = FXCollections.observableArrayList( obs1 );

		ExecutionChart chart = mock( ExecutionChart.class );
		when( chart.getPosition() ).thenReturn( 0D );
		when( chart.getSpan() ).thenReturn( 0L );
		when( chart.getTickZoomLevel() ).thenReturn( ZoomLevel.ALL );
		
		SegmentToSeriesFunction f = new SegmentToSeriesFunction( executionObs, observables, chart );

		Statistic<Number> stat = mock( Statistic.class );
		Iterable<DataPoint<Number>> period = createDataPoints();

		when( stat.getPeriod( -2000L, 2000L, ZoomLevel.ALL.getLevel(), execution ) ).thenReturn( period );

		LineSegment segment = mock( LineSegment.class );
		when( segment.getStatistic() ).thenReturn( stat );
		when( segment.getAttribute( LineSegmentView.SCALE_ATTRIBUTE, "0" ) ).thenReturn( "1" );

		when( chart.getColor( segment, execution )).thenReturn( Color.BLACK );
		
		Series<Number, Number> series = f.apply( segment );
		
		assertEquals( Color.BLACK, ((Circle) series.getData().iterator().next().getNode()).getFill() );
		
		//TODO check the series is correct
	}

	private List<DataPoint<Number>> createDataPoints()
	{
		List<DataPoint<Number>> dps = new ArrayList<>();
		for( int i = 0; i < 100; i++ )
		{
			dps.add( new DataPointImpl<Number>( i, i + 1 ) );
		}

		return dps;
	}

}
