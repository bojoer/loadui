package com.eviware.loadui.ui.fx.control.fields;

import javafx.scene.control.TextFieldBuilder;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class ValidatableDoubleField extends ValidatableTextField<Double>
{
	public final static Function<String, Double> STRING_TO_DOUBLE = new Function<String, Double>()
	{
		@Override
		public Double apply( @Nullable String input )
		{
			return Double.parseDouble( input );
		}
	};

	public final static Predicate<String> CONVERTABLE_TO_DOUBLE = new Predicate<String>()
	{
		@Override
		public boolean apply( @Nullable String input )
		{
			try
			{
				STRING_TO_DOUBLE.apply( input );
				return true;
			}
			catch( NumberFormatException e )
			{
				return false;
			}
		}
	};

	private final Predicate<Double> constraint;

	public ValidatableDoubleField( final Predicate<String> stringConstraint, final Predicate<Double> doubleConstraint,
			final Function<String, Double> convertFunction )
	{
		super( stringConstraint == null ? CONVERTABLE_TO_DOUBLE : Predicates
				.and( CONVERTABLE_TO_DOUBLE, stringConstraint ), Objects.firstNonNull( convertFunction, STRING_TO_DOUBLE ) );

		constraint = Objects.firstNonNull( doubleConstraint, Predicates.<Double> alwaysTrue() );
	}

	@Override
	public boolean validate()
	{
		if( !super.validate() )
			return false;
		if( constraint.apply( convert.apply( getText() ) ) )
		{
			ValidatableFieldSupport.setValid( this );
			return true;
		}
		ValidatableFieldSupport.setInvalid( this );
		return false;
	}

	public static class Builder<B extends Builder<B>> extends TextFieldBuilder<Builder<B>>
	{
		private Function<String, Double> convertFunction;
		private Predicate<String> stringConstraint;
		private Predicate<Double> longConstraint;

		private Builder()
		{
		}

		@SuppressWarnings( "rawtypes" )
		public static Builder<?> create()
		{
			return new Builder();
		}

		public Builder<B> convertFunction( Function<String, Double> function )
		{
			this.convertFunction = function;
			return this;
		}

		public Builder<B> stringConstraint( Predicate<String> constraint )
		{
			this.stringConstraint = constraint;
			return this;
		}

		public Builder<B> longConstraint( Predicate<Double> constraint )
		{
			this.longConstraint = constraint;
			return this;
		}

		public void applyTo( ValidatableDoubleField field )
		{
			//This is cheating, we don't actually support applyTo for the ValidatableDoubleField fields as they are final.
			super.applyTo( field );
		}

		@Override
		public ValidatableDoubleField build()
		{
			ValidatableDoubleField field = new ValidatableDoubleField( stringConstraint, longConstraint, convertFunction );
			applyTo( field );
			return field;
		}
	}
}
