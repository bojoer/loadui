package com.eviware.loadui.util.test;

import java.util.Collections;

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

		assertTrue( isAMock( one ) );
		assertTrue( isAMock( two ) );
	}

	@Test
	public void shouldReturnMappedBeanAddedInConstructor()
	{
		final TestInterface testBean = new TestInterface()
		{
		};

		new BeanInjectorMocker( Collections.<Class<?>, Object> singletonMap( TestInterface.class, testBean ) );

		Object one = BeanInjector.getBean( TestInterface.class );
		Object two = BeanInjector.getBean( TestInterface.class );

		assertThat( one, is( TestInterface.class ) );
		assertThat( two, is( TestInterface.class ) );

		assertSame( testBean, one );
		assertSame( testBean, two );

		assertFalse( isAMock( one ) );
		assertFalse( isAMock( two ) );
	}

	public boolean isAMock( Object mock )
	{
		try
		{
			verifyZeroInteractions( mock );
		}
		catch( NotAMockException e )
		{
			return false;
		}
		return true;
	}

	@Test
	public void shouldReturnMappedBeanAddedByPut()
	{
		final TestInterface testBean = new TestInterface()
		{
		};

		new BeanInjectorMocker().put( TestInterface.class, testBean );

		Object one = BeanInjector.getBean( TestInterface.class );
		Object two = BeanInjector.getBean( TestInterface.class );

		assertThat( one, is( TestInterface.class ) );
		assertThat( two, is( TestInterface.class ) );

		assertSame( testBean, one );
		assertSame( testBean, two );
	}

	public interface TestInterface
	{

	}
}
