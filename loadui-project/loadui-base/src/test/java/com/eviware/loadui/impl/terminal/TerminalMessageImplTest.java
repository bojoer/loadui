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
package com.eviware.loadui.impl.terminal;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import com.eviware.loadui.api.terminal.TerminalMessage;

public class TerminalMessageImplTest
{
	private ConversionService conversionService;
	private TerminalMessage message;

	@Before
	public void setup()
	{
		conversionService = new DefaultConversionService();

		message = new TerminalMessageImpl( conversionService );
	}

	@Test
	public void shouldStoreItems()
	{
		message.put( "a", "Hello", String.class );
		message.put( "b", 42, Integer.class );
		message.put( "c", ( long )4711, Long.class );
		message.put( "d", null );

		assertThat( ( String )message.get( "a" ), is( "Hello" ) );
		assertThat( ( Integer )message.get( "b" ), is( 42 ) );
		assertThat( ( Long )message.get( "c" ), is( 4711L ) );
		assertThat( message.get( "d" ), is( nullValue() ) );
	}

	@Test
	public void shouldInferTypes()
	{
		message.put( "a", "Hello" );
		message.put( "b", 42 );
		message.put( "c", 4711L );

		assertThat( message.get( "a" ), instanceOf( String.class ) );
		assertThat( message.get( "b" ), instanceOf( Integer.class ) );
		assertThat( message.get( "c" ), instanceOf( Long.class ) );

		assertThat( ( String )message.get( "a" ), is( "Hello" ) );
		assertThat( ( Integer )message.get( "b" ), is( 42 ) );
		assertThat( ( Long )message.get( "c" ), is( 4711L ) );
	}

	@Test
	public void shouldSerializeAndDeserialize()
	{
		message.put( "a", "Hello" );
		message.put( "b", 42 );
		message.put( "c", 13.37 );
		message.put( "d", new Date() );

		Object serialized = message.serialize();

		message = new TerminalMessageImpl( conversionService );

		message.load( serialized );

		assertThat( message.get( "a" ), instanceOf( String.class ) );
		assertThat( message.get( "b" ), instanceOf( Integer.class ) );
		assertThat( message.get( "c" ), instanceOf( Double.class ) );
		assertThat( message.get( "d" ), instanceOf( Date.class ) );

		assertThat( ( String )message.get( "a" ), is( "Hello" ) );
		assertThat( ( Integer )message.get( "b" ), is( 42 ) );
		assertThat( ( Double )message.get( "c" ), is( 13.37 ) );
	}
}
