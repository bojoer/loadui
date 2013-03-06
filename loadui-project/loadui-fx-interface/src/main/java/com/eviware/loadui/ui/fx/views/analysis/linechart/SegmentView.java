package com.eviware.loadui.ui.fx.views.analysis.linechart;

import java.util.Random;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.line.Segment;
import com.eviware.loadui.util.statistics.ChartUtils;

public abstract class SegmentView<T extends Segment> extends StackPane
{
	public static final String COLOR_ATTRIBUTE = "color";
	protected static final Logger log = LoggerFactory.getLogger( SegmentView.class );

	protected final T segment;
	protected final ChartView chartView;

	@FXML
	protected Label segmentLabel;

	@FXML
	protected Rectangle legendColorRectangle;

	protected String color;

	public SegmentView( T segment, ChartView chartView )
	{
		this.segment = segment;
		this.chartView = chartView;
		loadAttributes();
	}

	private void loadAttributes()
	{
		String color;
		try
		{
			color = segment.getAttribute( COLOR_ATTRIBUTE, "no_color" );
			log.debug( "found color attribute:" + color );
		}
		catch( IllegalArgumentException e )
		{
			color = "no_color";
		}
		this.color = color;
	}

	private String newColor()
	{
		Random rand = new Random();

		int n = rand.nextInt( 8 );

		return ChartUtils.lineToColor( n );
	}

	public void setColor( String color )
	{
		log.debug( "COOOOOOOOOOOOOOOOOOOOOOOLOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOR: " + color );
		this.color = color;
		legendColorRectangle.setFill( Color.web( color ) );
		segment.setAttribute( COLOR_ATTRIBUTE, color );
	}

	protected void init()
	{
		if( color.equals( "no_color" ) )
		{
			setColor( newColor() );
			log.debug( segment + " color reset to: " + color );
		}
		else
		{
			setColor( color );
		}

	}

	@FXML
	protected void delete()
	{
		( ( Segment.Removable )segment ).remove();
	}
}
