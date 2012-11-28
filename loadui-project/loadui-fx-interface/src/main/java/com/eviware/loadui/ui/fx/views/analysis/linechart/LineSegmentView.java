package com.eviware.loadui.ui.fx.views.analysis.linechart;

import javafx.fxml.FXML;

import com.eviware.loadui.api.statistics.model.chart.line.LineSegment;
import com.eviware.loadui.ui.fx.util.FXMLUtils;

public class LineSegmentView extends SegmentView<LineSegment>
{
	public LineSegmentView( LineSegment segment )
	{
		super( segment );
		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		setText( segment.getStatisticHolder().getLabel() + " " + segment.getVariableName() + " "
				+ segment.getStatisticName() );

		createContextMenu();
	}

}
