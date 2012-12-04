package com.eviware.loadui.ui.fx.control;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;

import com.sun.javafx.Utils;

public class Knob extends Control
{
	private static final String DEFAULT_STYLE_CLASS = "knob";

	private final DoubleProperty value = new SimpleDoubleProperty( this, "value", 0 )
	{
		@Override
		protected void invalidated()
		{
			adjustValues();
		}
	};

	public Knob()
	{
		initialize();
	}

	public Knob( String text )
	{
		initialize();
		setText( text );
	}

	public Knob( String text, double min, double max, double value )
	{
		initialize();
		setText( text );
		setMax( max );
		setMin( min );
		setValue( value );
		adjustValues();
	}

	private void initialize()
	{
		getStyleClass().setAll( DEFAULT_STYLE_CLASS );
	}

	public final StringProperty textProperty()
	{
		if( text == null )
		{
			text = new SimpleStringProperty( this, "text", "" );
		}
		return text;
	}

	private StringProperty text;

	public final void setText( String value )
	{
		textProperty().setValue( value );
	}

	public final String getText()
	{
		return text == null ? "" : text.getValue();
	}

	public DoubleProperty valueProperty()
	{
		return value;
	}

	public double getValue()
	{
		return value.get();
	}

	public void setValue( double value )
	{
		valueProperty().set( value );
	}

	private DoubleProperty min;

	public DoubleProperty minProperty()
	{
		if( min == null )
		{
			min = new SimpleDoubleProperty( this, "min", getMin() )
			{
				@Override
				protected void invalidated()
				{
					if( get() > getMax() )
					{
						setMax( get() );
					}
					adjustValues();
				}
			};
		}
		return min;
	}

	public double getMin()
	{
		return min == null ? Double.NEGATIVE_INFINITY : min.get();
	}

	public void setMin( double value )
	{
		minProperty().set( value );
		bounded.set( !( Double.isInfinite( getMax() ) || Double.isInfinite( getMin() ) ) );
	}

	private DoubleProperty max;

	public DoubleProperty maxProperty()
	{
		if( max == null )
		{
			max = new SimpleDoubleProperty( this, "max", getMax() )
			{
				@Override
				protected void invalidated()
				{
					if( get() < getMin() )
					{
						setMin( get() );
					}
					adjustValues();
				}
			};
		}
		return max;
	}

	public double getMax()
	{
		return max == null ? Double.POSITIVE_INFINITY : max.get();
	}

	public void setMax( double value )
	{
		maxProperty().set( value );
		bounded.set( !( Double.isInfinite( getMax() ) || Double.isInfinite( getMin() ) ) );
	}

	private DoubleProperty step;

	public DoubleProperty stepProperty()
	{
		if( step == null )
		{
			step = new SimpleDoubleProperty( this, "step", getStep() );
		}
		return step;
	}

	public double getStep()
	{
		return step == null ? 1 : step.get();
	}

	public void setStep( double value )
	{
		stepProperty().set( value );
	}

	private DoubleProperty span;

	public DoubleProperty spanProperty()
	{
		if( span == null )
		{
			span = new SimpleDoubleProperty( this, "span", getSpan() );
		}
		return span;
	}

	private double getSpanImpl()
	{
		return span == null ? Double.NaN : span.get();
	}

	public double getSpan()
	{
		double spanValue = getSpanImpl();
		return Double.isNaN( spanValue ) ? ( isBounded() ? getMax() - getMin() + 1 : 100 ) : spanValue;
	}

	public void setSpan( double value )
	{
		spanProperty().set( value );
	}

	private final ReadOnlyBooleanWrapper bounded = new ReadOnlyBooleanWrapper( this, "bounded", false )
	{
		@Override
		protected void invalidated()
		{
			if( isBounded() )
			{
				getStyleClass().add( "bounded" );
			}
			else
			{
				getStyleClass().remove( "bounded" );
			}
		}
	};

	public ReadOnlyBooleanProperty boundedProperty()
	{
		return bounded.getReadOnlyProperty();
	}

	public boolean isBounded()
	{
		return bounded.get();
	}

	private void adjustValues()
	{
		if( ( getValue() < getMin() || getValue() > getMax() ) )
		{
			setValue( Utils.clamp( getMin(), getValue(), getMax() ) );
		}
	}
}
