package com.eviware.loadui.util.test;

import java.util.Collections;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.*;
import org.mockito.exceptions.misusing.NotAMockException;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import com.eviware.loadui.util.BeanInjector;

public class BeanInjectorMockerTest
{
	@Test
	public void shouldReturnMocks()
	{
		new BeanInjectorMocker();

		Object one = BeanInjector.getBean( TestInterface.class );
		Object two = BeanInjector.getBean( TestInterface.class );

		assertThat( one, is( TestInterface.class ) );
		assertThat( two, is( TestInterface.class ) );

		assertSame( one, two );

		assertThat( one, isMock() );
		assertThat( two, isMock() );
	}

	@Test
	public void shouldReturnMappedBeanAddedInConstructor()
	{
		final TestInterface testBean = new TestInterface()
		{
		};

		new BeanInjectorMocker( Collections.<Class<?>, Object> singletonMap( TestInterface.class, testBean ) );

		TestInterface one = BeanInjector.getBean( TestInterface.class );
		TestInterface two = BeanInjector.getBean( TestInterface.class );

		assertThat( one, instanceOf( TestInterface.class ) );
		assertThat( two, instanceOf( TestInterface.class ) );

		assertThat( one, sameInstance( testBean ) );
		assertThat( two, sameInstance( testBean ) );

		assertThat( one, isNotMock() );
		assertThat( two, isNotMock() );
	}

	@Test
	public void shouldReturnMappedBeanAddedByPut()
	{
		final TestInterface testBean = new TestInterface()
		{
		};

		new BeanInjectorMocker().put( TestInterface.class, testBean );

		TestInterface one = BeanInjector.getBean( TestInterface.class );
		TestInterface two = BeanInjector.getBean( TestInterface.class );

		assertThat( one, instanceOf( TestInterface.class ) );
		assertThat( two, instanceOf( TestInterface.class ) );

		assertThat( one, sameInstance( testBean ) );
		assertThat( two, sameInstance( testBean ) );
	}

	public interface TestInterface
	{
	}

	public static <T> Matcher<T> isMock()
	{
		return new IsMock<T>();
	}

	public static <T> Matcher<T> isNotMock()
	{
		return not( new IsMock<T>() );
	}

	/**
	 * Checks if an Object is a mock or not. Limited by the fact that it checks
	 * that no interactions have been invoked on the mock, meaning it needs to be
	 * called before invoking any method on the mock to function.
	 * 
	 * @author dain.nilsson
	 * 
	 * @param <T>
	 */
	public static class IsMock<T> extends BaseMatcher<T>
	{
		public boolean matches( Object item )
		{
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
			description.appendText( "mock" );
		}

	}
}
