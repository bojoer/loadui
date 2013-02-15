package com.eviware.loadui.ui.fx.views.analysis.linechart;

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
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleButtonBuilder;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.AnchorPaneBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;

import com.eviware.loadui.ui.fx.util.ManualObservable;
import com.sun.javafx.PlatformUtil;

public class SegmentBox extends VBox
{
	private final VBox segmentViewContainer = new VBox();
	private final Button scalingCloseButton = ButtonBuilder.create().id( "scaling-ok-button" ).text( "Ok" )
			.onAction( new EventHandler<ActionEvent>()
			{
				@Override
				public void handle( ActionEvent _ )
				{
					scaling.set( false );
				}
			} ).build();

	private final Button scalingCancelButton = ButtonBuilder.create().id( "scaling-cancel-button" ).text( "Cancel" )
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
	private final Label statisticsLabel;
	private final HBox scalingButtonBox;
	private final HBox betweenStatisticsAndLineSegmentViews;
	private final HBox betweenScalingAndScalingButtons;

	private final String styleClass = "chart-segment-box";

	public SegmentBox()
	{
		getStyleClass().setAll( styleClass );

		scaling.set( false );

		statisticsLabel = LabelBuilder.create().text( "Statistics" ).id( "statistics-label" ).alignment( Pos.CENTER_LEFT )
				.build();
		expandCollapseSegments = ToggleButtonBuilder.create().id( "expander").styleClass( "styleable-graphic" ).alignment( Pos.CENTER_RIGHT )
				.build();

		AnchorPane topBox = AnchorPaneBuilder.create().children( statisticsLabel, expandCollapseSegments ).build();

		AnchorPane.setLeftAnchor( statisticsLabel, 0d );
		AnchorPane.setRightAnchor( expandCollapseSegments, 0d );

		if( !PlatformUtil.isMac() )
		{
			scalingButtonBox = HBoxBuilder.create().visible( false )
					.children( scalingCloseButton, HBoxBuilder.create().minWidth( 3 ).build(), scalingCancelButton )
					.styleClass( "scaling-button-box" ).alignment( Pos.BOTTOM_RIGHT ).build();

		}
		else
		{
			scalingButtonBox = HBoxBuilder.create().visible( false )
					.children( scalingCancelButton, HBoxBuilder.create().minWidth( 3 ).build(), scalingCloseButton )
					.styleClass( "scaling-button-box" ).alignment( Pos.BOTTOM_RIGHT ).build();

		}

		//Give me some space..
		betweenStatisticsAndLineSegmentViews = HBoxBuilder.create().minHeight( 6 ).build();
		betweenScalingAndScalingButtons = HBoxBuilder.create().minHeight( 6 ).build();

		getChildren().addAll( topBox, betweenStatisticsAndLineSegmentViews, segmentViewContainer,
				betweenScalingAndScalingButtons, scalingButtonBox );

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
					scalingButtonBox.visibleProperty().set( true );
					for( Node node : segmentViewContainer.getChildren() )
					{
						if( node instanceof LineSegmentView )
						{
							LineSegmentView view = ( LineSegmentView )node;
						}
					}
				}
				else
				{
					scalingButtonBox.visibleProperty().set( false );
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
