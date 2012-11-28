package com.eviware.loadui.ui.fx.views.analysis.linechart;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenuBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItemBuilder;

import com.eviware.loadui.api.statistics.model.chart.line.Segment;

public abstract class SegmentView<T extends Segment> extends Label
{
	protected final T segment;

	public SegmentView( T segment )
	{
		this.segment = segment;
	}

	public void setColor( String color )
	{
		setStyle( "-fx-background-color: " + color + ";" );
	}

	protected void createContextMenu()
	{
		if( segment instanceof Segment.Removable )
		{
			setContextMenu( ContextMenuBuilder.create()
					.items( MenuItemBuilder.create().text( "Remove" ).onAction( new EventHandler<ActionEvent>()
					{
						@Override
						public void handle( ActionEvent _ )
						{
							( ( Segment.Removable )segment ).remove();
						}
					} ).build() ).build() );
		}
	}
}
