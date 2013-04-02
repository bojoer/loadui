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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.MouseEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.util.execution.TestExecutionUtils;

public class ChartScrollBar extends ScrollBar
{
	protected static final Logger log = LoggerFactory.getLogger( ChartScrollBar.class );

	private final DoubleProperty leftPositionProperty = new SimpleDoubleProperty( 0d );
	private final BooleanProperty followState = new SimpleBooleanProperty();
	private final DoubleProperty followValue = new SimpleDoubleProperty( 0d );

	private final DoubleProperty followLeftSideValue = new SimpleDoubleProperty( 0d );

	//private boolean followStateinternal;

	public ChartScrollBar()
	{
		super();

		valueProperty().addListener( new InvalidationListener()
		{

			@Override
			public void invalidated( Observable arg0 )
			{
				updateLeftSide();
			}

		} );

		followValue.addListener( new ChangeListener<Number>()
		{
			@Override
			public void changed( ObservableValue<? extends Number> arg0, Number oldValue, Number newValue )
			{
				if( followState.getValue() && !isDisabled() )
				{
					// checks to see if user has moved bar since last updatefollow
					followState.set( leftPositionProperty.get() == followLeftSideValue.get() );

					if( followState.getValue() )
					{
						updateFollow();
					}
				}
			}
		} );

		disableFollowOnUserScroll();

		/*
		 * to make follow instantly go to right position without waiting for data
		 * update. NOTICE do not remove this, it will make binding followValue not
		 * work. there is a card for this issue in leankit / 2013-01-28
		 */
		followState.addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				if( followState.get() )
				{
					updateFollow();
				}

			}
		} );

		visibleAmountProperty().addListener( new InvalidationListener()
		{
			// gets triggered on ZoomLevel change and when Y axis resizes
			@Override
			public void invalidated( Observable arg0 )
			{
				if( !followState.getValue() && !TestExecutionUtils.isExecutionRunning() )
				{
					updateLeftSide();
				}
			}
		} );

	}

	private void disableFollowOnUserScroll()
	{
		skinProperty().addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				Node node = getSkin().getNode().lookup( ".thumb" );
				if( node != null )
				{
					final EventHandler<? super MouseEvent> regularHandler = node.getOnMousePressed();
					node.setOnMousePressed( new EventHandler<MouseEvent>()
					{
						@Override
						public void handle( MouseEvent e )
						{
							followState.set( false );
							regularHandler.handle( e );
						}
					} );
				}
			}
		} );
	}

	public void updateLeftSide()
	{
		double lenght = getMax() - getMin();
		// where the current value is on the axis in percent
		double percentage = getValue() / lenght;
		/*
		 * Equation to get the left point of the scrollBar "thumb". The value
		 * position in the axis reflects where it is on the "thumb". This equation
		 * just subtracts the the length it is inside the "thumb" to get the left
		 * point position of the "thumb"
		 */
		double leftside = getValue() - ( getVisibleAmount() * percentage );
		leftPositionProperty.set( leftside );

	}

	public ReadOnlyDoubleProperty leftSidePositionProperty()
	{
		return leftPositionProperty;
	}

	public BooleanProperty followStateProperty()
	{
		return followState;
	}

	public DoubleProperty followValueProperty()
	{
		return followValue;
	}

	public void updateFollow()
	{
		setLeftSidePosition( followValue.getValue() - getVisibleAmount() + 2000 );
		followLeftSideValue.set( leftPositionProperty.get() );
	}

	public void setLeftSidePosition( double position )
	{

		if( position + getVisibleAmount() > getMax() )
		{
			double validValue = getMax() - getVisibleAmount();
			position = validValue > 0 ? validValue : 0;
		}
		else if( position < 0 )
		{
			position = 0;
		}

		double lenght = getMax() - getMin();

		/*
		 * This is another version of the equation to in the constructor to get
		 * the correct value from an desired position of the left point on the
		 * "thumb".
		 */
		double value = -lenght * position / ( getVisibleAmount() - lenght );

		valueProperty().set( value );
	}

	public void setToLeftSide()
	{
		setLeftSidePosition( getMax() - getVisibleAmount() );
	}
}
