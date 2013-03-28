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
package com.eviware.loadui.ui.fx.control.skin;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.NumberExpression;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.PopupControl;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.RegionBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;
import javafx.util.converter.NumberStringConverter;

import com.eviware.loadui.ui.fx.control.Knob;
import com.eviware.loadui.ui.fx.control.behavior.KnobBehavior;
import com.sun.javafx.Utils;
import com.sun.javafx.scene.control.skin.SkinBase;

public class KnobSkin extends SkinBase<Knob, KnobBehavior>
{
	private final ValueDisplay valueDisplay;
	private final Label label;
	private final StackPane base;
	private final Region handle;

	private DoubleProperty startAngle;

	public DoubleProperty startAngleProperty()
	{
		if( startAngle == null )
		{
			startAngle = new SimpleDoubleProperty( this, "startAngle", getStartAngle() );
		}
		return startAngle;
	}

	public double getStartAngle()
	{
		return startAngle == null ? Math.PI / 2 : startAngle.get();
	}

	public void setStartAngle( double value )
	{
		startAngleProperty().setValue( value );
	}

	private DoubleProperty angleSpan;

	public DoubleProperty angleSpanProperty()
	{
		if( angleSpan == null )
		{
			angleSpan = new SimpleDoubleProperty( this, "angleSpan", getAngleSpan() );
		}
		return angleSpan;
	}

	public double getAngleSpan()
	{
		return angleSpan == null ? 2 * Math.PI : angleSpan.get();
	}

	public void setAngleSpan( double value )
	{
		angleSpanProperty().setValue( value );
	}

	private final NumberExpression angle;

	public NumberExpression angleProperty()
	{
		return angle;
	}

	public Double getAngle()
	{
		return angle.doubleValue();
	}

	public KnobSkin( final Knob control )
	{
		super( control, new KnobBehavior( control ) );

		angle = new DoubleBinding()
		{
			{
				bind( control.valueProperty(), control.spanProperty() );
			}

			@Override
			protected double computeValue()
			{
				double value = Double.isInfinite( getSkinnable().getMin() ) ? getSkinnable().getValue() : getSkinnable()
						.getValue() - getSkinnable().getMin();
				return getStartAngle() + ( getAngleSpan() * value / getSkinnable().getSpan() ) % getAngleSpan();
			}
		};

		label = LabelBuilder.create().build();
		label.textProperty().bind( control.textProperty() );

		handle = RegionBuilder.create().styleClass( "handle" ).build();

		base = StackPaneBuilder.create().styleClass( "ticks" )
				.children( RegionBuilder.create().styleClass( "base" ).build() ).build();

		addEventHandler( MouseEvent.ANY, new DragBehavior() );
		setOnScroll( new EventHandler<ScrollEvent>()
		{
			@Override
			public void handle( ScrollEvent event )
			{
				getBehavior().increment( ( int )Math.signum( event.getDeltaY() ) );
				event.consume();
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

		valueDisplay = new ValueDisplay();

		getChildren().setAll( base, handle, label );
	}

	public Label getLabel()
	{
		return label;
	}

	public Region getBase()
	{
		return base;
	}

	public Region getHandle()
	{
		return handle;
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
		double x = Math.cos( getAngle() ) * radius;
		double y = Math.sin( getAngle() ) * radius;
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
				valueDisplay.setEditable( false );
				valueDisplay.display();
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
				valueDisplay.setEditable( true );
				valueDisplay.hide();
			}
			else if( event.getEventType() == MouseEvent.MOUSE_CLICKED )
			{
				if( event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 )
				{
					valueDisplay.display();
				}
			}
		}
	}

	private class ValueDisplay extends PopupControl
	{
		private final TextField valueField = new TextField();
		private final NumberExpression textLength;

		private ValueDisplay()
		{
			setAutoHide( true );

			textLength = new IntegerBinding()
			{
				{
					bind( valueField.textProperty() );
				}

				@Override
				protected int computeValue()
				{
					return valueField.getText().length();
				}
			};

			valueField.prefColumnCountProperty().bind( textLength.add( 3 ).divide( 2 ) );
			valueField.textProperty().bindBidirectional( getSkinnable().valueProperty(), new NumberStringConverter() );
			valueField.setOnAction( new EventHandler<ActionEvent>()
			{
				@Override
				public void handle( ActionEvent event )
				{
					hide();
				}
			} );

			bridge.getChildren().setAll( valueField );
		}

		public void display()
		{
			Point2D point = Utils.pointRelativeTo( getSkinnable(), valueField.getWidth(), 0, HPos.CENTER, VPos.TOP, false );
			show( getSkinnable(), point.getX(), point.getY() - 20 );
		}

		public void setEditable( boolean value )
		{
			valueField.setEditable( value );
		}
	}
}
