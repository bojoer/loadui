package com.eviware.loadui.impl.assertion;

import javax.annotation.Nonnull;

import com.eviware.loadui.api.addon.AddonItem;
import com.eviware.loadui.api.addressable.Addressable;
import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.assertion.Constraint;
import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.serialization.Resolver;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.assertion.ToleranceSupport;

import edu.umd.cs.findbugs.annotations.NonNull;

public class AssertionItemImpl implements AssertionItem.Mutable
{
	private static final String PARENT_ID = "parentId";
	private static final String VALUE_REFERENCE = "valueReference";
	private static final String TOLERANCE_ALLOWED_OCCURRENCES = "toleranceAllowedOccurrences";
	private static final String TOLERANCE_PERIOD = "tolerancePeriod";

	private final ToleranceSupport toleranceSupport = new ToleranceSupport();
	private final AssertionAddonImpl addon;
	private final AddonItem.Support addonSupport;
	private final Addressable parent;
	private final ListenableValue<?> value;

	public AssertionItemImpl( @NonNull AssertionAddonImpl addon, @Nonnull AddonItem.Support addonSupport,
			@NonNull Addressable parent, @NonNull Resolver<ListenableValue<?>> valueResolver )
	{
		this.addon = addon;
		this.addonSupport = addonSupport;

		//TODO: Store in addonSupport!
		this.parent = parent;
		value = valueResolver.getValue();
	}

	public AssertionItemImpl( @NonNull AssertionAddonImpl addon, @Nonnull AddonItem.Support addonSupport )
	{
		this.addon = addon;
		this.addonSupport = addonSupport;

		parent = BeanInjector.getBean( AddressableRegistry.class ).lookup( addonSupport.getAttribute( PARENT_ID, "" ) );

		int tolerancePeriod = Integer.parseInt( addonSupport.getAttribute( TOLERANCE_PERIOD, "0" ) );
		int toleranceAllowedOccurrences = Integer.parseInt( addonSupport
				.getAttribute( TOLERANCE_ALLOWED_OCCURRENCES, "0" ) );
		toleranceSupport.setTolerance( tolerancePeriod, toleranceAllowedOccurrences );

		value = null;
	}

	@Override
	public Addressable getParent()
	{
		return parent;
	}

	@Override
	public ListenableValue<?> getValue()
	{
		return value;
	}

	@Override
	public Constraint<?> getConstraint()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getTolerancePeriod()
	{
		return toleranceSupport.getPeriod();
	}

	@Override
	public int getToleranceAllowedOccurrences()
	{
		return toleranceSupport.getAllowedOccurrences();
	}

	@Override
	public String getId()
	{
		return addonSupport.getId();
	}

	@Override
	public void delete()
	{
		addon.removeAssertion( this );
		addonSupport.delete();
	}

	@Override
	public void setTolerance( int period, int allowedOccurrences )
	{
		addonSupport.setAttribute( TOLERANCE_PERIOD, String.valueOf( period ) );
		addonSupport.setAttribute( TOLERANCE_ALLOWED_OCCURRENCES, String.valueOf( allowedOccurrences ) );
		toleranceSupport.setTolerance( period, allowedOccurrences );
	}

	@Override
	public void setConstraint( Constraint<?> constraint )
	{
		// TODO Auto-generated method stub
	}
}