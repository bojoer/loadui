package com.eviware.loadui.ui.fx.control.skin;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.RegionBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;

import com.eviware.loadui.ui.fx.control.Knob;
import com.eviware.loadui.ui.fx.control.behavior.KnobBehavior;
import com.sun.javafx.scene.control.skin.SkinBase;

public class KnobSkin extends SkinBase<Knob, KnobBehavior>
{
	private static final double START_ANGLE = Math.PI / 2;

	private final Label label;
	private final StackPane base;
	private final Region handle;

	public KnobSkin( Knob control )
	{
		super( control, new KnobBehavior( control ) );

		label = LabelBuilder.create().build();
		label.textProperty().bind( control.textProperty() );

		handle = RegionBuilder.create().styleClass( "handle" ).build();

		base = StackPaneBuilder.create().styleClass( "base" ).build();
		addEventHandler( MouseEvent.ANY, new DragBehavior() );
		setOnScroll( new EventHandler<ScrollEvent>()
		{
			@Override
			public void handle( ScrollEvent event )
			{
				getBehavior().increment( ( int )event.getTextDeltaY() );
			}
		} );

		InvalidationListener invalidationListener = new InvalidationListener()
		{
			@Override
			public void invalidated( Observable observable )
			{
				requestLayout();
			}
		};
		control.valueProperty().addListener( invalidationListener );
		control.spanProperty().addListener( invalidationListener );

		getChildren().setAll( base, handle, label );
	}

	@Override
	protected double computeMinHeight( double width )
	{
		return getInsets().getTop() + base.minHeight( width ) + label.minHeight( width ) + getInsets().getBottom();
	}

	@Override
	protected double computePrefHeight( double width )
	{
		return getInsets().getTop() + base.prefHeight( width ) + label.prefHeight( width ) + getInsets().getBottom();
	}

	@Override
	protected void layoutChildren()
	{
		double left = getInsets().getLeft();
		double top = getInsets().getTop();
		double remainingHeight = getHeight() - top - getInsets().getBottom();
		double width = getWidth() - left - getInsets().getRight();

		layoutInArea( label, left, top, width, remainingHeight, getBaselineOffset(), HPos.CENTER, VPos.BOTTOM );
		remainingHeight -= label.getHeight();
		layoutInArea( base, left, top, width, remainingHeight, getBaselineOffset(), Insets.EMPTY, false, false,
				HPos.CENTER, VPos.TOP );
		handle.autosize();

		double handleRadius = Math.max( handle.getWidth(), handle.getHeight() ) / 2;
		double radius = Math.min( remainingHeight, width ) / 2 - handleRadius;
		double angle = START_ANGLE + ( Math.PI * 2 * getSkinnable().getValue() / getSkinnable().getSpan() )
				% ( 2 * Math.PI );
		double x = Math.cos( angle ) * radius;
		double y = Math.sin( angle ) * radius;
		layoutInArea( handle, left + width / 2 + ( x - handleRadius ), top + remainingHeight / 2 + ( y - handleRadius ),
				handleRadius * 2, handleRadius * 2, handle.getBaselineOffset(), HPos.CENTER, VPos.CENTER );
	}

	private class DragBehavior implements EventHandler<MouseEvent>
	{
		private boolean dragging = false;
		private double lastY;

		@Override
		public void handle( MouseEvent event )
		{
			if( event.getEventType() == MouseEvent.DRAG_DETECTED )
			{
				dragging = true;
				lastY = event.getY();
			}
			else if( event.getEventType() == MouseEvent.MOUSE_DRAGGED )
			{
				if( dragging )
				{
					double nextY = event.getY();
					getSkinnable().setValue( getSkinnable().getValue() + getSkinnable().getStep() * ( lastY - nextY ) );
					lastY = nextY;
				}
			}
			else if( event.getEventType() == MouseEvent.MOUSE_RELEASED )
			{
				dragging = false;
			}
		}
	}
}
