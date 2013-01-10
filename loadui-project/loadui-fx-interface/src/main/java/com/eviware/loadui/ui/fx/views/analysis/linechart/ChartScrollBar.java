package com.eviware.loadui.ui.fx.views.analysis.linechart;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.ScrollBar;

import com.eviware.loadui.util.execution.TestExecutionUtils;

public class ChartScrollBar extends ScrollBar
{
	private DoubleProperty leftPositionProperty = new SimpleDoubleProperty( 0d );
	private BooleanProperty followState = new SimpleBooleanProperty();

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

				if( getVisibleAmount() < getMax() && !isDisabled()
						&& followState.get() != ( getValue() == getMax() && TestExecutionUtils.isExecutionRunning() ) )
				{
					followState.set( getValue() == getMax() );
				}

			}
		} );

		maxProperty().addListener( new InvalidationListener()
		{

			@Override
			public void invalidated( Observable arg0 )
			{
				if( followState.getValue() && !isDisabled() )
				{
					setToLeftSide();
				}

			}
		} );

		//when checkbox is pressed
		followState.addListener( new InvalidationListener()
		{

			@Override
			public void invalidated( Observable arg0 )
			{

				// set to true
				if( followState.getValue() )
				{
					setToLeftSide();
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
