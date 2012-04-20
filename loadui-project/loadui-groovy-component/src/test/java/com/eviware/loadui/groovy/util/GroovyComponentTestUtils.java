package com.eviware.loadui.groovy.util;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.eviware.loadui.api.component.BehaviorProvider;
import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.groovy.GroovyBehaviorProvider;
import com.eviware.loadui.impl.model.ComponentItemImpl;
import com.eviware.loadui.util.component.ComponentTestUtils;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class GroovyComponentTestUtils extends ComponentTestUtils
{
	private static final Object lock = new Object();
	private static final ConcurrentMap<ComponentDescriptor, BehaviorProvider> descriptors = Maps.newConcurrentMap();
	private static final ComponentRegistry registry = mock( ComponentRegistry.class );

	static
	{
		System.setProperty( "groovy.root", "target" + File.separator + ".groovy" );

		doAnswer( new Answer<Void>()
		{
			@Override
			public Void answer( InvocationOnMock invocation ) throws Throwable
			{
				ComponentDescriptor descriptor = ( ComponentDescriptor )invocation.getArguments()[0];
				BehaviorProvider provider = ( BehaviorProvider )invocation.getArguments()[1];
				synchronized( lock )
				{
					descriptors.put( descriptor, provider );
					lock.notifyAll();
				}

				return null;
			}
		} ).when( registry ).registerDescriptor( any( ComponentDescriptor.class ), any( BehaviorProvider.class ) );
	}

	public static void initialize( String pathToComponentScripts )
	{
		new GroovyBehaviorProvider( registry, Executors.newSingleThreadScheduledExecutor(), new File(
				pathToComponentScripts ) );
	}

	public static ComponentItem createComponent( final String componentName ) throws ComponentCreationException
	{
		return createComponent( componentName, ComponentTestUtils.createComponentItem() );
	}

	public static ComponentItem createComponent( final String componentName, ComponentItemImpl component )
			throws ComponentCreationException
	{
		Optional<ComponentDescriptor> descriptorOptional = null;
		Predicate<ComponentDescriptor> predicate = new Predicate<ComponentDescriptor>()
		{
			@Override
			public boolean apply( ComponentDescriptor input )
			{
				return Objects.equal( componentName, input.getLabel() );
			}
		};

		long deadline = System.currentTimeMillis() + 5000;
		synchronized( lock )
		{
			while( !( descriptorOptional = Iterables.tryFind( descriptors.keySet(), predicate ) ).isPresent()
					&& System.currentTimeMillis() < deadline )
			{
				try
				{
					lock.wait( deadline - System.currentTimeMillis() );
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}
			}
		}

		ComponentDescriptor descriptor = descriptorOptional.get();

		component.setAttribute( ComponentItem.TYPE, descriptor.getLabel() );
		component.setBehavior( descriptors.get( descriptor ).createBehavior( descriptor, component.getContext() ) );

		return component;
	}
}
