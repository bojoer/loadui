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
package com.eviware.loadui.groovy.categories;

import java.util.Map;

import groovy.lang.MissingMethodException;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.summary.MutableChapter;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.groovy.GroovyBehaviorProvider;
import com.eviware.loadui.groovy.GroovyScriptSupport;
import com.eviware.loadui.impl.component.categories.RunnerBase;
import com.eviware.loadui.util.ReleasableUtils;

public class GroovyRunner extends RunnerBase
{
	private final GroovyScriptSupport scriptSupport;

	public GroovyRunner( GroovyBehaviorProvider scriptUpdateFirer, ComponentContext context )
	{
		super( context );

		scriptSupport = new GroovyScriptSupport( scriptUpdateFirer, this, context );
	}

	@Override
	protected TerminalMessage sample( TerminalMessage triggerMessage, Object sampleId ) throws SampleCancelledException
	{
		Object value = scriptSupport.invokeClosure( false, true, "sample", triggerMessage, sampleId );
		if( value instanceof Throwable )
		{
			Throwable exception = ( Throwable )value;
			if( exception.getCause() instanceof SampleCancelledException )
				throw ( SampleCancelledException )exception.getCause();
			else
			{
				scriptSupport.getLog().error( "Exception in closure sample:", exception );
				if( exception instanceof RuntimeException )
					throw ( RuntimeException )exception;
				else
					throw new RuntimeException( exception );
			}
		}

		return ( TerminalMessage )value;
	}

	@Override
	protected int onCancel()
	{
		int runningRequests = 0;
		try
		{
			Object returnValue = scriptSupport.invokeClosure( false, false, "onCancel" );
			if( returnValue instanceof Number )
				runningRequests = ( ( Number )returnValue ).intValue();
			else
				scriptSupport.getLog().warn( "onCancel returned value of type {}, expecting an int!",
						returnValue == null ? null : returnValue.getClass() );
		}
		catch( MissingMethodException e )
		{
			// Ignore, as this method is optional.
		}
		return runningRequests;
	}

	@Override
	public void onTerminalConnect( OutputTerminal output, InputTerminal input )
	{
		super.onTerminalConnect( output, input );
		scriptSupport.invokeClosure( true, false, "onTerminalConnect", output, input );
	}

	@Override
	public void onTerminalDisconnect( OutputTerminal output, InputTerminal input )
	{
		super.onTerminalDisconnect( output, input );
		scriptSupport.invokeClosure( true, false, "onTerminalDisconnect", output, input );
	}

	@Override
	public void onTerminalMessage( OutputTerminal output, InputTerminal input, TerminalMessage message )
	{
		super.onTerminalMessage( output, input, message );
		scriptSupport.invokeClosure( true, false, "onTerminalMessage", output, input, message );
	}

	@Override
	public void onTerminalSignatureChange( OutputTerminal output, Map<String, Class<?>> signature )
	{
		super.onTerminalSignatureChange( output, signature );
		scriptSupport.invokeClosure( true, false, "onTerminalSignatureChange", output, signature );
	}

	@Override
	public Object collectStatisticsData()
	{
		Object result = scriptSupport.invokeClosure( true, false, "collectStatisticsData" );
		return result != null ? result : super.collectStatisticsData();
	}

	@Override
	public void handleStatisticsData( Map<AgentItem, Object> statisticsData )
	{
		super.handleStatisticsData( statisticsData );
		scriptSupport.invokeClosure( true, false, "handleStatisticsData", statisticsData );
	}

	@Override
	public void generateSummary( MutableChapter summary )
	{
		super.generateSummary( summary );
		scriptSupport.invokeClosure( true, false, "generateSummary", summary );
	}

	@Override
	public void onRelease()
	{
		super.onRelease();
		ReleasableUtils.release( scriptSupport );
	}
}
