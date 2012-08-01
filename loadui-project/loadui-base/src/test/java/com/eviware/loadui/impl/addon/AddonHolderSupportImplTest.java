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
package com.eviware.loadui.impl.addon;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.eviware.loadui.api.addon.Addon;
import com.eviware.loadui.api.addon.Addon.Context;
import com.eviware.loadui.api.addon.AddonHolder;
import com.eviware.loadui.api.addon.AddonItem;
import com.eviware.loadui.api.addon.AddonRegistry;
import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.config.AddonListConfig;
import com.eviware.loadui.util.test.BeanInjectorMocker;
import com.eviware.loadui.util.test.DefaultAddonRegistry;

public class AddonHolderSupportImplTest
{
	AddonRegistry addonRegistry;
	AddonHolderSupportImpl addonHolderSupport;
	private MockAddon addonMock;
	private Addon.Context context;
	private Addon.Context secondContext;
	private Addon.Factory<?> factoryMock;
	private AddonListConfig config;

	private static interface MockAddon extends Addon
	{
	}

	private static interface SecondMockAddon extends Addon
	{
	}

	@SuppressWarnings( "unchecked" )
	@Before
	public void setup()
	{
		addonRegistry = new DefaultAddonRegistry();
		new BeanInjectorMocker().put( AddressableRegistry.class, mock( AddressableRegistry.class ) ).put(
				AddonRegistry.class, addonRegistry );

		config = AddonListConfig.Factory.newInstance();
		AddonHolder holderMock = mock( AddonHolder.class );

		addonMock = mock( MockAddon.class );
		factoryMock = mock( Addon.Factory.class );
		when( factoryMock.create( ( Context )anyObject() ) ).thenAnswer( new Answer<MockAddon>()
		{
			@Override
			public MockAddon answer( InvocationOnMock invocation ) throws Throwable
			{
				context = ( Context )invocation.getArguments()[0];
				return addonMock;
			}
		} );

		addonRegistry.registerFactory( MockAddon.class, ( Addon.Factory<MockAddon> )factoryMock );

		addonHolderSupport = new AddonHolderSupportImpl( holderMock, config );
		addonHolderSupport.init();
	}

	@Test
	public void shouldCreateAddonLazily()
	{

		MockAddon addon = addonHolderSupport.getAddon( MockAddon.class );
		assertThat( addon, sameInstance( addonMock ) );

		addon = addonHolderSupport.getAddon( MockAddon.class );
		assertThat( addon, sameInstance( addonMock ) );

		verify( factoryMock ).create( context );
		verifyNoMoreInteractions( factoryMock );
	}

	@Test
	public void shouldCreateAddonItemSupports()
	{
		addonHolderSupport.getAddon( MockAddon.class );

		AddonItem.Support addonItemSupport = context.createAddonItemSupport();

		assertThat( addonItemSupport, is( AddonItem.Support.class ) );
		assertThat( context.getAddonItemSupports(),
				is( ( Collection<AddonItem.Support> )Collections.singleton( addonItemSupport ) ) );

		assertThat( config.getAddonList().size(), is( 1 ) );
		assertThat( config.getAddonArray( 0 ).getType(), is( MockAddon.class.getName() ) );

		context.createAddonItemSupport();

		assertThat( context.getAddonItemSupports().size(), is( 2 ) );

		assertThat( config.getAddonList().size(), is( 2 ) );
	}

	@SuppressWarnings( "unchecked" )
	@Test
	public void shouldSeparateAddonItemSupportsOfDifferentType()
	{
		addonHolderSupport.getAddon( MockAddon.class );

		final SecondMockAddon secondAddonMock = mock( SecondMockAddon.class );

		Addon.Factory<SecondMockAddon> secondFactoryMock = mock( Addon.Factory.class );
		when( secondFactoryMock.create( ( Context )anyObject() ) ).thenAnswer( new Answer<SecondMockAddon>()
		{
			@Override
			public SecondMockAddon answer( InvocationOnMock invocation ) throws Throwable
			{
				secondContext = ( Context )invocation.getArguments()[0];
				return secondAddonMock;
			}
		} );

		addonRegistry.registerFactory( SecondMockAddon.class, secondFactoryMock );

		addonHolderSupport.getAddon( SecondMockAddon.class );

		context.createAddonItemSupport();
		secondContext.createAddonItemSupport();
		context.createAddonItemSupport();

		assertThat( context.getAddonItemSupports().size(), is( 2 ) );
		assertThat( secondContext.getAddonItemSupports().size(), is( 1 ) );
		assertThat( config.getAddonList().size(), is( 3 ) );
	}

	@Test
	public void shouldDeleteAddonItemSupports()
	{
		addonHolderSupport.getAddon( MockAddon.class );

		AddonItem.Support addonItemSupport = context.createAddonItemSupport();

		addonItemSupport.delete();

		assertThat( context.getAddonItemSupports(), IsEmptyCollection.<AddonItem.Support> empty() );
	}
}
