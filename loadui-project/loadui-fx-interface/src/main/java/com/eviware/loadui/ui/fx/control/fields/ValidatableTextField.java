package com.eviware.loadui.ui.fx.control.fields;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextField;

import javax.annotation.Nonnull;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

public class ValidatableTextField<T> extends TextField implements Field<T>
{
	private final Predicate<String> contraint;
	private final BooleanProperty isValidProperty = new SimpleBooleanProperty( false );
	protected final Function<String, T> convert;

	public ValidatableTextField( @Nonnull Predicate<String> contraint, @Nonnull Function<String, T> convert, String text )
	{
		super( text );
		this.contraint = contraint;
		this.convert = convert;

		textProperty().addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				isValidProperty.set( validate() );
			}
		} );
	}

	public ValidatableTextField( @Nonnull Predicate<String> contraint, @Nonnull Function<String, T> convert )
	{
		this( contraint, convert, "" );
	}

	protected boolean validate()
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
	public T getFieldValue()
	{
		return convert.apply( getText() );
	}

	@Override
	public ReadOnlyBooleanProperty isValidProperty()
	{
		return isValidProperty;
	}

	@Override
	public boolean isValid()
	{
		return isValidProperty().get();
	}
}
