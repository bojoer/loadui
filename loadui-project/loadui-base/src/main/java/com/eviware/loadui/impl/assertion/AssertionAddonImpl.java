/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.impl.assertion;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.addon.Addon;
import com.eviware.loadui.api.addon.AddonItem;
import com.eviware.loadui.api.addressable.Addressable;
import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.assertion.AssertionAddon;
import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.messaging.BroadcastMessageEndpoint;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.serialization.Resolver;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.collections.CollectionEventSupport;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class AssertionAddonImpl implements AssertionAddon, Releasable
{
	private static final String BASE_CHANNEL = "/" + AssertionAddonImpl.class.getName();

	protected static final Logger log = LoggerFactory.getLogger( AssertionAddonImpl.class );

	private final String channel;
	private final AssertionItemDistributor controllerListener;
	private final AssertionItemAgentListener agentListener;
	private final AssertionExecutionTask assertionTask = new AssertionExecutionTask();
	private final Addon.Context context;
	private final CanvasItem canvas;
	private final CollectionEventSupport<AssertionItemImpl<?>, AddonItem.Support> assertionItems;
	private final boolean isController;

	public AssertionAddonImpl( Addon.Context context, CanvasItem canvas )
	{
		this.context = context;
		this.canvas = canvas;
		assertionItems = new CollectionEventSupport<>( context.getOwner(), ASSERTION_ITEMS );

		for( AddonItem.Support addonItemSupport : context.getAddonItemSupports() )
		{
			try
			{
				@SuppressWarnings( "rawtypes" )
				AssertionItemImpl assertionItem = new AssertionItemImpl( canvas, this, addonItemSupport );
				assertionItems.addItemWith( assertionItem, addonItemSupport );
			}
			catch( Exception e )
			{
				log.error( "Failed adding AssertionItem", e );
			}
		}

		BeanInjector.getBean( TestRunner.class ).registerTask( assertionTask, Phase.PRE_START, Phase.POST_STOP );

		channel = BASE_CHANNEL + "/" + canvas.getId();
		isController = LoadUI.isController();

		if( canvas instanceof SceneItem )
		{
			if( isController )
			{
				agentListener = null;
				canvas.addEventListener( CollectionEvent.class, controllerListener = new AssertionItemDistributor() );
			}
			else
			{
				controllerListener = null;
				BeanInjector.getBean( BroadcastMessageEndpoint.class ).addMessageListener( channel,
						agentListener = new AssertionItemAgentListener() );
			}
		}
		else
		{
			agentListener = null;
			controllerListener = null;
		}
	}

	@Override
	public Collection<AssertionItemImpl<?>> getAssertions()
	{
		return assertionItems.getItems();
	}

	@Override
	public <T> AssertionItem.Mutable<T> createAssertion( Addressable owner,
			Resolver<? extends ListenableValue<T>> listenableValueResolver )
	{
		AddonItem.Support addonItemSupport = context.createAddonItemSupport();
		AssertionItemImpl<T> assertionItem = new AssertionItemImpl<>( canvas, this, addonItemSupport, owner,
				listenableValueResolver );
		assertionItems.addItemWith( assertionItem, addonItemSupport );
		if( assertionTask.running )
		{
			assertionItem.start();
		}

		log.debug( "Assertion created" );
		return assertionItem;
	}

	@Override
	public void release()
	{
		if( controllerListener != null )
		{
			canvas.removeEventListener( CollectionEvent.class, controllerListener );
		}
		else if( agentListener != null )
		{
			BeanInjector.getBean( BroadcastMessageEndpoint.class ).removeMessageListener( agentListener );
		}

		BeanInjector.getBean( TestRunner.class ).unregisterTask( assertionTask, Phase.values() );
		ReleasableUtils.releaseAll( assertionItems );
	}

	String getChannel()
	{
		return channel;
	}

	void removeAssertion( AssertionItemImpl<?> assertionItem )
	{
		assertionItems.removeItem( assertionItem );
	}

	void logFailure( AssertionItemImpl<?> source, AssertionFailureEvent event )
	{
		if( isController )
		{
			BeanInjector.getBean( TestEventManager.class ).logTestEvent( source, event );
		}
	}

	private class AssertionExecutionTask implements TestExecutionTask
	{
		private boolean running = false;

		@Override
		public void invoke( TestExecution execution, Phase phase )
		{
			switch( phase )
			{
			case PRE_START :
				running = true;
				for( AssertionItemImpl<?> assertionItem : assertionItems.getItems() )
				{
					assertionItem.start();
				}
				break;
			case POST_STOP :
				running = false;
				for( AssertionItemImpl<?> assertionItem : assertionItems.getItems() )
				{
					assertionItem.stop();
				}
				break;
			}
		}
	}

	private class AssertionItemDistributor implements EventHandler<CollectionEvent>
	{
		@Override
		public void handleEvent( CollectionEvent event )
		{
			if( ASSERTION_ITEMS.equals( event.getKey() ) )
			{
				AssertionItemImpl<?> assertionItem = ( AssertionItemImpl<?> )event.getElement();
				switch( event.getEvent() )
				{
				case ADDED :
					String exportedAssertionItem = context.exportAddonItemSupport( assertionItems
							.getAttachment( assertionItem ) );
					send( Collections.singletonMap( assertionItem.getId(), exportedAssertionItem ) );
					break;
				case REMOVED :
					send( Collections.singletonMap( assertionItem.getId(), null ) );
					break;
				}
			}
		}

		private void send( Object data )
		{
			canvas.getProject().broadcastMessage( ( SceneItem )canvas, channel, data );
		}
	}

	private class AssertionItemAgentListener implements MessageListener
	{
		@Override
		public void handleMessage( String chan, MessageEndpoint endpoint, Object data )
		{
			@SuppressWarnings( "unchecked" )
			Map<String, String> map = ( Map<String, String> )data;

			String id = Iterables.getFirst( map.keySet(), null );
			AddressableRegistry registry = BeanInjector.getBean( AddressableRegistry.class );
			AssertionItem<?> oldAssertionItem = ( AssertionItem<?> )registry.lookup( id );
			if( oldAssertionItem != null )
			{
				oldAssertionItem.delete();
			}

			String assertionItemData = map.get( id );
			if( assertionItemData != null )
			{
				AddonItem.Support addonItemSupport = context.importAddonItemSupport( assertionItemData );
				@SuppressWarnings( "rawtypes" )
				AssertionItemImpl assertionItem = new AssertionItemImpl( canvas, AssertionAddonImpl.this, addonItemSupport );
				assertionItems.addItemWith( assertionItem, addonItemSupport );
				if( assertionTask.running )
				{
					assertionItem.start();
				}
			}
		}
	}

	public final static class Factory implements Addon.Factory<AssertionAddon>
	{
		private final static Set<Class<?>> eagerTypes = ImmutableSet.<Class<?>> of( CanvasItem.class );

		@Override
		public Class<AssertionAddon> getType()
		{
			return AssertionAddon.class;
		}

		@Override
		public AssertionAddon create( Context context )
		{
			if( context.getOwner() instanceof CanvasItem )
			{
				return new AssertionAddonImpl( context, ( CanvasItem )context.getOwner() );
			}
			throw new IllegalArgumentException( "AssertionAddon is only applicable for CanvasItems!" );
		}

		@Override
		public Set<Class<?>> getEagerTypes()
		{
			return eagerTypes;
		}
	}
}
