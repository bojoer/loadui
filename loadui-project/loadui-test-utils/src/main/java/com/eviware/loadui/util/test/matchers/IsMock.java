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
package com.eviware.loadui.util.test.matchers;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.mockito.exceptions.misusing.NotAMockException;

/**
 * Checks if an Object is a mock or not. Limited by the fact that it checks that
 * no interactions have been invoked on the mock, meaning it needs to be called
 * before invoking any method on the mock to function.
 * 
 * @author dain.nilsson
 * 
 * @param <T>
 */
public class IsMock<T> extends BaseMatcher<T>
{
	public boolean matches( Object item )
	{
		assertThat( item, notNullValue() );

		try
		{
			verifyZeroInteractions( item );
		}
		catch( NotAMockException e )
		{
			return false;
		}

		return true;
	}

	public void describeTo( Description description )
	{
		description.appendText( "is a mock" );
	}
}