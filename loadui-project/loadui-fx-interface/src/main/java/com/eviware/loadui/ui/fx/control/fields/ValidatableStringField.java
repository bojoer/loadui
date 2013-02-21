package com.eviware.loadui.ui.fx.control.fields;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;

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

	public static class Builder<B extends Builder<B>> extends TextFieldBuilder<Builder<B>>
	{
		private Predicate<String> stringConstraint;

		private Builder()
		{
		}

		@SuppressWarnings( "rawtypes" )
		public static Builder<?> create()
		{
			return new Builder();
		}

		public Builder<B> stringConstraint( Predicate<String> constraint )
		{
			this.stringConstraint = constraint;
			return this;
		}

		@SuppressWarnings( "cast" )
		public void applyTo( ValidatableStringField field )
		{
			super.applyTo( ( TextField )field );
		}

		@Override
		public ValidatableStringField build()
		{
			ValidatableStringField field = new ValidatableStringField( stringConstraint );
			applyTo( field );
			return field;
		}
	}
}
