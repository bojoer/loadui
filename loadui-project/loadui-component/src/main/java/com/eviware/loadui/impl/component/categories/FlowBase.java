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
package com.eviware.loadui.impl.component.categories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.categories.FlowCategory;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.impl.component.ActivityStrategies;
import com.eviware.loadui.impl.component.BlinkOnUpdateActivityStrategy;
import com.eviware.loadui.util.ReleasableUtils;

/**
 * Base class for flow components which defines base behavior which can be
 * extended to fully implement a flow ComponentBehavior.
 * 
 * @author dain.nilsson
 */
public abstract class FlowBase extends BaseCategory implements FlowCategory
{
	private final InputTerminal incomingTerminal;
	private final List<OutputTerminal> outgoingTerminals = new ArrayList<>();
	private Map<String, Class<?>> inputSignature = Collections.emptyMap();

	private final BlinkOnUpdateActivityStrategy activityStrategy = ActivityStrategies.newBlinkOnUpdateStrategy();

	/**
	 * Constructs a FlowBase.
	 * 
	 * @param context
	 *           A ComponentContext to bind the FlowBase to.
	 */
	public FlowBase( ComponentContext context )
	{
		super( context );

		context.setActivityStrategy( activityStrategy );
		incomingTerminal = context.createInput( INCOMING_TERMINAL, "Incoming messages" );
		context.setLikeFunction( incomingTerminal, new ComponentContext.LikeFunction()
		{
			private final AtomicBoolean delegating = new AtomicBoolean( false );

			@Override
			public boolean call( OutputTerminal output )
			{
				if( delegating.get() )
					return false;

				try
				{
					delegating.set( true );
					for( OutputTerminal ot : getOutgoingTerminalList() )
					{
						for( Connection conn : ot.getConnections() )
						{
							if( conn.getInputTerminal().likes( output ) )
								return true;
						}
					}
				}
				finally
				{
					delegating.set( false );
				}

				return false;
			}
		} );
	}

	/**
	 * Creates an additional OutputTerminal and appends it to the
	 * outgoingTerminals List.
	 * 
	 * @return
	 */
	final public OutputTerminal createOutgoing( String name )
	{
		OutputTerminal output = getContext().createOutput( name, "Output Terminal " + ( outgoingTerminals.size() + 1 ) );
		getContext().setSignature( output, inputSignature );
		outgoingTerminals.add( output );

		return output;
	}

	/**
	 * Creates an additional OutputTerminal and appends it to the
	 * outgoingTerminals List using a default name.
	 * 
	 * @return
	 */
	final public OutputTerminal createOutgoing()
	{
		return createOutgoing( OUTGOING_TERMINAL + " " + ( outgoingTerminals.size() + 1 ) );
	}

	/**
	 * Deletes the OutputTerminal in the outgoingTerminals List with the highest
	 * numbering (the last one to be added). If no OutputTerminals exist in this
	 * list, nothing will happen.
	 */
	final public OutputTerminal deleteOutgoing()
	{
		if( outgoingTerminals.size() > 0 )
		{
			OutputTerminal removed = outgoingTerminals.remove( outgoingTerminals.size() - 1 );
			getContext().deleteTerminal( removed );
			return removed;
		}
		return null;
	}

	@Override
	public void onTerminalConnect( OutputTerminal output, InputTerminal input )
	{
		super.onTerminalConnect( output, input );

		updateSignature();
	}

	@Override
	public void onTerminalDisconnect( OutputTerminal output, InputTerminal input )
	{
		super.onTerminalDisconnect( output, input );

		updateSignature();
	}

	@Override
	public void onTerminalSignatureChange( OutputTerminal output, Map<String, Class<?>> signature )
	{
		super.onTerminalSignatureChange( output, signature );

		updateSignature();
	}

	@Override
	final public InputTerminal getIncomingTerminal()
	{
		return incomingTerminal;
	}

	@Override
	final public List<OutputTerminal> getOutgoingTerminalList()
	{
		return Collections.unmodifiableList( outgoingTerminals );
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

	protected void updateSignature()
	{
		Map<String, Class<?>> newSig = new HashMap<>();
		for( Connection connection : new ArrayList<>( incomingTerminal.getConnections() ) )
			for( Entry<String, Class<?>> entry : connection.getOutputTerminal().getMessageSignature().entrySet() )
				newSig.put( entry.getKey(), entry.getValue() );
		inputSignature = newSig;

		for( OutputTerminal output : getOutgoingTerminalList() )
			getContext().setSignature( output, inputSignature );
	}

	@Override
	public void onTerminalMessage( OutputTerminal output, InputTerminal input, TerminalMessage message )
	{
		super.onTerminalMessage( output, input, message );

		if( input == incomingTerminal )
		{
			activityStrategy.update();
		}
	}

	@Override
	public synchronized void onRelease()
	{
		super.onRelease();
		ReleasableUtils.release( activityStrategy );
	}
}
