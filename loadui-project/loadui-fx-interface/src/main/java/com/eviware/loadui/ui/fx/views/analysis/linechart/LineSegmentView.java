package com.eviware.loadui.ui.fx.views.analysis.linechart;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Slider;

import com.eviware.loadui.api.statistics.model.chart.line.LineSegment;
import com.eviware.loadui.ui.fx.util.FXMLUtils;

public class LineSegmentView extends SegmentView<LineSegment>
{
	private BooleanProperty scaling = new SimpleBooleanProperty( true );

	@FXML
	private MenuButton menuButton;

	@FXML
	private Slider slider;

	public LineSegmentView( LineSegment segment )
	{
		super( segment );
		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		//		segmentLabel.setText( segment.getStatisticHolder().getLabel() + " " + segment.getVariableName() + " "
		//				+ segment.getStatisticName() );

		scaling.addListener( new InvalidationListener()
		{

			@Override
			public void invalidated( Observable arg0 )
			{
				if( scaling.get() )
				{
					menuButton.setDisable( true );
					slider.setDisable( false );
				}
				else
				{
					menuButton.setDisable( false );
					slider.setDisable( true );

				}

			}
		} );

		scaling.set( false );
		slider.setCursor( Cursor.H_RESIZE );

	}

	public void enableScaling( boolean value )
	{
		scaling.set( value );
	}

	public BooleanProperty scalingProperty()
	{
		return scaling;
	}

	@FXML
	public void enableScaling()
	{
		scaling.set( true );
	}

}
