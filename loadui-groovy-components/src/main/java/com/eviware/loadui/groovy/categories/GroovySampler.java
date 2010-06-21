/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.groovy.categories;

import groovy.lang.MissingMethodException;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.impl.component.categories.SamplerBase;

public class GroovySampler extends SamplerBase
{
	final Object delegate;

	public GroovySampler( ComponentContext context, Object delegate )
	{
		super( context );
		this.delegate = delegate;
	}

	@Override
	protected TerminalMessage sample( TerminalMessage triggerMessage, Object sampleId ) throws SampleCancelledException
	{
		try
		{
			return ( TerminalMessage )InvokerHelper.invokeMethod( delegate, "sample", new Object[] { triggerMessage,
					sampleId } );
		}
		catch( InvokerInvocationException e )
		{
			Throwable cause = e.getCause();
			if( cause instanceof SampleCancelledException )
				throw ( SampleCancelledException )cause;
			else
				throw e;
		}
	}

	@Override
	protected void onCancel()
	{
		try
		{
			InvokerHelper.invokeMethod( delegate, "onCancel", new Object[] {} );
		}
		catch( MissingMethodException e )
		{
			// Ignore.
		}
	}
}
