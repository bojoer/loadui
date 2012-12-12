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
package com.eviware.loadui.util.groovy;

import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides additional methods or properties to the script environment.
 * 
 * @author dain.nilsson
 */
public interface GroovyResolver
{
	public static final NullResolver NULL_RESOLVER = new NullResolver();

	/**
	 * Resolves methods.
	 * 
	 * @author dain.nilsson
	 */
	public interface Methods extends GroovyResolver
	{
		/**
		 * Called when an unresolved method is invoked from the context of the
		 * script.
		 * 
		 * @param methodName
		 * @param args
		 * @return
		 * @throws MissingMethodException
		 */
		@Nullable
		public Object invokeMethod( @Nonnull String methodName, Object... args ) throws MissingMethodException;
	}

	/**
	 * Resolves Properties.
	 * 
	 * @author dain.nilsson
	 */
	public interface Properties extends GroovyResolver
	{

		/**
		 * Called when an unresolved property is accessed from the context of the
		 * script.
		 * 
		 * @param propertyName
		 * @return
		 * @throws MissingPropertyException
		 */
		@Nullable
		public Object getProperty( @Nonnull String propertyName ) throws MissingPropertyException;
	}

	public static final class NullResolver implements GroovyResolver.Methods, GroovyResolver.Properties
	{
		private NullResolver()
		{
		}

		@Override
		public Object getProperty( String propertyName ) throws MissingPropertyException
		{
			throw new MissingPropertyException( propertyName, NullResolver.class );
		}

		@Override
		public Object invokeMethod( String methodName, Object... args ) throws MissingMethodException
		{
			throw new MissingMethodException( methodName, NullResolver.class, args );
		}
	}
}