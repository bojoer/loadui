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

import java.util.Map;

import org.codehaus.groovy.runtime.InvokerHelper;

import com.eviware.loadui.api.component.ComponentBehavior;
import com.eviware.loadui.api.model.RunnerItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;

import groovy.lang.GroovyObject;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;
import com.eviware.loadui.api.summary.MutableChapter;

public class GroovyBaseCategory<C extends ComponentBehavior> extends GroovyObjectSupport implements ComponentBehavior
{
	private final C base;
	private final GroovyObject delegate;

	public GroovyBaseCategory( C baseImpl, GroovyObject delegate )
	{
		this.base = baseImpl;
		this.delegate = delegate;
	}

	protected C getBase()
	{
		return base;
	}

	@Override
	public Object invokeMethod( String name, Object args )
	{
		try
		{
			return InvokerHelper.getMetaClass( this ).invokeMethod( this, name, args );
		}
		catch( MissingMethodException e )
		{
			return InvokerHelper.getMetaClass( delegate ).invokeMethod( delegate, name, args );
		}
	}

	@Override
	public final String getCategory()
	{
		return base.getCategory();
	}

	@Override
	public final String getColor()
	{
		return base.getColor();
	}

	@Override
	public final void onRelease()
	{
		base.onRelease();
	}

	@Override
	public void onTerminalConnect( OutputTerminal output, InputTerminal input )
	{
		base.onTerminalConnect( output, input );
	}

	@Override
	public void onTerminalDisconnect( OutputTerminal output, InputTerminal input )
	{
		base.onTerminalDisconnect( output, input );
	}

	@Override
	public void onTerminalMessage( OutputTerminal output, InputTerminal input, TerminalMessage message )
	{
		base.onTerminalMessage( output, input, message );
	}

	@Override
	public void onTerminalSignatureChange( OutputTerminal output, Map<String, Class<?>> signature )
	{
		base.onTerminalSignatureChange( output, signature );
	}

	@Override
	public Object collectStatisticsData()
	{
		return base.collectStatisticsData();
	}

	@Override
	public void handleStatisticsData( Map<RunnerItem, Object> statisticsData )
	{
		base.handleStatisticsData( statisticsData );
	}
	
	@Override
	 public void generateSummary( MutableChapter summary )
	 {
	  // TODO Auto-generated method stub

	 }
}
