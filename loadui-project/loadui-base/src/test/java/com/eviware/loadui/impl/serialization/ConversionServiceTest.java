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
package com.eviware.loadui.impl.serialization;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

public class ConversionServiceTest
{
	private ConversionService conversionService;

	@Before
	public void setup()
	{
		conversionService = new DefaultConversionService();
	}

	@Test
	public void shouldConvertStringsToNumbers()
	{
		assertEquals( new Integer( 4711 ), conversionService.convert( "4711", Integer.class ) );

		assertEquals( new Long( 4711 ), conversionService.convert( "4711", Long.class ) );

		assertEquals( new Float( 47.11 ), conversionService.convert( "47.11", Float.class ) );

		assertEquals( new Double( 47.11 ), conversionService.convert( "47.11", Double.class ) );
	}

	@Test
	public void shouldConvertNumbersToString()
	{
		assertEquals( "4711", conversionService.convert( 4711, String.class ) );

		assertEquals( "4711", conversionService.convert( 4711L, String.class ) );

		assertEquals( "47.11", conversionService.convert( 47.11, String.class ) );

		assertEquals( "47.11", conversionService.convert( 47.11d, String.class ) );

		assertEquals( "4711", conversionService.convert( new Integer( 4711 ), String.class ) );

		assertEquals( "4711", conversionService.convert( new Long( 4711 ), String.class ) );

		assertEquals( "47.11", conversionService.convert( new Float( 47.11 ), String.class ) );

		assertEquals( "47.11", conversionService.convert( new Double( 47.11 ), String.class ) );
	}

	@Test
	public void shouldConvertBothWays()
	{
		for( Object obj : Arrays.asList( new Integer( 4711 ), new Long( 4711 ), new Float( 47.11 ), new Double( 47.11 ) ) )
			assertEquals( obj, conversionService.convert( conversionService.convert( obj, String.class ), obj.getClass() ) );
	}

	@Test
	public void shouldConvertNumbers()
	{
		for( Number arg : Arrays.asList( new Integer( 4711 ), new Long( 4711 ), new Float( 47.11 ), new Double( 47.11 ) ) )
		{
			for( Class<?> type : Arrays.asList( Integer.class, Long.class, Float.class, Double.class ) )
				assertThat( conversionService.convert( arg, type ), instanceOf( type ) );
		}
	}
}
