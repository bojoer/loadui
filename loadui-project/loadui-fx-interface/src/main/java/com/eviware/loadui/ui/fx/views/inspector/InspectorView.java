package com.eviware.loadui.ui.fx.views.inspector;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.TimelineBuilder;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import com.eviware.loadui.ui.fx.util.FXMLUtils;

public class InspectorView extends VBox
{
	private final BooleanProperty minimizedProperty = new SimpleBooleanProperty( this, "minimized", false );

	private boolean dragging = false;
	private double startY = 0;
	private double lastHeight = 0;

	@FXML
	private Region buttonBar;

	public InspectorView()
	{
		FXMLUtils.load( this );
	}

	@FXML
	protected void initialize()
	{
		setMaxHeight( 50 );

		buttonBar.addEventHandler( MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				startY = event.getScreenY() + getHeight();
			}
		} );
		buttonBar.addEventHandler( MouseEvent.DRAG_DETECTED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				dragging = true;
				minimizedProperty.set( false );
			}
		} );
		buttonBar.addEventHandler( MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				if( dragging )
				{
					setMaxHeight( startY - event.getScreenY() );
				}
			}
		} );
		buttonBar.addEventHandler( MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				dragging = false;
				if( getHeight() > getMaxHeight() )
				{
					minimizedProperty.set( true );
				}
			}
		} );

		buttonBar.addEventHandler( MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				if( event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 )
				{
					double target = 0;
					if( minimizedProperty.get() )
					{
						target = lastHeight;
					}
					else
					{
						lastHeight = getHeight();
					}

					TimelineBuilder
							.create()
							.keyFrames(
									new KeyFrame( Duration.seconds( 0.3 ), new KeyValue( maxHeightProperty(), target,
											Interpolator.EASE_BOTH ) ) ).onFinished( new EventHandler<ActionEvent>()
							{
								@Override
								public void handle( ActionEvent arg0 )
								{
									minimizedProperty.set( !minimizedProperty.get() );
								}
							} ).build().playFromStart();
				}
			}
		} );
	}
}
