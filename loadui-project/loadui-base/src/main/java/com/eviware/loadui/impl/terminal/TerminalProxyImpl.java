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
package com.eviware.loadui.impl.terminal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.core.convert.ConversionService;

import com.eviware.loadui.api.addressable.Addressable;
import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.TerminalConnectionEvent;
import com.eviware.loadui.api.events.TerminalEvent;
import com.eviware.loadui.api.events.TerminalMessageEvent;
import com.eviware.loadui.api.events.TerminalRemoteMessageEvent;
import com.eviware.loadui.api.events.TerminalSignatureEvent;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.api.terminal.TerminalProxy;
import com.google.common.base.Preconditions;

public class TerminalProxyImpl implements TerminalProxy
{
	private final static String CHANNEL = "/" + TerminalProxyImpl.class.getName();
	// Arguments
	private final static String EVENT = "event";
	private final static String TARGET = "target";
	private final static String OUTPUT = "output";
	private final static String CONTENT = "content";

	private final ConversionService conversionService;
	private final AddressableRegistry addressableRegistry;
	private final MessageEndpoint endpoint;

	private final TerminalListener terminalListener = new TerminalListener();

	private enum Event
	{
		CONNECT, DISCONNECT, SIGNATURE, MESSAGE
	}

	public TerminalProxyImpl( ConversionService conversionService, AddressableRegistry addressableRegistry,
			MessageEndpoint endpoint )
	{
		this.conversionService = conversionService;
		this.addressableRegistry = addressableRegistry;
		this.endpoint = endpoint;
		endpoint.addMessageListener( CHANNEL, new Listener() );
	}

	@Override
	public void sendTerminalEvent( TerminalEvent event, MessageEndpoint endpoint, Addressable target )
	{
		Map<String, Object> message;
		message = new HashMap<String, Object>();
		message.put( OUTPUT, event.getOutputTerminal().getId() );
		message.put( TARGET, target.getId() );

		if( event instanceof TerminalConnectionEvent )
		{
			TerminalConnectionEvent cEvent = ( TerminalConnectionEvent )event;
			if( cEvent.getEvent() == TerminalConnectionEvent.Event.CONNECT )
				message.put( EVENT, Event.CONNECT.toString() );
			else
				message.put( EVENT, Event.DISCONNECT.toString() );
		}
		else if( event instanceof TerminalMessageEvent )
		{
			TerminalMessageEvent mEvent = ( TerminalMessageEvent )event;
			message.put( EVENT, Event.MESSAGE.toString() );
			message.put( CONTENT, mEvent.getMessage().serialize() );
		}
		else if( event instanceof TerminalSignatureEvent )
		{
			TerminalSignatureEvent sEvent = ( TerminalSignatureEvent )event;
			message.put( EVENT, Event.SIGNATURE.toString() );
			for( Entry<String, Class<?>> entry : sEvent.getSignature().entrySet() )
				message.put( "__" + entry.getKey(), entry.getValue().getName() );
		}

		endpoint.sendMessage( CHANNEL, message );
	}

	@Override
	public void export( OutputTerminal terminal )
	{
		terminal.addEventListener( TerminalMessageEvent.class, terminalListener );
	}

	@Override
	public void unexport( OutputTerminal terminal )
	{
		terminal.removeEventListener( TerminalMessageEvent.class, terminalListener );
	}

	private class TerminalListener implements EventHandler<TerminalMessageEvent>
	{
		@Override
		public void handleEvent( TerminalMessageEvent event )
		{
			sendTerminalEvent( event, endpoint, event.getOutputTerminal() );
		}
	}

	private class Listener implements MessageListener
	{
		private void propagateMessage( Map<String, String> message, OutputTerminal output, InputTerminal input )
		{
			Event event = Event.valueOf( message.get( EVENT ) );

			TerminalEvent terminalEvent = null;
			switch( event )
			{
			case CONNECT :
				terminalEvent = new TerminalConnectionEvent( new ConnectionStub( output, input ), output, input,
						TerminalConnectionEvent.Event.CONNECT );
				break;
			case DISCONNECT :
				terminalEvent = new TerminalConnectionEvent( new ConnectionStub( output, input ), output, input,
						TerminalConnectionEvent.Event.DISCONNECT );
				break;
			case SIGNATURE :
				Map<String, Class<?>> signature = new HashMap<String, Class<?>>();
				for( Entry<String, String> entry : message.entrySet() )
				{
					if( entry.getKey().startsWith( "__" ) )
					{
						try
						{
							signature.put( entry.getKey().substring( 2 ), Class.forName( entry.getValue() ) );
						}
						catch( ClassNotFoundException e )
						{
							throw new RuntimeException( e );
						}
					}
				}
				( ( OutputTerminalStub )output ).setMessageSignature( signature );
				terminalEvent = new TerminalSignatureEvent( output, signature );
				break;
			case MESSAGE :
				TerminalMessage content = new TerminalMessageImpl( conversionService );
				content.load( message.get( CONTENT ) );
				terminalEvent = new TerminalMessageEvent( output, content );
				break;
			default :
				throw new IllegalArgumentException();
			}
			input.getTerminalHolder().handleTerminalEvent( input, terminalEvent );
		}

		@Override
		@SuppressWarnings( "unchecked" )
		public void handleMessage( String channel, final MessageEndpoint endpoint, Object data )
		{
			Preconditions.checkArgument( endpoint instanceof AgentItem );
			AgentItem agent = ( AgentItem )endpoint;

			final Map<String, String> message = ( Map<String, String> )data;
			Terminal value = ( Terminal )addressableRegistry.lookup( message.get( OUTPUT ) );
			// TODO: lookup if null.
			OutputTerminal output = ( OutputTerminal )value;
			Addressable target = addressableRegistry.lookup( message.get( TARGET ) );
			if( target == null )
				return;

			if( target instanceof InputTerminal )
				propagateMessage( message, output, ( InputTerminal )target );
			else if( target instanceof OutputTerminalImpl )
			{
				OutputTerminalImpl source = ( OutputTerminalImpl )target;
				TerminalMessage content = new TerminalMessageImpl( conversionService );
				content.load( message.get( CONTENT ) );
				source.fireEvent( new TerminalRemoteMessageEvent( source, content, agent ) );
			}
		}
	}
}
