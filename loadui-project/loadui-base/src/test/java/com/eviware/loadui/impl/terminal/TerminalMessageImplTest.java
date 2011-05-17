/*
 * Copyright 2011 eviware software ab
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

import org.junit.*;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.ConversionServiceFactory;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import com.eviware.loadui.api.terminal.TerminalMessage;

public class TerminalMessageImplTest
{
	private ConversionService conversionService;
	private TerminalMessage message;

	@Before
	public void setup()
	{
		conversionService = ConversionServiceFactory.createDefaultConversionService();

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

		assertThat( message.get( "a" ), is( String.class ) );
		assertThat( message.get( "b" ), is( Integer.class ) );
		assertThat( message.get( "c" ), is( Long.class ) );

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

		Object serialized = message.serialize();

		message = new TerminalMessageImpl( conversionService );

		message.load( serialized );

		assertThat( message.get( "a" ), is( String.class ) );
		assertThat( message.get( "b" ), is( Integer.class ) );
		assertThat( message.get( "c" ), is( Double.class ) );

		assertThat( ( String )message.get( "a" ), is( "Hello" ) );
		assertThat( ( Integer )message.get( "b" ), is( 42 ) );
		assertThat( ( Double )message.get( "c" ), is( 13.37 ) );
	}
}
