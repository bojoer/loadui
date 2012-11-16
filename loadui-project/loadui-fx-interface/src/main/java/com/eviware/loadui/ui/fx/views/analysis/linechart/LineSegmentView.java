package com.eviware.loadui.ui.fx.views.analysis.linechart;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import com.eviware.loadui.api.statistics.model.chart.line.LineSegment;
import com.eviware.loadui.ui.fx.util.FXMLUtils;

public class LineSegmentView extends Label
{
	private final LineSegment lineSegment;

	public LineSegmentView( LineSegment lineSegment )
	{
		this.lineSegment = lineSegment;

		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		setText( lineSegment.getStatisticHolder().getLabel() + " " + lineSegment.getVariableName() + " "
				+ lineSegment.getStatisticName() );
	}
}