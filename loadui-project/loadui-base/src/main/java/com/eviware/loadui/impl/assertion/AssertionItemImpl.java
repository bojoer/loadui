package com.eviware.loadui.impl.assertion;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.addon.AddonItem;
import com.eviware.loadui.api.addressable.Addressable;
import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.assertion.Constraint;
import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.serialization.ListenableValue.ValueListener;
import com.eviware.loadui.api.serialization.Resolver;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.assertion.ToleranceSupport;
import com.eviware.loadui.util.serialization.SerializationUtils;

public class AssertionItemImpl<T> implements AssertionItem.Mutable<T>, Releasable
{
	protected static final Logger log = LoggerFactory.getLogger( AssertionItemImpl.class );

	private static final String PARENT_ID = "parentId";
	private static final String VALUE_REFERENCE = "valueReference";
	private static final String TOLERANCE_ALLOWED_OCCURRENCES = "toleranceAllowedOccurrences";
	private static final String TOLERANCE_PERIOD = "tolerancePeriod";

	private final ToleranceSupport toleranceSupport = new ToleranceSupport();
	private final ValueAsserter valueAsserter = new ValueAsserter();
	private final AssertionAddonImpl addon;
	private final AddonItem.Support addonSupport;
	private final Addressable parent;
	private final ListenableValue<T> value;

	private Constraint<? super T> constraint;

	//Create new AssertionItem
	public AssertionItemImpl( @Nonnull AssertionAddonImpl addon, @Nonnull AddonItem.Support addonSupport,
			@Nonnull Addressable parent, @Nonnull Resolver<ListenableValue<T>> valueResolver )
	{
		this.addon = addon;
		this.addonSupport = addonSupport;
		addonSupport.init( this );

		this.parent = parent;
		try
		{
			addonSupport.setAttribute( VALUE_REFERENCE,
					Base64.encodeBase64String( SerializationUtils.serialize( valueResolver ) ) );
		}
		catch( IOException e )
		{
			log.error( "Unable to serialize value resolver!", e );
		}
		value = valueResolver.getValue();
	}

	//Load existing AssertionItem
	public AssertionItemImpl( @Nonnull AssertionAddonImpl addon, @Nonnull AddonItem.Support addonSupport )
	{
		this.addon = addon;
		this.addonSupport = addonSupport;
		addonSupport.init( this );

		parent = BeanInjector.getBean( AddressableRegistry.class ).lookup( addonSupport.getAttribute( PARENT_ID, "" ) );

		int tolerancePeriod = Integer.parseInt( addonSupport.getAttribute( TOLERANCE_PERIOD, "0" ) );
		int toleranceAllowedOccurrences = Integer.parseInt( addonSupport
				.getAttribute( TOLERANCE_ALLOWED_OCCURRENCES, "0" ) );
		toleranceSupport.setTolerance( tolerancePeriod, toleranceAllowedOccurrences );

		ListenableValue<T> tmpValue = null;
		try
		{
			@SuppressWarnings( "unchecked" )
			Resolver<ListenableValue<T>> valueResolver = ( Resolver<ListenableValue<T>> )SerializationUtils
					.deserialize( Base64.decodeBase64( addonSupport.getAttribute( VALUE_REFERENCE, "" ) ) );
			tmpValue = valueResolver.getValue();
		}
		catch( ClassNotFoundException e )
		{
			log.error( "Unable to deserialize ValueResolver!", e );
		}
		catch( IOException e )
		{
			log.error( "Unable to deserialize ValueResolver!", e );
		}
		value = tmpValue;
	}

	public void start()
	{
		toleranceSupport.clear();
		value.addListener( valueAsserter );
	}

	public void stop()
	{
		value.removeListener( valueAsserter );
	}

	@Override
	public Addressable getParent()
	{
		return parent;
	}

	@Override
	public ListenableValue<T> getValue()
	{
		return value;
	}

	@Override
	public Constraint<? super T> getConstraint()
	{
		// TODO Auto-generated method stub
		return constraint;
	}

	@Override
	public void setConstraint( Constraint<? super T> constraint )
	{
		// TODO Auto-generated method stub
		this.constraint = constraint;
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
	public void setTolerance( int period, int allowedOccurrences )
	{
		addonSupport.setAttribute( TOLERANCE_PERIOD, String.valueOf( period ) );
		addonSupport.setAttribute( TOLERANCE_ALLOWED_OCCURRENCES, String.valueOf( allowedOccurrences ) );
		toleranceSupport.setTolerance( period, allowedOccurrences );
	}

	@Override
	public String getId()
	{
		return addonSupport.getId();
	}

	@Override
	public void delete()
	{
		release();
		addon.removeAssertion( this );
		addonSupport.delete();
	}

	@Override
	public void release()
	{
		stop();
	}

	private class ValueAsserter implements ValueListener<T>
	{
		@Override
		public void update( T value )
		{
			if( !constraint.validate( value ) )
			{
				long timestamp = System.currentTimeMillis();
				if( toleranceSupport.occur( timestamp ) )
				{
					//TODO: Raise failure
					log.error( "Asserted value: {} did not meet Constraint: {}", value, constraint );
				}
			}
		}
	}
}