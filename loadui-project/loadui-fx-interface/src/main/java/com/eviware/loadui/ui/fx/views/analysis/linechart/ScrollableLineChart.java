package com.eviware.loadui.ui.fx.views.analysis.linechart;

import java.io.File;

import javax.imageio.ImageIO;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.SnapshotResult;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.charting.line.ZoomLevel;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.NodeUtils;

public class ScrollableLineChart extends HBox
{
	private final LongProperty position = new SimpleLongProperty( 0 );
	private final LongProperty shownSpan = new SimpleLongProperty( 60000 );
	private final LongProperty xScale = new SimpleLongProperty( 1 );

	protected static final Logger log = LoggerFactory.getLogger( ScrollableLineChart.class );

	@FXML
	private SegmentBox segmentBox;

	@FXML
	private LineChart<Number, Number> lineChart;

	@FXML
	private NumberAxis xAxis;

	@FXML
	private ChartScrollBar scrollBar;

	public ScrollableLineChart()
	{
		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		scrollBar.visibleAmountProperty().bind( shownSpan );
		scrollBar.blockIncrementProperty().bind( shownSpan.divide( 2 ) );
		scrollBar.unitIncrementProperty().bind( shownSpan.divide( 40 ) );

		position.bind( scrollBar.leftSidePositionProperty() );

	}

	/**
	 * returns the tickmode ZoomLevel
	 */
	public ZoomLevel setZoomLevel( ZoomLevel zoomLevel )
	{
		if( zoomLevel.equals( ZoomLevel.ALL ) )
		{
			zoomLevel = ZoomLevel.forSpan( scrollBar.maxProperty().longValue() / 1000 );
			scrollBar.setDisable( true );

			xAxis.setAutoRanging( true );
			xAxis.lowerBoundProperty().unbind();
			xAxis.upperBoundProperty().unbind();
			shownSpan.bind( maxProperty() );
		}
		else
		{
			scrollBar.setDisable( false );

			xAxis.setAutoRanging( false );
			xAxis.lowerBoundProperty().bind( position );
			xAxis.upperBoundProperty().bind( position.add( shownSpan ).add( 2000d ) );
			shownSpan.bind( xAxis.widthProperty().multiply( xScale ) );
		}

		xScale.setValue( ( 1000.0 * zoomLevel.getInterval() ) / zoomLevel.getUnitWidth() );

		setTickMode( zoomLevel );

		log.debug( "ZoomLevel set to: " + zoomLevel.name() + " xScale is now: " + xScale.getValue() );

		return zoomLevel;

	}

	public void setTickMode( ZoomLevel level )
	{
		int minorTickCount = level.getMajorTickInterval() / level.getInterval();

		// major tick interval
		xAxis.setTickUnit( ( 1000.0 * level.getInterval() * minorTickCount ) );
		xAxis.setMinorTickCount( minorTickCount == 1 ? 0 : minorTickCount );

		scrollBar.setLeftSidePosition( position.doubleValue() );

		log.debug( "TickMode set to: " + level.name() );

	}

	public ReadOnlyLongProperty positionProperty()
	{
		return position;
	}

	public double getPosition()
	{
		return position.doubleValue();
	}

	public void setPosition( double position )
	{
		scrollBar.setLeftSidePosition( position );
	}

	public VBox getSegments()
	{
		return segmentBox.getSegmentsContainer();
	}

	public LineChart<Number, Number> getLineChart()
	{
		return lineChart;
	}

	public LongProperty spanProperty()
	{
		return shownSpan;
	}

	public javafx.beans.Observable scaleUpdate()
	{
		return segmentBox.scaleUpdate();
	}

	public DoubleProperty maxProperty()
	{
		return scrollBar.maxProperty();
	}

	public StringProperty titleProperty()
	{
		return lineChart.titleProperty();
	}
}
