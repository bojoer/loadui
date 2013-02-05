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
