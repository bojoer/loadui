package com.eviware.loadui.ui.fx.api.analysis;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.paint.Color;

import com.eviware.loadui.api.charting.line.ZoomLevel;
import com.eviware.loadui.api.statistics.model.chart.line.Segment;
import com.eviware.loadui.api.statistics.store.Execution;

public interface ExecutionChart
{
	public void setZoomLevel( ZoomLevel zoomLevel );

	public void setChartProperties( final ObservableValue<Execution> currentExecution, Observable poll );

	public Node getNode();

	public double getPosition();

	public void setPosition( double position );

	public LineChart<Long, Number> getLineChart();

	public long getSpan();

	public StringProperty titleProperty();

	public BooleanProperty scrollbarFollowStateProperty();

	public ZoomLevel getTickZoomLevel();

	public Color getColor( Segment segment, Execution execution );

}
