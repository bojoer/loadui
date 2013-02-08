package com.eviware.loadui.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;

public class BeanInjectorTest
{

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	@Test
	public void testGetBeanBeforeServiceIsPublished() throws InterruptedException
	{
		final A aService = new AImpl();

		ServiceReference aServRef = mock( ServiceReference.class );
		when( aServRef.getProperty( "objectClass" ) ).thenReturn( new String[] { A.class.getName() } );

		BundleContext ctx = mock( BundleContext.class );
		when( ctx.getService( aServRef ) ).thenReturn( aService );

		BeanInjector.defaultTimeout = 75;
		BeanInjector.setBundleContext( ctx );

		final ServiceEvent mockEvent = mock( ServiceEvent.class );
		when( mockEvent.getType() ).thenReturn( ServiceEvent.REGISTERED );
		when( mockEvent.getServiceReference() ).thenReturn( ( ServiceReference )aServRef );

		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Thread.sleep( 50 );
				}
				catch( InterruptedException e )
				{
				}
				BeanInjector.serviceListener.serviceChanged( mockEvent );
			}
		} ).start();

		assertEquals( aService, BeanInjector.getBean( A.class ) );

	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	@Test
	public void testGetBeanAfterServiceIsPublished() throws InterruptedException
	{
		final A aService = new AImpl();

		ServiceReference aServRef = mock( ServiceReference.class );
		when( aServRef.getProperty( "objectClass" ) ).thenReturn( new String[] { A.class.getName() } );

		BundleContext ctx = mock( BundleContext.class );
		when( ctx.getService( aServRef ) ).thenReturn( aService );

		BeanInjector.defaultTimeout = 75;
		BeanInjector.setBundleContext( ctx );

		final ServiceEvent mockEvent = mock( ServiceEvent.class );
		when( mockEvent.getType() ).thenReturn( ServiceEvent.REGISTERED );
		when( mockEvent.getServiceReference() ).thenReturn( ( ServiceReference )aServRef );

		BeanInjector.serviceListener.serviceChanged( mockEvent );

		assertEquals( aService, BeanInjector.getBean( A.class ) );

	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	@Test
	public void testGetBeanAfterSeveralServicesPublished() throws InterruptedException
	{
		BeanInjector.reset();
		final A aService = new AImpl();
		final A a2Service = new AImpl();
		final B bService = new BImpl();

		ServiceReference aServRef = mock( ServiceReference.class );
		when( aServRef.getProperty( "objectClass" ) ).thenReturn( new String[] { A.class.getName() } );
		ServiceReference a2ServRef = mock( ServiceReference.class );
		when( a2ServRef.getProperty( "objectClass" ) ).thenReturn( new String[] { A.class.getName() } );
		ServiceReference bServRef = mock( ServiceReference.class );
		when( bServRef.getProperty( "objectClass" ) ).thenReturn( new String[] { B.class.getName() } );

		BundleContext ctx = mock( BundleContext.class );
		when( ctx.getService( aServRef ) ).thenReturn( aService );
		when( ctx.getService( bServRef ) ).thenReturn( bService );
		when( ctx.getService( a2ServRef ) ).thenReturn( a2Service );

		BeanInjector.defaultTimeout = 75;
		BeanInjector.setBundleContext( ctx );

		final ServiceEvent mockEvent = mock( ServiceEvent.class );
		when( mockEvent.getType() ).thenReturn( ServiceEvent.REGISTERED );
		when( mockEvent.getServiceReference() ).thenReturn( ( ServiceReference )aServRef );

		final ServiceEvent mockEvent2 = mock( ServiceEvent.class );
		when( mockEvent2.getType() ).thenReturn( ServiceEvent.REGISTERED );
		when( mockEvent2.getServiceReference() ).thenReturn( ( ServiceReference )bServRef );

		final ServiceEvent mockEvent3 = mock( ServiceEvent.class );
		when( mockEvent3.getType() ).thenReturn( ServiceEvent.REGISTERED );
		when( mockEvent3.getServiceReference() ).thenReturn( ( ServiceReference )a2ServRef );

		final List<ServiceEvent> events = Arrays.asList( mockEvent, mockEvent2, mockEvent3 );

		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Thread.sleep( 50 );
				}
				catch( InterruptedException e )
				{
				}
				for( ServiceEvent event : events )
					BeanInjector.serviceListener.serviceChanged( event );
			}
		} ).start();

		assertEquals( aService, BeanInjector.getBean( A.class ) ); // when first service is registered, the bean is returned immediately
		assertEquals( aService, BeanInjector.getBean( A.class ) ); // again
		assertEquals( aService, BeanInjector.getBean( A.class ) ); // and again
		assertEquals( bService, BeanInjector.getBean( B.class ) );

		Thread.sleep( 50 );
		assertEquals( a2Service, BeanInjector.getBean( A.class ) ); // by now, first service was replaced by second service
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	@Test( expected = RuntimeException.class )
	public void testUnregisterAfterServiceIsPublished() throws InterruptedException
	{
		final A aService = new AImpl();

		ServiceReference aServRef = mock( ServiceReference.class );
		when( aServRef.getProperty( "objectClass" ) ).thenReturn( new String[] { A.class.getName() } );

		BundleContext ctx = mock( BundleContext.class );
		when( ctx.getService( aServRef ) ).thenReturn( aService );

		BeanInjector.defaultTimeout = 75;
		BeanInjector.setBundleContext( ctx );

		final ServiceEvent mockEvent = mock( ServiceEvent.class );
		when( mockEvent.getType() ).thenReturn( ServiceEvent.REGISTERED );
		when( mockEvent.getServiceReference() ).thenReturn( ( ServiceReference )aServRef );

		final ServiceEvent unregisterEvent = mock( ServiceEvent.class );
		when( unregisterEvent.getType() ).thenReturn( ServiceEvent.UNREGISTERING );
		when( unregisterEvent.getServiceReference() ).thenReturn( ( ServiceReference )aServRef );

		BeanInjector.serviceListener.serviceChanged( mockEvent );

		assertEquals( aService, BeanInjector.getBean( A.class ) );

		BeanInjector.serviceListener.serviceChanged( unregisterEvent );

		BeanInjector.getBean( A.class ); // timeout (wrapped in RuntimeException) as service is not registered anymore

	}

	public interface A
	{
	}

	public class AImpl implements A
	{
	}

	public interface B
	{
	}

	public class BImpl implements B
	{
	}

}
