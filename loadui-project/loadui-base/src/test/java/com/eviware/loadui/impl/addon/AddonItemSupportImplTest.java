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
