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
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.serialization.Resolver;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.collections.CollectionEventSupport;
import com.google.common.collect.ImmutableSet;

public class AssertionAddonImpl implements AssertionAddon, Releasable
{
	private final Addon.Context context;
	private final CollectionEventSupport<AssertionItemImpl<?>, Void> assertionItems;

	public AssertionAddonImpl( Addon.Context context )
	{
		this.context = context;
		assertionItems = new CollectionEventSupport<AssertionItemImpl<?>, Void>( context.getOwner(), ASSERTION_ITEMS );

		for( AddonItem.Support addonItem : context.getAddonItemSupports() )
		{
			@SuppressWarnings( "rawtypes" )
			AssertionItemImpl assertionItem = new AssertionItemImpl( this, addonItem );
			assertionItems.addItem( assertionItem );
		}
	}

	@Override
	public Collection<AssertionItemImpl<?>> getAssertions()
	{
		return assertionItems.getItems();
	}

	@Override
	public AssertionItem.Mutable createAssertion( Addressable owner, Resolver<ListenableValue<?>> listenableValueResolver )
	{
		@SuppressWarnings( { "rawtypes", "unchecked" } )
		AssertionItemImpl<?> assertionItem = new AssertionItemImpl( this, context.createAddonItemSupport(), owner,
				listenableValueResolver );
		assertionItems.addItem( assertionItem );

		return assertionItem;
	}

	@Override
	public void release()
	{
		ReleasableUtils.releaseAll( assertionItems );
	}

	void removeAssertion( AssertionItemImpl<?> assertionItem )
	{
		assertionItems.removeItem( assertionItem );
	}

	private class AssertionExecutionTask implements TestExecutionTask
	{
		@Override
		public void invoke( TestExecution execution, Phase phase )
		{
			switch( phase )
			{
			case PRE_START :
				for( AssertionItemImpl<?> assertionItem : assertionItems.getItems() )
				{
					assertionItem.start();
				}
			}
		}
	}

	public final static class Factory implements Addon.Factory<AssertionAddon>
	{
		private final static Set<Class<?>> eagerTypes = ImmutableSet.<Class<?>> of( ProjectItem.class );

		@Override
		public Class<AssertionAddon> getType()
		{
			return AssertionAddon.class;
		}

		@Override
		public AssertionAddon create( Context context )
		{
			return new AssertionAddonImpl( context );
		}

		@Override
		public Set<Class<?>> getEagerTypes()
		{
			return eagerTypes;
		}
	}
}
