package com.eviware.loadui.ui.fx.views.analysis.linechart;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import com.eviware.loadui.api.statistics.model.chart.line.Segment;

public abstract class SegmentView<T extends Segment> extends StackPane
{
	protected final T segment;

	@FXML
	protected Label segmentLabel;
	
	@FXML
	protected Rectangle legendColorRectangle; 
	
	protected String color; 
	
	public SegmentView( T segment )
	{
		this.segment = segment;
	}

	public void setColor( String color )
	{
		this.color = color; 
		legendColorRectangle.setStyle( "-fx-fill: " + color + ";");
	}
		
	@FXML
	protected void delete()
	{
		( ( Segment.Removable )segment ).remove();
	}
}
