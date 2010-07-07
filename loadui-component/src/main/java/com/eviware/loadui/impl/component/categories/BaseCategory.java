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
package com.eviware.loadui.impl.component.categories;

import java.util.Map;

import com.eviware.loadui.api.component.ComponentBehavior;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.model.RunnerItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.api.summary.MutableChapter;


/**
 * Base class for component Categories which defines default implementations of
 * all required ComponentBehavior methods (which do nothing).
 * 
 * @author dain.nilsson
 */
public abstract class BaseCategory implements ComponentBehavior
{	
	private final ComponentContext context;

	/**
	 * Constructs a new BaseCategory.
	 * 
	 * @param context
	 *           A ComponentContext to bind the BaseCategory to.
	 */
	public BaseCategory( ComponentContext context )
	{
		this.context = context;
	}

	/**
	 * Returns the bound ComponentContext.
	 * 
	 * @return The ComponentContext.
	 */
	protected ComponentContext getContext()
	{
		return context;
	}

	@Override
	public void onRelease()
	{
	}

	@Override
	public void onTerminalConnect( OutputTerminal output, InputTerminal input )
	{
	}

	@Override
	public void onTerminalDisconnect( OutputTerminal output, InputTerminal input )
	{
	}

	@Override
	public void onTerminalMessage( OutputTerminal output, InputTerminal input, TerminalMessage message )
	{
	}

	@Override
	public void onTerminalSignatureChange( OutputTerminal output, Map<String, Class<?>> signature )
	{
	}

	@Override
	public Object collectStatisticsData()
	{
		return null;
	}

	@Override
	public void handleStatisticsData( Map<RunnerItem, Object> statisticsData )
	{
	}
	
	@Override
	 public void generateSummary( MutableChapter summary )
	 {

	 }
}
