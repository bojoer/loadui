package com.eviware.loadui.ui.fx.views.analysis;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.layout.VBox;

import com.eviware.loadui.ui.fx.views.analysis.linechart.LineSegmentView;

public class SegmentBox extends VBox
{
	private VBox segmentViewContainer = new VBox();

	private final Button scalingCloseButton = ButtonBuilder.create().alignment( Pos.BOTTOM_RIGHT ).text( "Done" )
			.onAction( new EventHandler<ActionEvent>()
			{

				@Override
				public void handle( ActionEvent arg0 )
				{
					scaling.set( false );
				}
			} ).build();

	private BooleanProperty scaling = new SimpleBooleanProperty( false );

	public SegmentBox()
	{
		getChildren().addAll( segmentViewContainer );

		for( Node node : segmentViewContainer.getChildren() )
		{
			if( node instanceof LineSegmentView )
			{
				LineSegmentView view = ( LineSegmentView )node;
				view.scalingProperty().bind( scaling );
				view.setContainer( this );
			}
		}

		segmentViewContainer.getChildren().addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable _ )
			{
				for( Node node : segmentViewContainer.getChildren() )
				{
					if( node instanceof LineSegmentView )
					{
						LineSegmentView view = ( LineSegmentView )node;
						view.scalingProperty().bind( scaling );
						view.setContainer( SegmentBox.this );
					}
				}
			}
		} );

		scaling.addListener( new InvalidationListener()
		{

			@Override
			public void invalidated( Observable arg0 )
			{
				if( scaling.get() )
				{
					getChildren().add( scalingCloseButton );
				}
				else
				{
					getChildren().remove( scalingCloseButton );
				}

			}
		} );

	}

	public VBox getSegmentsContainer()
	{
		return segmentViewContainer;
	}

	public void setEnableScaling( boolean value )
	{
		scaling.set( value );
	}

}
