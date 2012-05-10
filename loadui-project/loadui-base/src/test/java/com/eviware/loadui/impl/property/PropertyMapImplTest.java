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
package com.eviware.loadui.impl.property;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.config.ModelItemConfig;
import com.eviware.loadui.config.PropertyListConfig;
import com.eviware.loadui.impl.model.ModelItemImpl;
import com.eviware.loadui.util.StringUtils;

public class PropertyMapImplTest
{
	private PropertyListConfig config;
	private PropertyMapImpl map;
	private ConversionService conversionService;
	private ModelItemImpl<?> owner;

	@Before
	public void setup()
	{
		config = PropertyListConfig.Factory.newInstance();
		owner = mock( ModelItemImpl.class );
		ModelItemConfig mic = mock( ModelItemConfig.class );
		when( ( ModelItemConfig )owner.getConfig() ).thenReturn( mic );
		when( mic.getProperties() ).thenReturn( config );
		conversionService = new DefaultConversionService();
		map = new PropertyMapImpl( owner, conversionService, owner.getConfig().getProperties() );
	}

	@Test
	public void shouldCreateProperty()
	{
		Property<String> p = map.createProperty( "test", String.class );

		assertNotNull( p );
		assertThat( p.getKey(), is( "test" ) );
		assertEquals( p.getOwner(), owner );
		assertEquals( String.class, p.getType() );

		p.setValue( "Hello world" );

		assertThat( p.getValue(), is( "Hello world" ) );
	}

	@SuppressWarnings( "unchecked" )
	@Test
	public void shouldInitializeFromConfig()
	{
		Property<String> p = map.createProperty( "test", String.class );
		p.setValue( "Hello world" );

		PropertyMapImpl map2 = new PropertyMapImpl( owner, conversionService, owner.getConfig().getProperties() );

		assertThat( map2.values().size(), is( 1 ) );

		Property<String> p2 = ( Property<String> )map2.get( "test" );

		assertEquals( p2.getType(), String.class );
		assertThat( p2.getValue(), is( "Hello world" ) );
	}

	@Test
	@SuppressWarnings( "unchecked" )
	public void shouldPreserveSpacesAndFixLineBreaks() throws XmlException, IOException
	{
		Property<String> p = map.createProperty( "test", String.class );
		String testString = " This\rhas spaces \r\n and breaks\n";
		p.setValue( testString );

		File tmp = File.createTempFile( "PropertyTest", ".xml" );
		config.save( tmp );
		config = PropertyListConfig.Factory.parse( tmp );

		PropertyMapImpl map2 = new PropertyMapImpl( owner, conversionService, owner.getConfig().getProperties() );

		assertThat( map2.values().size(), is( 1 ) );

		Property<String> p2 = ( Property<String> )map2.get( "test" );

		assertEquals( p2.getType(), String.class );
		assertThat( p2.getValue(), is( StringUtils.fixLineSeparators( testString ) ) );
	}
}
