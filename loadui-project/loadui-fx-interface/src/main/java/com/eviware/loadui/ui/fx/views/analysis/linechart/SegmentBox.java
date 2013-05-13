/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
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
import javafx.scene.layout.RegionBuilder;
import javafx.scene.layout.VBox;

import com.eviware.loadui.ui.fx.util.ManualObservable;

public class SegmentBox extends VBox
{
	private final VBox segmentViewContainer = new VBox();
	private final Button scalingCloseButton = ButtonBuilder.create().id( "scaling-ok-button" ).text( "Done" )
			.onAction( new EventHandler<ActionEvent>()
			{
				@Override
				public void handle( ActionEvent _ )
				{
					scaling.set( false );
				}
			} ).build();

	private final ManualObservable chartUpdate = new ManualObservable();
	private final BooleanProperty scaling = new SimpleBooleanProperty( false );
	private final ToggleButton expandCollapseSegments;
	private final Label statisticsLabel;
	private final HBox scalingButtonBox;
	private final HBox betweenStatisticsAndLineSegmentViews;
	private final HBox betweenScalingAndScalingButtons;

	private static final String styleClass = "chart-segment-box";

	public SegmentBox()
	{
		getStyleClass().setAll( styleClass );
		setMinWidth( 170 );
		scaling.set( false );

		statisticsLabel = LabelBuilder.create().text( "Statistics" ).id( "statistics-label" ).alignment( Pos.CENTER_LEFT )
				.build();

		expandCollapseSegments = ToggleButtonBuilder
				.create()
				.id( "expander" )
				.graphic(
						HBoxBuilder
								.create()
								.children( RegionBuilder.create().styleClass( "graphic" ).build(),
										RegionBuilder.create().styleClass( "secondary-graphic" ).build() ).build() )
				.alignment( Pos.CENTER_RIGHT ).build();

		AnchorPane topBox = AnchorPaneBuilder.create().children( statisticsLabel, expandCollapseSegments ).build();

		AnchorPane.setLeftAnchor( statisticsLabel, 0d );
		AnchorPane.setRightAnchor( expandCollapseSegments, 0d );

		scalingButtonBox = HBoxBuilder.create().visible( false )
				.children( scalingCloseButton, HBoxBuilder.create().minWidth( 3 ).build() )
				.styleClass( "scaling-button-box" ).alignment( Pos.BOTTOM_RIGHT ).build();

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
				if( getMinWidth() < view.getWidth() )
				{
					if( minWidthProperty().isBound() )
					{
						minWidthProperty().unbind();
						minWidthProperty().bind( view.widthProperty() );
					}
					else
					{
						minWidthProperty().bind( view.widthProperty() );
					}
				}

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
				chartUpdate.fireInvalidation();
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

	public void updateChart()
	{
		chartUpdate.fireInvalidation();
	}

	public Observable chartUpdate()
	{
		return chartUpdate;
	}

	public void enableScaling()
	{
		scaling.set( true );

	}
}
