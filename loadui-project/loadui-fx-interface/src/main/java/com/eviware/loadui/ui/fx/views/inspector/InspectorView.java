package com.eviware.loadui.ui.fx.views.inspector;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

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

		minimizedProperty.addListener( new ChangeListener<Boolean>()
		{
			@Override
			public void changed( ObservableValue<? extends Boolean> property, Boolean oldVal, Boolean newVal )
			{
				if( newVal )
				{
					lastHeight = getHeight();
					System.out.println( "Storing height: " + lastHeight );
					setMaxHeight( 0 );
				}
				else
				{
					System.out.println( "Restoring height: " + lastHeight );
					setMaxHeight( lastHeight );
				}
			}
		} );

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
			}
		} );

		buttonBar.addEventHandler( MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				if( event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 )
				{
					minimizedProperty.set( !minimizedProperty.get() );
					System.out.println( "TOGGLE!" );
				}
			}
		} );
	}
}
