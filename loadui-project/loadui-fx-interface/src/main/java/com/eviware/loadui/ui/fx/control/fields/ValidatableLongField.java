package com.eviware.loadui.ui.fx.control.fields;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class ValidatableLongField extends ValidatableTextField<Long>
{
	protected static final Logger log = LoggerFactory.getLogger( ValidatableLongField.class );

	public final static Function<String, Long> STRING_TO_LONG = new Function<String, Long>()
	{
		@Override
		@Nullable
		public Long apply( @Nullable String input )
		{
			return Long.parseLong( input );
		}
	};

	public final static Function<String, Long> EMPTY_TO_NEGATIVE_ONE = new Function<String, Long>()
	{
		@Override
		@Nullable
		public Long apply( @Nullable String input )
		{
			if( input.isEmpty() )
				return -1L;
			return Long.parseLong( input );
		}
	};

	public final static Predicate<String> CONVERTABLE_TO_LONG = new Predicate<String>()
	{
		@Override
		public boolean apply( @Nullable String input )
		{
			try
			{
				STRING_TO_LONG.apply( input );
				return true;
			}
			catch( NumberFormatException e )
			{
				return false;
			}
		}
	};

	public final static Predicate<String> IS_EMPTY = new Predicate<String>()
	{
		@Override
		public boolean apply( @Nullable String input )
		{
			return input.isEmpty();
		}
	};

	private final Predicate<Long> constraint;

	private ValidatableLongField( final Predicate<String> stringConstraint, final Predicate<Long> longConstraint,
			final Function<String, Long> convertFunction )
	{
		super( Objects.firstNonNull( stringConstraint, CONVERTABLE_TO_LONG ), Objects.firstNonNull( convertFunction,
				STRING_TO_LONG ) );
		this.constraint = Objects.firstNonNull( longConstraint, Predicates.<Long> alwaysTrue() );
		setMaxWidth( 55 );
	}

	@Override
	public boolean validate()
	{
		if( !super.validate() )
			return false;
		log.debug( "constraint2: " + constraint );
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
		private Function<String, Long> convertFunction;
		private Predicate<String> stringConstraint;
		private Predicate<Long> longConstraint;

		private Builder()
		{
		}

		@SuppressWarnings( "rawtypes" )
		public static Builder<?> create()
		{
			return new Builder();
		}

		public Builder<B> convertFunction( Function<String, Long> function )
		{
			this.convertFunction = function;
			return this;
		}

		public Builder<B> stringConstraint( Predicate<String> constraint )
		{
			this.stringConstraint = constraint;
			return this;
		}

		public Builder<B> longConstraint( Predicate<Long> constraint )
		{
			this.longConstraint = constraint;
			return this;
		}

		@SuppressWarnings( "cast" )
		public void applyTo( ValidatableLongField field )
		{
			super.applyTo( ( TextField )field );
		}

		@Override
		public ValidatableLongField build()
		{
			ValidatableLongField field = new ValidatableLongField( stringConstraint, longConstraint, convertFunction );
			applyTo( field );
			return field;
		}
	}
}
