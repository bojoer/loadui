package com.eviware.loadui.impl.assertion;

import javax.annotation.Nonnull;

import com.eviware.loadui.api.addon.AddonItem;
import com.eviware.loadui.api.addressable.Addressable;
import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.assertion.Constraint;
import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.util.BeanInjector;

public class AssertionItemImpl implements AssertionItem.MutableConstraint, AssertionItem.MutableTolerance
{
	private static final String PARENT_ID = "parentId";
	private static final String VALUE_REFERENCE = "valueReference";
	private static final String TOLERANCE_OCCURRENCE_COUNT = "toleranceOccurrenceCount";
	private static final String TOLERANCE_PERIOD = "tolerancePeriod";

	private final AddonItem.Support addonSupport;
	private final Addressable owner;
	private final ListenableValue<?> value;

	public AssertionItemImpl( @Nonnull AddonItem.Support addonSupport )
	{
		this.addonSupport = addonSupport;

		owner = BeanInjector.getBean( AddressableRegistry.class ).lookup( addonSupport.getAttribute( PARENT_ID, "" ) );
		value = null;
	}

	@Override
	public Addressable getParent()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListenableValue<?> getValue()
	{
		// TODO Auto-generated method stub
		return null;
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
		return Integer.parseInt( addonSupport.getAttribute( TOLERANCE_OCCURRENCE_COUNT, "0" ) );
	}

	@Override
	public int getToleranceOccurrenceCount()
	{
		return Integer.parseInt( addonSupport.getAttribute( TOLERANCE_PERIOD, "0" ) );
	}

	@Override
	public String getId()
	{
		return addonSupport.getId();
	}

	@Override
	public void delete()
	{
		addonSupport.delete();
	}

	@Override
	public void setTolerance( int period, int occurrenceCount )
	{
		addonSupport.setAttribute( TOLERANCE_PERIOD, String.valueOf( period ) );
		addonSupport.setAttribute( TOLERANCE_OCCURRENCE_COUNT, String.valueOf( occurrenceCount ) );
	}

	@Override
	public void setConstraint( Constraint<?> constraint )
	{
		// TODO Auto-generated method stub

	}
}
