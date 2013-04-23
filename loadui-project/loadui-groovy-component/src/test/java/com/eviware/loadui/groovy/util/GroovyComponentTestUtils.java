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
package com.eviware.loadui.groovy.util;

import com.eviware.loadui.api.component.*;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticVariable.Mutable;
import com.eviware.loadui.groovy.GroovyBehaviorProvider;
import com.eviware.loadui.groovy.GroovyBehaviorSupport;
import com.eviware.loadui.impl.model.ComponentItemImpl;
import com.eviware.loadui.util.component.ComponentTestUtils;
import com.eviware.loadui.util.groovy.ClassLoaderRegistry;
import com.eviware.loadui.util.groovy.GroovyEnvironment;
import com.eviware.loadui.util.groovy.GroovyEnvironmentClassLoader;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;

import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class GroovyComponentTestUtils extends ComponentTestUtils
{
	private static final Object lock = new Object();
	private static final ConcurrentMap<ComponentDescriptor, BehaviorProvider> descriptors = Maps.newConcurrentMap();
	private static final ComponentRegistry registry = mock( ComponentRegistry.class );

	static
	{
		System.setProperty( "groovy.root", new File( "target", ".groovy" ).getAbsolutePath() );

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
				pathToComponentScripts ), new ClassLoaderRegistry()
		{
			@Override
			protected GroovyEnvironmentClassLoader provideClassLoader( ClassLoader bundleClassLoader )
			{
				return new GroovyEnvironmentClassLoader( bundleClassLoader, new File( "target", ".groovy" ) );
			}
		} );
	}

	@SuppressWarnings( "unchecked" )
	public static ComponentItem createComponent( final String componentName ) throws ComponentCreationException
	{
		ComponentItem component = ComponentTestUtils.createComponentItem();
		ComponentItem componentSpy = mock( ComponentItem.class, delegatesTo( component ) );
		ComponentContext contextSpy = mock( ComponentContext.class, delegatesTo( componentSpy.getContext() ) );
		doReturn( contextSpy ).when( componentSpy ).getContext();
		doReturn( componentSpy ).when( contextSpy ).getComponent();

		final Mutable mockVariable = mock( StatisticVariable.Mutable.class );
		when( mockVariable.getStatisticHolder() ).thenReturn( componentSpy );
		@SuppressWarnings( "rawtypes" )
		final Statistic statisticMock = mock( Statistic.class );
		when( statisticMock.getStatisticVariable() ).thenReturn( mockVariable );
		when( mockVariable.getStatistic( anyString(), anyString() ) ).thenReturn( statisticMock );
		doReturn( mockVariable ).when( contextSpy ).addStatisticVariable( anyString(), anyString(),
				Matchers.<String>anyVararg() );
		doReturn( mockVariable ).when( contextSpy ).addListenableStatisticVariable( anyString(), anyString(),
				Matchers.<String>anyVararg() );

		ComponentItem componentItemImplSpy = mock( ComponentItemImpl.class, delegatesTo( component ) );
		doReturn( contextSpy ).when( componentItemImplSpy ).getContext();

		createComponent( componentName, componentItemImplSpy );
		contextSpy.setNonBlocking( true );

		return componentSpy;
	}

	public static ComponentItem createComponent( final String componentName, ComponentItem component )
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
		ComponentTestUtils.setComponentBehavior( component,
				descriptors.get( descriptor ).createBehavior( descriptor, component.getContext() ) );

		return component;
	}

	public static GroovyEnvironment getEnvironment( ComponentItem component )
	{
		try
		{
			final Class<? extends ComponentBehavior> cls = component.getBehavior().getClass();
			Field field = cls.getDeclaredField( "scriptSupport" );
			field.setAccessible( true );
			GroovyBehaviorSupport support = ( GroovyBehaviorSupport )field.get( component.getBehavior() );
			return support.getEnvironment();
		}
		catch( NoSuchFieldException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( SecurityException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( IllegalArgumentException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( IllegalAccessException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new IllegalArgumentException();
	}
}
