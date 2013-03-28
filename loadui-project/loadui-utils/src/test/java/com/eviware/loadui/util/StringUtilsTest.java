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
package com.eviware.loadui.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringUtilsTest
{

	//TODO a lot of tests to implement!

	@Test
	public void testPadLeft() {
		assertEquals( "a", StringUtils.padLeft( "a", 0 ));
		assertEquals( " a", StringUtils.padLeft( "a", 2 ));
		assertEquals( "   a", StringUtils.padLeft( "a", 4 ));
		assertEquals( "a", StringUtils.padLeft( "a", -2 ));
		
		assertEquals( "123", StringUtils.padLeft( "123", 0 ));
		assertEquals( "123", StringUtils.padLeft( "123", 2 ));
		assertEquals( " 123", StringUtils.padLeft( "123", 4 ));
		assertEquals( "123", StringUtils.padLeft( "123", -2 ));
	}
	
	@Test
	public void testRight() {
		assertEquals( "a", StringUtils.padRight( "a", 0 ));
		assertEquals( "a ", StringUtils.padRight( "a", 2 ));
		assertEquals( "a   ", StringUtils.padRight( "a", 4 ));
		assertEquals( "a", StringUtils.padRight( "a", -2 ));
		
		assertEquals( "123", StringUtils.padRight( "123", 0 ));
		assertEquals( "123", StringUtils.padRight( "123", 2 ));
		assertEquals( "123 ", StringUtils.padRight( "123", 4 ));
		assertEquals( "123", StringUtils.padRight( "123", -2 ));
	}

}
