package com.eviware.loadui.ui.fx.views.analysis.linechart;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenuBuilder;
import javafx.scene.control.MenuItemBuilder;

import com.eviware.loadui.api.statistics.model.chart.line.LineSegment;
import com.eviware.loadui.ui.fx.util.FXMLUtils;

public class LineSegmentView extends SegmentView
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

		if( lineSegment instanceof LineSegment.Removable )
		{
			setContextMenu( ContextMenuBuilder.create()
					.items( MenuItemBuilder.create().text( "Remove" ).onAction( new EventHandler<ActionEvent>()
					{
						@Override
						public void handle( ActionEvent _ )
						{
							( ( LineSegment.Removable )lineSegment ).remove();
						}
					} ).build() ).build() );
		}
	}

}
