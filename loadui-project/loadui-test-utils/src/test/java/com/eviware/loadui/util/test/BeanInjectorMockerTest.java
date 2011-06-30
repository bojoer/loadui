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
	/**
	 * Dummy interface to create mocks for.
	 * 
	 * @author dain.nilsson
	 */
	private interface TestInterface
	{
	}

	/**
	 * Subclass of TestInterface, also used for testing.
	 * 
	 * @author dain.nilsson
	 */
	private interface TestInterfaceSubclass extends TestInterface
	{
	}

	@Test
	@SuppressWarnings( "unchecked" )
	public void shouldReturnMocks()
	{
		new BeanInjectorMocker();

		TestInterface beanOne = BeanInjector.getBean( TestInterface.class );
		TestInterface beanTwo = BeanInjector.getBean( TestInterface.class );

		assertThat( beanOne, mockObject() );
		assertThat( beanOne, instanceOf( TestInterface.class ) );

		assertThat( beanTwo, allOf( mockObject(), is( TestInterface.class ) ) );

		assertThat( beanOne, sameInstance( beanTwo ) );
	}

	@Test
	@SuppressWarnings( "unchecked" )
	public void shouldReturnMappedBeanAddedInConstructor()
	{
		final TestInterface testBean = new TestInterface()
		{
		};

		new BeanInjectorMocker( Collections.<Class<?>, Object> singletonMap( TestInterface.class, testBean ) );

		TestInterface beanOne = BeanInjector.getBean( TestInterface.class );
		TestInterface beanTwo = BeanInjector.getBean( TestInterface.class );

		assertThat( beanOne, is( notMockObject() ) );
		assertThat( beanOne, is( instanceOf( TestInterface.class ) ) );
		assertThat( beanOne, is( sameInstance( testBean ) ) );

		assertThat( beanTwo, allOf( notMockObject(), is( TestInterface.class ), sameInstance( testBean ) ) );
	}

	@Test
	@SuppressWarnings( "unchecked" )
	public void shouldReturnMappedBeanAddedByPut()
	{
		final TestInterface testBean = new TestInterface()
		{
		};

		new BeanInjectorMocker().put( TestInterface.class, testBean );

		TestInterface beanOne = BeanInjector.getBean( TestInterface.class );
		TestInterface beanTwo = BeanInjector.getBean( TestInterface.class );

		assertThat( beanOne, notMockObject() );
		assertThat( beanOne, is( TestInterface.class ) );
		assertThat( beanOne, sameInstance( testBean ) );

		assertThat( beanTwo, allOf( notMockObject(), is( TestInterface.class ), sameInstance( testBean ) ) );
	}

	@Test
	public void shouldNotReturnMappedBeanForSuperClass()
	{
		final TestInterfaceSubclass testSubclassBean = new TestInterfaceSubclass()
		{
		};

		new BeanInjectorMocker().put( TestInterfaceSubclass.class, testSubclassBean );

		TestInterface bean = BeanInjector.getBean( TestInterface.class );
		TestInterfaceSubclass subclassBean = BeanInjector.getBean( TestInterfaceSubclass.class );

		assertThat( bean, not( sameInstance( ( TestInterface )testSubclassBean ) ) );
		assertThat( bean, mockObject() );

		assertThat( subclassBean, sameInstance( testSubclassBean ) );
	}

	/**
	 * Verified that the object to match is a mock.
	 * 
	 * @return
	 */
	public static <T> Matcher<T> mockObject()
	{
		return new IsMock<T>();
	}

	/**
	 * Verified that the object to match is not a mock.
	 * 
	 * @return
	 */
	public static <T> Matcher<T> notMockObject()
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
	private static class IsMock<T> extends BaseMatcher<T>
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
}
