package com.eviware.loadui.impl.addon;

import java.util.Collection;
import java.util.Collections;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.*;
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

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

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

		assertThat( config.getAddonArray().length, is( 1 ) );
		assertThat( config.getAddonArray( 0 ).getType(), is( MockAddon.class.getName() ) );

		context.createAddonItemSupport();

		assertThat( context.getAddonItemSupports().size(), is( 2 ) );

		assertThat( config.getAddonArray().length, is( 2 ) );
	}

	@SuppressWarnings( "unchecked" )
	@Test
	public void shouldSeparateAddonItemSupportsOfDifferentType()
	{
		addonHolderSupport.getAddon( MockAddon.class );

		final SecondMockAddon secondAddonMock = mock( SecondMockAddon.class );

		Addon.Factory<SecondMockAddon> factoryMock = mock( Addon.Factory.class );
		when( factoryMock.create( ( Context )anyObject() ) ).thenAnswer( new Answer<SecondMockAddon>()
		{
			@Override
			public SecondMockAddon answer( InvocationOnMock invocation ) throws Throwable
			{
				secondContext = ( Context )invocation.getArguments()[0];
				return secondAddonMock;
			}
		} );

		addonRegistry.registerFactory( SecondMockAddon.class, factoryMock );

		addonHolderSupport.getAddon( SecondMockAddon.class );

		context.createAddonItemSupport();
		secondContext.createAddonItemSupport();
		context.createAddonItemSupport();

		assertThat( context.getAddonItemSupports().size(), is( 2 ) );
		assertThat( secondContext.getAddonItemSupports().size(), is( 1 ) );
		assertThat( config.getAddonArray().length, is( 3 ) );
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
