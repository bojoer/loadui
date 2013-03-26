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
package com.eviware.loadui.util.test.matchers;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.mockito.Mockito;

/**
 * Checks if an Object is a mock or not.
 * 
 * @author dain.nilsson
 * 
 * @param <T>
 */
public class IsMock<T> extends BaseMatcher<T>
{
	@Override
	public boolean matches( Object item )
	{
		assertThat( item, notNullValue() );

		return Mockito.mockingDetails( item ).isMock();
	}

	@Override
	public void describeTo( Description description )
	{
		description.appendText( "is a mock" );
	}
}
