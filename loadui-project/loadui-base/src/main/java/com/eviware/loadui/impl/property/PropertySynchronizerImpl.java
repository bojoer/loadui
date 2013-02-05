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
package com.eviware.loadui.impl.property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.events.PropertyEvent.Event;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.property.PropertySynchronizer;
import com.eviware.loadui.util.StringUtils;

public class PropertySynchronizerImpl implements PropertySynchronizer
{
	public final static Logger log = LoggerFactory.getLogger( PropertySynchronizerImpl.class );

	private final static String CHANNEL = "/" + PropertySynchronizerImpl.class.getName();

	private final static String SYNCHRONIZE = "synchronize";

	private final static String MODELITEM = "item";
	private final static String EVENT = "event";
	private final static String PROPERTY_NAME = "propertyName";
	private final static String PROPERTY_TYPE = "propertyType";
	private final static String ARGUMENT = "argument";

	private static String generateSignature( Map<String, String> map )
	{
		List<String> entries = new ArrayList<String>();
		for( Map.Entry<String, String> entry : map.entrySet() )
			entries.add( "[" + entry.getKey() + ":" + entry.getValue() + "]" );
		Collections.sort( entries );
		StringBuilder s = new StringBuilder();
		for( String entry : entries )
			s.append( entry );

		return s.toString();
	}

	private final Set<String> handled = new HashSet<String>();
	private final Map<ModelItem, MessageEndpoint> endpoints = new HashMap<ModelItem, MessageEndpoint>();
	private final Listener messageListener = new Listener();
	private final PropertyEventHandler eventHandler = new PropertyEventHandler();
	private final AddressableRegistry addressableRegistry;
	private final ConversionService conversionService;
	private final ExecutorService executorService;

	public PropertySynchronizerImpl( AddressableRegistry addressableRegistry, ExecutorService executorService,
			ConversionService conversionService )
	{
		this.addressableRegistry = addressableRegistry;
		this.executorService = executorService;
		this.conversionService = conversionService;
	}

	@Override
	public void syncProperties( ModelItem item, MessageEndpoint endpoint )
	{
		if( !endpoints.containsKey( item ) )
		{
			endpoints.put( item, endpoint );
			item.addEventListener( BaseEvent.class, eventHandler );
			endpoint.addMessageListener( CHANNEL, messageListener );
			endpoint.sendMessage( CHANNEL, Collections.singletonMap( SYNCHRONIZE, item.getId() ) );
		}
	}

	@Override
	public void unsyncProperties( ModelItem item )
	{
		if( endpoints.containsKey( item ) )
		{
			item.removeEventListener( BaseEvent.class, eventHandler );
			endpoints.remove( item ).removeMessageListener( messageListener );
		}
	}

	private Map<String, String> createMessage( PropertyEvent event )
	{
		Map<String, String> message = new HashMap<String, String>();
		message.put( MODELITEM, ( ( ModelItem )event.getSource() ).getId() );
		message.put( EVENT, event.getEvent().toString() );
		Property<?> property = event.getProperty();
		message.put( PROPERTY_NAME, property.getKey() );
		message.put( PROPERTY_TYPE, property.getType().getName() );
		switch( event.getEvent() )
		{
		case VALUE :
			if( property.getValue() != null && conversionService.canConvert( property.getType(), Reference.class ) )
				message.put( ARGUMENT, conversionService.convert( property.getValue(), Reference.class ).getId() );
			else
				message.put( ARGUMENT, property.getStringValue() );
			break;
		case RENAMED :
			message.put( ARGUMENT, ( String )event.getPreviousValue() );
			break;
		default :
			message.put( ARGUMENT, "" );
		}

		return message;
	}

	private class PropertyEventHandler implements EventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( event.getSource() instanceof ModelItem )
			{
				ModelItem item = ( ModelItem )event.getSource();
				if( ModelItem.RELEASED.equals( event.getKey() ) )
					unsyncProperties( item );
				else if( event instanceof PropertyEvent )
				{
					PropertyEvent pEvent = ( PropertyEvent )event;
					if( pEvent.getProperty().isPropagated() )
					{
						Map<String, String> message = createMessage( pEvent );
						if( !handled.remove( generateSignature( message ) ) )
						{
							endpoints.get( item ).sendMessage( CHANNEL, message );
						}
					}
				}
			}
		}
	}

	private class Listener implements MessageListener
	{
		@Override
		@SuppressWarnings( "unchecked" )
		public void handleMessage( String channel, final MessageEndpoint endpoint, Object data )
		{
			Map<String, String> message = ( Map<String, String> )data;
			if( message.containsKey( SYNCHRONIZE ) )
			{
				ModelItem item = ( ModelItem )addressableRegistry.lookup( message.get( SYNCHRONIZE ) );
				if( item != null )
				{
					for( Property<?> property : item.getProperties() )
					{
						if( property.isPropagated() )
						{
							endpoint.sendMessage( CHANNEL, createMessage( new PropertyEvent( item, property,
									PropertyEvent.Event.VALUE, property.getValue() ) ) );
						}
					}
				}
				return;
			}

			handled.add( generateSignature( message ) );
			ModelItem item = ( ModelItem )addressableRegistry.lookup( message.get( MODELITEM ) );
			Event event = conversionService.convert( message.get( EVENT ), Event.class );
			if( item == null )
			{
				log.warn( "Got Property Synchronization message for unknown PropertyHolder: {}", event );
				return;
			}
			switch( event )
			{
			case VALUE :
				final Property<?> property = item.getProperty( message.get( PROPERTY_NAME ) );
				if( property != null )
				{
					if( message.get( ARGUMENT ) != null
							&& conversionService.canConvert( property.getType(), Reference.class ) )
					{
						final Reference ref = new Reference( message.get( ARGUMENT ), endpoint );
						executorService.execute( new Runnable()
						{
							@Override
							public void run()
							{
								property.setValue( conversionService.convert( ref, property.getType() ) );
							}
						} );
					}
					else
						property.setValue( StringUtils.fixLineSeparators( message.get( ARGUMENT ) ) );
				}
				break;
			case CREATED :
				try
				{
					item.createProperty( message.get( PROPERTY_NAME ), Class.forName( message.get( PROPERTY_TYPE ) ) );
				}
				catch( ClassNotFoundException e )
				{
					e.printStackTrace();
				}
				break;
			case DELETED :
				item.deleteProperty( message.get( PROPERTY_NAME ) );
				break;
			case RENAMED :
				item.renameProperty( message.get( ARGUMENT ), message.get( PROPERTY_NAME ) );
				break;
			}
		}
	}
}
