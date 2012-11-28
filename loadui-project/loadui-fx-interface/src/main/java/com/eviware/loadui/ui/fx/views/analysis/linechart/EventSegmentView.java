package com.eviware.loadui.ui.fx.views.analysis.linechart;

import javafx.fxml.FXML;

import com.eviware.loadui.api.statistics.model.chart.line.TestEventSegment;
import com.eviware.loadui.ui.fx.util.FXMLUtils;

public class EventSegmentView extends SegmentView
{
	private final TestEventSegment eventSegment;

	public EventSegmentView( TestEventSegment lineSegment )
	{
		this.eventSegment = lineSegment;

		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		setText( eventSegment.getTypeLabel() + " " + eventSegment.getSourceLabel() );
	}

}