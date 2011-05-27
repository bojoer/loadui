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
package com.eviware.loadui.impl.component.categories;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.categories.OnOffCategory;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;

public abstract class OnOffBase extends BaseCategory implements OnOffCategory
{
	private final Property<Boolean> stateProperty;
	private final InputTerminal stateTerminal;

	public OnOffBase( ComponentContext context )
	{
		super( context );

		stateProperty = context.createProperty( STATE_PROPERTY, Boolean.class, true );
		stateTerminal = context.createInput( STATE_TERMINAL, "Component activation",
				OnOffCategory.STATE_TERMINAL_DESCRIPTION );
	}

	@Override
	final public Property<Boolean> getStateProperty()
	{
		return stateProperty;
	}

	@Override
	final public InputTerminal getStateTerminal()
	{
		return stateTerminal;
	}

	@Override
	public void onTerminalMessage( OutputTerminal output, InputTerminal input, TerminalMessage message )
	{
		if( input == stateTerminal && message.containsKey( ENABLED_MESSAGE_PARAM ) )
		{
			stateProperty.setValue( message.get( ENABLED_MESSAGE_PARAM ) );
		}
	}
}
