package com.eviware.loadui.impl.addon;

import org.junit.*;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.GenericConversionService;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import com.eviware.loadui.api.model.PropertyHolder;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.property.PropertyMap;
import com.eviware.loadui.config.AddonListConfig;
import com.eviware.loadui.util.test.BeanInjectorMocker;

public class AddonItemSupportImplTest
{
	AddonListConfig list;
	AddonItemSupportImpl support;
	AddonItemHolderSupport owner;

	@Before
	public void setup()
	{
		new BeanInjectorMocker().put( ConversionService.class, new GenericConversionService() );

		list = AddonListConfig.Factory.newInstance();
		owner = mock( AddonItemHolderSupport.class );
		support = new AddonItemSupportImpl( owner, list.addNewAddon(), list );
	}

	@Test
	public void shouldPersistProperties()
	{
		PropertyHolder phMock = mock( PropertyHolder.class );
		PropertyMap pm = support.getPropertyMap( phMock );

		Property<String> testProperty = pm.createProperty( "TEST", String.class );
		testProperty.setValue( "Hello" );

		assertThat( testProperty.getValue(), is( "Hello" ) );

		support.release();

		support = new AddonItemSupportImpl( owner, list.getAddonArray( 0 ), list );
		pm = support.getPropertyMap( phMock );
		testProperty = pm.createProperty( "TEST", String.class );

		assertThat( testProperty.getValue(), is( "Hello" ) );
	}

	@Test
	public void shouldPersistAttributes()
	{
		support.setAttribute( "TEST", "Hi there" );

		assertThat( support.getAttribute( "TEST", null ), is( "Hi there" ) );

		support.release();

		support = new AddonItemSupportImpl( owner, list.getAddonArray( 0 ), list );
		assertThat( support.getAttribute( "TEST", null ), is( "Hi there" ) );
	}
}
