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