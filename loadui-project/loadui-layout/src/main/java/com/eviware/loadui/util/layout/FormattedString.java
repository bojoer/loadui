/*
 * Copyright 2010 eviware software ab
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.util.layout;

import java.util.Observable;

import com.eviware.loadui.api.serialization.Value;

public class FormattedString extends Observable
{
	private String format;
	private Object[] args = new Object[0];
	private String value;

	public FormattedString( String pattern, Object... args )
	{
		this.format = pattern;
		this.args = args;

		update();
	}

	public void setFormat( String pattern )
	{
		this.format = pattern;
	}

	public void setArgs( Object... args )
	{
		this.args = args;
	}

	public void update()
	{
		Object[] values = new Object[args.length];
		for( int i = args.length - 1; i >= 0; i-- )
		{
			Object arg = args[i];
			if( arg instanceof Value<?> )
				values[i] = ( ( Value<?> )arg ).getValue();
			else
				values[i] = arg;
		}

		setValue( String.format( format, values ) );
	}

	protected void setValue( String newValue )
	{
		if( value != null && value.equals( newValue ) )
			return;

		if( value == null && newValue == null )
			return;

		value = newValue;
		setChanged();
		notifyObservers( value );
	}

	public Object[] getArgs()
	{
		return args;
	}

	public String getFormat()
	{
		return format;
	}

	public String getCurrentValue()
	{
		return value;
	}

	public void release()
	{
		args = new Object[] {};
	}

	@Override
	public String toString()
	{
		return value;
	}
}
