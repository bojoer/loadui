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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.categories.FlowCategory;
import com.eviware.loadui.api.counter.Counter;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;

import com.eviware.loadui.api.terminal.TerminalMessage;

import com.eviware.loadui.impl.component.ActivityStrategies;
import com.eviware.loadui.util.BeanInjector;

/**
 * Base class for flow components which defines base behavior which can be
 * extended to fully implement a flow ComponentBehavior.
 * 
 * @author dain.nilsson
 */
public abstract class FlowBase extends BaseCategory implements FlowCategory
{
	private static final int BLINK_TIME = 1000;

	private final InputTerminal incomingTerminal;
	private final List<OutputTerminal> outgoingTerminals = new ArrayList<OutputTerminal>();
	private Map<String, Class<?>> inputSignature = Collections.emptyMap();

	private final ScheduledExecutorService executor;
	private final Runnable activityRunnable;
	private long lastMsg;
	private ScheduledFuture<?> activityFuture;
	private final ArrayList<Counter> counters = new ArrayList<Counter>();

	/**
	 * Constructs a FlowBase.
	 * 
	 * @param context
	 *           A ComponentContext to bind the FlowBase to.
	 */
	public FlowBase( ComponentContext context )
	{
		super( context );
		executor = BeanInjector.getBean( ScheduledExecutorService.class );

		context.setActivityStrategy( ActivityStrategies.ON );
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

		for( int i = 0; i < 10; i++ )
			counters.add( getContext().getCounter( "out_" + i ) );

		activityRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				long now = System.currentTimeMillis();
				if( lastMsg + BLINK_TIME <= now )
				{
					getContext().setActivityStrategy( ActivityStrategies.ON );
					activityFuture = null;
				}
				else
				{
					activityFuture = executor.schedule( activityRunnable, lastMsg + BLINK_TIME, TimeUnit.MILLISECONDS );
				}
			}
		};
	}

	/**
	 * Creates an additional OutputTerminal and appends it to the
	 * outgoingTerminals List.
	 * 
	 * @return
	 */
	final public OutputTerminal createOutgoing()
	{
		OutputTerminal output = getContext().createOutput( OUTGOING_TERMINAL + " " + ( outgoingTerminals.size() + 1 ),
				"Output Terminal " + " " + ( outgoingTerminals.size() + 1 ) );
		getContext().setSignature( output, inputSignature );
		outgoingTerminals.add( output );

		return output;
	}

	final public ArrayList<Counter> getCounters()
	{
		return counters;
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
		Map<String, Class<?>> newSig = new HashMap<String, Class<?>>();
		for( Connection connection : new ArrayList<Connection>( incomingTerminal.getConnections() ) )
			for( Entry<String, Class<?>> entry : connection.getOutputTerminal().getMessageSignature().entrySet() )
				newSig.put( entry.getKey(), entry.getValue() );
		inputSignature = newSig;

		for( OutputTerminal output : getOutgoingTerminalList() )
			getContext().setSignature( output, inputSignature );
	}

	@Override
	public void onTerminalMessage( OutputTerminal output, InputTerminal input, TerminalMessage message )
	{
		if( input == incomingTerminal )
		{
			lastMsg = System.currentTimeMillis();
			if( activityFuture == null )
			{
				getContext().setActivityStrategy( ActivityStrategies.BLINKING );
				activityFuture = executor.schedule( activityRunnable, BLINK_TIME, TimeUnit.MILLISECONDS );
			}
		}
	}
}
