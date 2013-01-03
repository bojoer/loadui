package com.eviware.loadui.ui.fx.views.analysis;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.ScrollBar;

public class ChartScrollBar extends ScrollBar
{
	private DoubleProperty leftPositionProperty = new SimpleDoubleProperty( 0d );

	public ChartScrollBar()
	{
		super();

		valueProperty().addListener( new InvalidationListener()
		{

			@Override
			public void invalidated( Observable arg0 )
			{
				double lenght = getMax() - getMin();
				double percentage = getValue() / lenght;
				double leftside = getValue() - ( getVisibleAmount() * percentage );

				leftPositionProperty.set( leftside );
			}
		} );

	}

	public ReadOnlyDoubleProperty leftSidePositionProperty()
	{
		return leftPositionProperty;
	}

	public void setLeftSidePosition( double position )
	{
		if( position + getVisibleAmount() > getMax() )
		{
			valueProperty().set( 0d );
		}
		else
		{
			double lenght = getMax() - getMin();
			double value = -lenght * position / ( getVisibleAmount() - lenght );

			valueProperty().set( value );
		}

	}
}
