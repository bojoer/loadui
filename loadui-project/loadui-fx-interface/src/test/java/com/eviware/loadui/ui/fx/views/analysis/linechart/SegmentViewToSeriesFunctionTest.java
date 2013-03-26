/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.ui.fx.views.analysis.linechart;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import org.junit.Ignore;
import org.junit.Test;

import com.eviware.loadui.api.charting.line.ZoomLevel;
import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.model.chart.line.LineSegment;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.api.analysis.ExecutionChart;
import com.eviware.loadui.ui.fx.util.Observables;
import com.eviware.loadui.ui.fx.util.Observables.Group;
import com.eviware.loadui.util.statistics.DataPointImpl;

public class SegmentViewToSeriesFunctionTest
{

	@Ignore
	@Test
	public void testSegmentViewToSeriesWithZoomLevelALL()
	{
		Execution execution = mock( Execution.class );
		ObservableValue<Execution> executionObs = new SimpleObjectProperty<Execution>( execution );

		Observable obs1 = new SimpleObjectProperty<String>( "Hi" );
		Observable obs2 = new SimpleObjectProperty<String>( "Hi" );
		Group group = Observables.group( FXCollections.observableArrayList( obs1, obs2 ) );

		ObservableList<Observable> observables = FXCollections.observableArrayList( obs1 );

		ExecutionChart chart = mock( ExecutionChart.class );
		when( chart.getPosition() ).thenReturn( 0D );
		when( chart.getSpan() ).thenReturn( 0L );
		when( chart.getTickZoomLevel() ).thenReturn( ZoomLevel.ALL );
		when( chart.scrollbarFollowStateProperty() ).thenReturn( new SimpleBooleanProperty() );

		SegmentViewToSeriesFunction f = new SegmentViewToSeriesFunction( executionObs, group, obs1, obs2, chart );

		Statistic<Number> stat = mock( Statistic.class );
		Iterable<DataPoint<Number>> period = createDataPoints();

		when( stat.getPeriod( -2000L, 2000L, ZoomLevel.ALL.getLevel(), execution ) ).thenReturn( period );

		SegmentView segmentView = mock( LineSegmentView.class );

		LineSegment segment = mock( LineSegment.class );
		when( segmentView.getSegment() ).thenReturn( segment );
		when( segment.getStatistic() ).thenReturn( stat );
		when( segment.getAttribute( LineSegmentView.SCALE_ATTRIBUTE, "0" ) ).thenReturn( "1" );
		when( chart.getColor( segment, execution ) ).thenReturn( Color.BLACK );

		Series<Long, Number> series = f.apply( segmentView );

		assertEquals( Color.BLACK, ( ( Circle )series.getData().iterator().next().getNode() ).getFill() );

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
