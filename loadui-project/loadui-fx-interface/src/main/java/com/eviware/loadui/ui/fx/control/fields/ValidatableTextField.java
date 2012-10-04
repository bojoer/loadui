package com.eviware.loadui.ui.fx.control.fields;

import javax.annotation.Nonnull;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import javafx.scene.control.TextField;

public class ValidatableTextField<T> extends TextField implements Field.Validatable<T>
{
	private final Predicate<String> contraint;
	private final Function<String, T> convert;

	public ValidatableTextField( @Nonnull Predicate<String> contraint, @Nonnull Function<String, T> convert, String text )
	{
		super( text );
		this.contraint = contraint;
		this.convert = convert;
	}

	public ValidatableTextField( @Nonnull Predicate<String> contraint, @Nonnull Function<String, T> convert )
	{
		this( contraint, convert, "" );
	}

	@Override
	public boolean validate()
	{
		if( contraint.apply( getText() ) )
		{
			ValidatableFieldSupport.setValid( this );
			return true;
		}
		ValidatableFieldSupport.setInvalid( this );
		return false;
	}

	@Override
	public T getValue()
	{
		return convert.apply( getText() );
	}
}
