package com.eviware.loadui.ui.fx.views.analysis.linechart;

import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.line.Segment;
import com.eviware.loadui.util.statistics.ChartUtils;

public abstract class SegmentView<T extends Segment> extends StackPane
{
	public static final String COLOR_ATTRIBUTE = "color";
	protected static final Logger log = LoggerFactory.getLogger( SegmentView.class );

	protected final T segment;
	protected final LineChartView lineChartView;

	@FXML
	protected Label segmentLabel;

	@FXML
	protected Rectangle legendColorRectangle;

	protected String color;

	public SegmentView( T segment, LineChartView lineChartView )
	{
		this.segment = segment;
		this.lineChartView = lineChartView;
		color = segment.getAttribute( COLOR_ATTRIBUTE, "no_color" );
	}

	private String newColor()
	{
		LineChartView mainChart = ( LineChartView )( lineChartView.getChartGroup().getChartView() );

		ArrayList<String> currentColorList = new ArrayList<>();

		for( Segment s : mainChart.getSegments() )
		{
			currentColorList.add( s.getAttribute( COLOR_ATTRIBUTE, "no_color" ) );
		}

		return ChartUtils.getNewRandomColor( currentColorList );
	}

	public void setColor( String color )
	{
		this.color = color;
		legendColorRectangle.setFill( Color.web( color ) );
		segment.setAttribute( COLOR_ATTRIBUTE, color );
	}

	protected void init()
	{
		if( color.equals( "no_color" ) )
		{
			setColor( newColor() );
		}
		else
		{
			legendColorRectangle.setFill( Color.web( color ) );
		}
	}

	@FXML
	protected void delete()
	{
		( ( Segment.Removable )segment ).remove();
	}
}
