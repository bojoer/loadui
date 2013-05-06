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

import static javafx.beans.binding.Bindings.when;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.control.SeparatorMenuItemBuilder;
import javafx.scene.control.Slider;
import javafx.scene.control.SliderBuilder;
import javafx.scene.layout.Region;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.line.LineSegment;
import com.eviware.loadui.ui.fx.control.skin.StyleableGraphicSlider;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.views.analysis.ShortName;
import com.eviware.loadui.util.statistics.StatisticNameFormatter;

public class LineSegmentView extends SegmentView<LineSegment>
{
	public static final String STROKE_ATTRIBUTE = "stroke";
	public static final String WIDTH_ATTRIBUTE = "width";
	public static final String SCALE_ATTRIBUTE = "scale";

	@FXML
	private MenuButton menuButton;

	private SegmentBox parent;

	private Slider slider;

	private Region sliderNob;

	private static final String scalingStyleClass = "scaling";

	protected static final Logger log = LoggerFactory.getLogger( LineSegmentView.class );

	private final BooleanProperty scaling = new SimpleBooleanProperty( true );
	private int scale = 0;
	private final ReadOnlyBooleanProperty isExpandedProperty;

	public LineSegmentView( LineSegment segment, LineChartView lineChartView, ReadOnlyBooleanProperty isExpandedProperty )
	{
		super( segment, lineChartView );
		this.isExpandedProperty = isExpandedProperty;
		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		super.init();
		slider = SliderBuilder.create().snapToTicks( true ).pickOnBounds( true ).visible( false ).min( -6 ).max( 6 )
				.majorTickUnit( 1 ).minorTickCount( 0 ).build();

		prefWidthProperty().bind( segmentLabel.widthProperty().add( 85 ) );
		minWidthProperty().bind( segmentLabel.widthProperty().add( 85 ) );
		segmentLabel.maxWidth( 400 );
		segmentLabel.wrapTextProperty().set( true );

		String fullName;
		if( LoadUI.isPro() )
		{
			fullName = ( StatisticVariable.MAIN_SOURCE.equals( segment.getSource() ) ? "Total" : segment.getSource() )
					+ " " + segment.getStatisticHolder().getLabel() + " " + segment.getVariableName() + " "
					+ StatisticNameFormatter.format( segment.getStatisticName() );
		}
		else
		{
			fullName = segment.getStatisticHolder().getLabel() + " " + segment.getVariableName() + " "
					+ StatisticNameFormatter.format( segment.getStatisticName() );
		}

		log.debug( "fullName: {}", fullName );

		String shortName = ShortName.forStatistic( segment.getVariableName(), segment.getStatisticName() );
		segmentLabel.textProperty().bind( when( isExpandedProperty ).then( fullName ).otherwise( shortName ) );

		loadStyles();
		getChildren().addAll( slider );

		scaling.addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				if( scaling.get() )
				{
					log.debug( "Started scaling chart" );
					menuButton.setDisable( true );
					getChildren().addAll( slider );
					getStyleClass().addAll( scalingStyleClass );
					slider.visibleProperty().set( true );
					if( sliderNob != null )
					{
						loadNob( slider );
					}
				}
				else
				{
					log.debug( "Finished scaling chart" );
					menuButton.setDisable( false );
					menuButton.setVisible( true );
					getChildren().removeAll( slider );
					getStyleClass().removeAll( scalingStyleClass );
				}
			}
		} );

		scaling.set( false );

		slider.valueProperty().addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable _ )
			{
				int snappedZoom = ( int )Math.round( slider.getValue() );

				if( scale != snappedZoom )
				{
					scale = snappedZoom;
					segment.setAttribute( SCALE_ATTRIBUTE, String.valueOf( scale ) );
					parent.updateChart();
				}
			}
		} );

		slider.getChildrenUnmodifiable().addListener( new ListChangeListener<Node>()
		{
			@Override
			public void onChanged( javafx.collections.ListChangeListener.Change<? extends Node> change )
			{
				while( change.next() )
				{
					for( Node node : change.getAddedSubList() )
					{
						if( node instanceof StyleableGraphicSlider )
						{
							loadNob( node );
						}
					}
				}
			}
		} );

		setMenuItemsFor( menuButton );
		menuButton.getItems().addAll( SeparatorMenuItemBuilder.create().build(),
				MenuItemBuilder.create().id( "scale-item" ).text( "Scale" ).onAction( scaleHandler() ).build() );

	}

	private EventHandler<ActionEvent> scaleHandler()
	{
		return new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				enableParentScaling();
				event.consume();
			}
		};
	}

	private void loadNob( final Node node )
	{
		Platform.runLater( new Runnable()
		{
			public void run()
			{
				Node nob = node.lookup( ".graphic" );
				if( nob != null && nob instanceof Region )
				{
					sliderNob = ( Region )nob;
					sliderNob.setStyle( "-fx-background-color: " + color + ";" );
				}
				else
				{
					log.warn( "Cannot find sliderNob for slider" );
				}
			}
		} );
	}

	private void loadStyles()
	{
		int newScale = 0;
		try
		{
			newScale = Integer.parseInt( segment.getAttribute( SCALE_ATTRIBUTE, "0" ) );
		}
		catch( NumberFormatException e )
		{
		}
		scale = newScale;
		slider.setValue( scale );
	}

	public void enableScaling( boolean value )
	{
		scaling.set( value );
	}

	public BooleanProperty scalingProperty()
	{
		return scaling;
	}

	public void setContainer( SegmentBox segmentBox )
	{
		parent = segmentBox;
	}

	private void enableParentScaling()
	{
		if( parent != null )
		{
			parent.enableScaling();
		}
		else
		{
			log.error( "LineSegmentView with label:(" + segmentLabel.getText()
					+ ") dont have the reference to its SegmentBox" );
		}
	}
}
