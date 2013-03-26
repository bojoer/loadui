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
package com.eviware.loadui.util.groovy.resolvers;

import javax.annotation.Nonnull;

import org.codehaus.groovy.runtime.InvokerHelper;

import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;

import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.groovy.GroovyResolver;
import com.google.common.base.Preconditions;

/**
 * Resolved methods and properties to a delegate Object using reflection.
 * 
 * @author dain.nilsson
 */
public class JavaBeanGroovyResolver implements GroovyResolver.Methods, GroovyResolver.Properties, Releasable
{
	private final Object javaBean;

	public JavaBeanGroovyResolver( @Nonnull Object javaBean )
	{
		this.javaBean = Preconditions.checkNotNull( javaBean );
	}

	@Override
	public void release()
	{
		ReleasableUtils.release( javaBean );
	}

	@Override
	public Object invokeMethod( String methodName, Object... args ) throws MissingMethodException
	{
		return InvokerHelper.invokeMethod( javaBean, methodName, args );
	}

	@Override
	public Object getProperty( String propertyName ) throws MissingPropertyException
	{
		return InvokerHelper.getProperty( javaBean, propertyName );
	}
}
