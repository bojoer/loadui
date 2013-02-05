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

public class SegmentBox extends VBox
{
	private final VBox segmentViewContainer = new VBox();
	private final Button scalingCloseButton = ButtonBuilder.create().styleClass( "scaling-close-button" ).text( "Ok" )
			.onAction( new EventHandler<ActionEvent>()
			{
				@Override
				public void handle( ActionEvent _ )
				{
					scaling.set( false );
				}
			} ).build();

	private final Button scalingCancelButton = ButtonBuilder.create().styleClass( "scaling-cancel-button" ).text( "Cancel" )
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
	
	public SegmentBox()
	{
		getStyleClass().add( "chart-segment-box" );
		
		statisticsLabel = LabelBuilder.create().text( "Statistics" ).id( "statistics-label").alignment( Pos.CENTER_LEFT ).build();
		expandCollapseSegments = ToggleButtonBuilder.create().id( "expander-toggle-button" ).alignment( Pos.CENTER_RIGHT ).build();
		
		AnchorPane topBox = AnchorPaneBuilder.create().children( statisticsLabel, expandCollapseSegments ).build();
		AnchorPane.setLeftAnchor( statisticsLabel, 0d);
		AnchorPane.setRightAnchor( expandCollapseSegments, 0d );
				
		scalingButtonBox = HBoxBuilder.create().children( scalingCancelButton, scalingCloseButton ).styleClass( "scaling-button-box").alignment( Pos.BASELINE_RIGHT ).build();
		
		/*Spaces in between components according to design since this class has no FXML (yet?)*/
		betweenStatisticsAndLineSegmentViews = HBoxBuilder.create().minHeight( 6 ).build();
		betweenScalingAndScalingButtons = HBoxBuilder.create().minHeight( 6 ).build();
		
		getChildren().addAll( topBox, betweenStatisticsAndLineSegmentViews, segmentViewContainer );
				
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
					getChildren().addAll(betweenScalingAndScalingButtons, scalingButtonBox);
				}
				else
				{
					getChildren().removeAll(betweenScalingAndScalingButtons, scalingButtonBox);
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
