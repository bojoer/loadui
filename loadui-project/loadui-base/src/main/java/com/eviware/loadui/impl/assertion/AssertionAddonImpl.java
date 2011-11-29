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
package com.eviware.loadui.impl.assertion;

import java.util.Collection;
import java.util.Set;

import com.eviware.loadui.api.addon.Addon;
import com.eviware.loadui.api.addon.AddonItem;
import com.eviware.loadui.api.addressable.Addressable;
import com.eviware.loadui.api.assertion.AssertionAddon;
import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.serialization.Resolver;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.collections.CollectionEventSupport;
import com.google.common.collect.ImmutableSet;

public class AssertionAddonImpl implements AssertionAddon, Releasable
{
	private final AssertionExecutionTask assertionTask = new AssertionExecutionTask();
	private final Addon.Context context;
	private final CanvasItem canvas;
	private final CollectionEventSupport<AssertionItemImpl<?>, Void> assertionItems;

	public AssertionAddonImpl( Addon.Context context, CanvasItem canvas )
	{
		this.context = context;
		this.canvas = canvas;
		assertionItems = new CollectionEventSupport<AssertionItemImpl<?>, Void>( context.getOwner(), ASSERTION_ITEMS );

		for( AddonItem.Support addonItem : context.getAddonItemSupports() )
		{
			@SuppressWarnings( "rawtypes" )
			AssertionItemImpl assertionItem = new AssertionItemImpl( canvas, this, addonItem );
			assertionItems.addItem( assertionItem );
		}

		BeanInjector.getBean( TestRunner.class ).registerTask( assertionTask, Phase.PRE_START, Phase.POST_STOP );
	}

	@Override
	public Collection<AssertionItemImpl<?>> getAssertions()
	{
		return assertionItems.getItems();
	}

	@Override
	public <T> AssertionItem.Mutable<T> createAssertion( Addressable owner,
			Resolver<ListenableValue<T>> listenableValueResolver )
	{
		AssertionItemImpl<T> assertionItem = new AssertionItemImpl<T>( canvas, this, context.createAddonItemSupport(),
				owner, listenableValueResolver );
		assertionItems.addItem( assertionItem );
		if( assertionTask.running )
		{
			assertionItem.start();
		}

		return assertionItem;
	}

	@Override
	public void release()
	{
		BeanInjector.getBean( TestRunner.class ).unregisterTask( assertionTask, Phase.values() );
		ReleasableUtils.releaseAll( assertionItems );
	}

	void removeAssertion( AssertionItemImpl<?> assertionItem )
	{
		assertionItems.removeItem( assertionItem );
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
