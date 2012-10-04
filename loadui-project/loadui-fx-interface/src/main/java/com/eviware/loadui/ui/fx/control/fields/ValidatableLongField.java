package com.eviware.loadui.ui.fx.control.fields;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class ValidatableLongField extends ValidatableTextField<Long>
{
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

	private final Predicate<Long> constraint;

	public ValidatableLongField()
	{
		super( CONVERTABLE_TO_LONG, STRING_TO_LONG );
		constraint = Predicates.alwaysTrue();
	}

	public ValidatableLongField( String text )
	{
		super( CONVERTABLE_TO_LONG, STRING_TO_LONG, text );
		constraint = Predicates.alwaysTrue();
	}

	public ValidatableLongField( Function<String, Long> convertFunction, String text )
	{
		super( CONVERTABLE_TO_LONG, convertFunction, text );
		constraint = Predicates.alwaysTrue();
	}

	public ValidatableLongField( Predicate<Long> constraint )
	{
		super( CONVERTABLE_TO_LONG, STRING_TO_LONG );
		this.constraint = constraint;
	}

	public ValidatableLongField( Predicate<Long> constraint, String text )
	{
		super( CONVERTABLE_TO_LONG, STRING_TO_LONG, text );
		this.constraint = constraint;
	}

	@Override
	public boolean validate()
	{
		if( !super.validate() )
			return false;
		if( constraint.apply( STRING_TO_LONG.apply( getText() ) ) )
		{
			ValidatableFieldSupport.setValid( this );
			return true;
		}
		ValidatableFieldSupport.setInvalid( this );
		return false;
	}
}
