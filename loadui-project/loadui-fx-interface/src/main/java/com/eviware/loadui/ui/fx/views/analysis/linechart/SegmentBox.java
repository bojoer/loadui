package com.eviware.loadui.ui.fx.views.analysis.linechart;

import static javafx.beans.binding.Bindings.when;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;

import com.eviware.loadui.ui.fx.util.ManualObservable;

public class SegmentBox extends VBox
{
	private final VBox segmentViewContainer = new VBox();
	private final Button scalingCloseButton = ButtonBuilder.create().alignment( Pos.BOTTOM_RIGHT ).text( "Done" )
			.onAction( new EventHandler<ActionEvent>()
			{
				@Override
				public void handle( ActionEvent _ )
				{
					scaling.set( false );
				}
			} ).build();

	private final ManualObservable scaleUpdate = new ManualObservable();
	private final BooleanProperty scaling = new SimpleBooleanProperty( false );
	private final ToggleButton expandCollapseSegments;

	public SegmentBox()
	{
		expandCollapseSegments = new ToggleButton();
		expandCollapseSegments.textProperty().bind(
				when( expandCollapseSegments.selectedProperty() ).then( "<<" ).otherwise( ">>" ) );

		getChildren().addAll( expandCollapseSegments, segmentViewContainer );

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
			public void invalidated( Observable _ )
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

	public ReadOnlyBooleanProperty isExpandedProperty()
	{
		return expandCollapseSegments.selectedProperty();
	}

	public VBox getSegmentsContainer()
	{
		return segmentViewContainer;
	}

	public void updateScale()
	{
		scaleUpdate.fireInvalidation();
	}

	public Observable scaleUpdate()
	{
		return scaleUpdate;
	}

	public void enableScaling()
	{
		scaling.set( true );
	}
}
