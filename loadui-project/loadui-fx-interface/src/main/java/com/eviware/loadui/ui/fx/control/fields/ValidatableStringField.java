package com.eviware.loadui.ui.fx.control.fields;

import javax.annotation.Nullable;

import com.google.common.base.Functions;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class ValidatableStringField extends ValidatableTextField<String>
{
	public static final Predicate<String> NOT_EMPTY = new Predicate<String>()
	{
		@Override
		public boolean apply( @Nullable String input )
		{
			return !Objects.equal( input, "" );
		}

	};

	public ValidatableStringField()
	{
		super( Predicates.<String> alwaysTrue(), Functions.<String> identity() );
	}

	public ValidatableStringField( Predicate<String> constraint )
	{
		super( constraint, Functions.<String> identity() );
	}
}
