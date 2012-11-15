package com.eviware.loadui.ui.fx.views.analysis;

import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.Properties;
import com.google.common.base.Objects;

public class ChartGroupView extends VBox
{
	private final ChartGroup chartGroup;
	private final ObservableValue<Execution> executionProperty;
	private final Observable poll;

	@FXML
	private Label chartGroupLabel;

	@FXML
	private VBox segments;

	@FXML
	private StackPane chartView;

	public ChartGroupView( ChartGroup chartGroup, ObservableValue<Execution> executionProperty, Observable poll )
	{
		this.chartGroup = chartGroup;
		this.executionProperty = executionProperty;
		this.poll = poll;

		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		chartGroupLabel.textProperty().bind( Properties.forLabel( chartGroup ) );
		chartView.getChildren().setAll( createChart( chartGroup.getType() ) );
		//TODO: Bind SegmentViews.
	}

	private Node createChart( String type )
	{
		if( Objects.equal( type, LineChartView.class.getName() ) )
		{
			return new LineChartViewNode( executionProperty, ( LineChartView )chartGroup.getChartView(), poll );
		}
		else
		{
			return LabelBuilder.create().text( "Unsupported chart type: " + type ).build();
		}
	}
}
