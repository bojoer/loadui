/* 
 * Copyright 2011 SmartBear Software
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
package com.eviware.loadui.impl.assertion;

import java.io.IOException;

import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.util.serialization.SerializationUtils;
import com.eviware.loadui.util.testevents.AbstractTestEvent;

public class AssertionFailureEvent extends AbstractTestEvent
{
	private final AssertionItem<?> assertionItem;

	private final String assertionLabel;
	private final String constraintString;
	private final String valueString;

	public AssertionFailureEvent( long timestamp, AssertionItem<?> assertionItem, Object value )
	{
		super( timestamp );

		this.assertionItem = assertionItem;

		assertionLabel = assertionItem.getLabel();
		constraintString = String.valueOf( assertionItem.getConstraint() );
		valueString = String.valueOf( value );
	}

	public AssertionFailureEvent( long timestamp, byte[] sourceData, String valueString )
	{
		super( timestamp );

		//TODO: Needed from source data: assertionItem address, assertion label, constraint string.
		assertionItem = null;
		assertionLabel = null;
		constraintString = null;

		this.valueString = valueString;
	}

	public AssertionItem<?> getAssertion()
	{
		return assertionItem;
	}

	@Override
	public String toString()
	{
		return String.format( "(%s) Asserted value: %s did not meet Constraint: %s", assertionLabel, valueString,
				constraintString );
	}

	public static final class Factory extends AbstractTestEvent.Factory<AssertionFailureEvent>
	{
		public Factory()
		{
			super( AssertionFailureEvent.class );
		}

		@Override
		public AssertionFailureEvent createTestEvent( long timestamp, byte[] sourceData, byte[] entryData )
		{
			try
			{
				return new AssertionFailureEvent( timestamp, sourceData,
						( String )SerializationUtils.deserialize( entryData ) );
			}
			catch( ClassNotFoundException e )
			{
				return new AssertionFailureEvent( timestamp, sourceData, e.getMessage() );
			}
			catch( IOException e )
			{
				return new AssertionFailureEvent( timestamp, sourceData, e.getMessage() );
			}
		}

		@Override
		public byte[] getDataForTestEvent( AssertionFailureEvent testEvent )
		{
			try
			{
				return SerializationUtils.serialize( testEvent.valueString );
			}
			catch( IOException e )
			{
				return null;
			}
		}
	}
}
