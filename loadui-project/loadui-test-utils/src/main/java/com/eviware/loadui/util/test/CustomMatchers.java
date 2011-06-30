package com.eviware.loadui.util.test;

import static org.hamcrest.CoreMatchers.not;

import org.hamcrest.Matcher;

import com.eviware.loadui.util.test.matchers.IsMock;

/**
 * Class for static methods for creating custom Matchers.
 * 
 * @author dain.nilsson
 */
public class CustomMatchers
{
	/**
	 * Verified that the object to match is a mock.
	 * 
	 * @return
	 */
	public static <T> Matcher<T> mockObject()
	{
		return new IsMock<T>();
	}

	/**
	 * Verified that the object to match is not a mock.
	 * 
	 * @return
	 */
	public static <T> Matcher<T> notMockObject()
	{
		return not( new IsMock<T>() );
	}
}
