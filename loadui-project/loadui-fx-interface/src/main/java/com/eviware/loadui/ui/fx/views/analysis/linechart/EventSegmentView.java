package com.eviware.loadui.ui.fx.views.analysis.linechart;

import javafx.fxml.FXML;

import com.eviware.loadui.api.statistics.model.chart.line.TestEventSegment;
import com.eviware.loadui.ui.fx.util.FXMLUtils;

public class EventSegmentView extends SegmentView<TestEventSegment>
{

	public EventSegmentView( TestEventSegment segment )
	{
		super( segment );
		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		segmentLabel.setText( segment.getTypeLabel() + " " + segment.getSourceLabel() );
	}
}
