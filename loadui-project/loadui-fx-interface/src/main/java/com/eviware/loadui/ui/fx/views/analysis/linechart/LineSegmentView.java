package com.eviware.loadui.ui.fx.views.analysis.linechart;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Slider;
import javafx.scene.control.SliderBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.model.chart.line.LineSegment;
import com.eviware.loadui.ui.fx.util.FXMLUtils;

public class LineSegmentView extends SegmentView<LineSegment>
{
	public static final String COLOR_ATTRIBUTE = "color";
	public static final String STROKE_ATTRIBUTE = "stroke";
	public static final String WIDTH_ATTRIBUTE = "width";
	public static final String SCALE_ATTRIBUTE = "scale";

	@FXML
	private MenuButton menuButton;

	private SegmentBox parent;

	private final Slider slider = SliderBuilder.create().snapToTicks( true ).min( -6 ).max( 6 ).majorTickUnit( 1 )
			.minorTickCount( 0 ).build();

	private final String scalingStyleClass = "scaling";

	protected static final Logger log = LoggerFactory.getLogger( LineSegmentView.class );

	private BooleanProperty scaling = new SimpleBooleanProperty( true );
	private int scale = 0;

	public LineSegmentView( LineSegment segment )
	{
		super( segment );
		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		segmentLabel.setText( segment.getStatisticHolder().getLabel() + " " + segment.getVariableName() + " "
				+ segment.getStatisticName() );

		loadStyles();

		scaling.addListener( new InvalidationListener()
		{

			@Override
			public void invalidated( Observable arg0 )
			{
				if( scaling.get() )
				{
					menuButton.setDisable( true );
					getChildren().addAll( slider );
					getStyleClass().add( scalingStyleClass );
				}
				else
				{
					menuButton.setDisable( false );
					getChildren().removeAll( slider );
					getStyleClass().removeAll( scalingStyleClass );

				}

			}
		} );

		scaling.set( false );

		slider.valueProperty().addListener( new InvalidationListener()
		{

			@Override
			public void invalidated( Observable arg0 )
			{
				int snappedZoom = ( int )Math.round( slider.valueProperty().doubleValue() );

				if( scale != snappedZoom )
				{
					scale = snappedZoom;
					segment.setAttribute( SCALE_ATTRIBUTE, String.valueOf( scale ) );
					parent.updateScale();
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

	@FXML
	public void enableScaling()
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
