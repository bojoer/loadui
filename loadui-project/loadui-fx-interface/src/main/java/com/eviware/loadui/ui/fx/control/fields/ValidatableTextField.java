/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
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
