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

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.categories.AnalysisCategory;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;

/**
 * Base class for analysis components which defines base behavior which can be
 * extended to fully implement an analysis ComponentBehavior.
 * 
 * @author dain.nilsson
 */
public abstract class AnalysisBase extends BaseCategory implements AnalysisCategory
{
	private final InputTerminal inputTerminal;

	/**
	 * Constructs an AnalysisBase.
	 * 
	 * @param context
	 *           A ComponentContext to bind the AnalysisBase to.
	 */
	public AnalysisBase( ComponentContext context )
	{
		super( context );

		inputTerminal = context.createInput( INPUT_TERMINAL, "Input Data to be Analysed" );
	}

	/**
	 * Analyzes the given TerminalMessage.
	 * 
	 * @param message
	 */
	public abstract void analyze( TerminalMessage message );

	@Override
	final public InputTerminal getInputTerminal()
	{
		return inputTerminal;
	}

	@Override
	public void onTerminalMessage( OutputTerminal output, InputTerminal input, TerminalMessage message )
	{
		if( input == inputTerminal )
			analyze( message );
	}

	@Override
	final public String getCategory()
	{
		return CATEGORY;
	}

	@Override
	final public String getColor()
	{
		return COLOR;
	}

}
