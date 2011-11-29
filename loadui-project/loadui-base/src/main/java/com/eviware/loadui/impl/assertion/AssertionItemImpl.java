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

import java.io.IOException;

import javax.annotation.Nonnull;

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
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.assertion.ToleranceSupport;
import com.eviware.loadui.util.serialization.SerializationUtils;
import com.eviware.loadui.util.testevents.TestEventSourceSupport;
import com.google.common.base.Objects;

public class AssertionItemImpl<T> implements AssertionItem.Mutable<T>, TestEvent.Source<AssertionFailureEvent>,
		Releasable
{
	protected static final Logger log = LoggerFactory.getLogger( AssertionItemImpl.class );

	private static final String PARENT_ID = "parentId";
	private static final String VALUE_REFERENCE = "valueReference";
	private static final String CONSTRAINT = "constraint";
	private static final String TOLERANCE_ALLOWED_OCCURRENCES = "toleranceAllowedOccurrences";
	private static final String TOLERANCE_PERIOD = "tolerancePeriod";

	private final ToleranceSupport toleranceSupport = new ToleranceSupport();
	private final ValueAsserter valueAsserter = new ValueAsserter();
	private final TestEventSourceSupport sourceSupport;
	private final AssertionAddonImpl addon;
	private final AddonItem.Support addonSupport;
	private final Addressable parent;
	private final ListenableValue<T> listenableValue;

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
			addonSupport.setAttribute( VALUE_REFERENCE, SerializationUtils.serializeBase64( valueResolver ) );
		}
		catch( IOException e )
		{
			log.error( "Unable to serialize value resolver!", e );
		}
		listenableValue = valueResolver.getValue();

		sourceSupport = new TestEventSourceSupport( getLabel(), createData() );
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
					.deserialize( addonSupport.getAttribute( VALUE_REFERENCE, null ) );
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
		listenableValue = tmpValue;

		try
		{
			@SuppressWarnings( "unchecked" )
			Constraint<? super T> constraint = ( Constraint<? super T> )SerializationUtils.deserialize( addonSupport
					.getAttribute( CONSTRAINT, null ) );
			this.constraint = constraint;
		}
		catch( ClassNotFoundException e )
		{
			log.error( "Unable to deserialize Constraint!", e );
		}
		catch( IOException e )
		{
			log.error( "Unable to deserialize Constraint!", e );
		}

		sourceSupport = new TestEventSourceSupport( getLabel(), createData() );
	}

	public void start()
	{
		toleranceSupport.clear();
		listenableValue.addListener( valueAsserter );
	}

	public void stop()
	{
		listenableValue.removeListener( valueAsserter );
	}

	@Override
	public Addressable getParent()
	{
		return parent;
	}

	@Override
	public String getLabel()
	{
		return addonSupport.getAttribute( LABEL, "Assertion" );
	}

	@Override
	public void setLabel( String label )
	{
		addonSupport.setAttribute( LABEL, label );
		sourceSupport.setLabel( label );
	}

	@Override
	public ListenableValue<T> getValue()
	{
		return listenableValue;
	}

	@Override
	public Constraint<? super T> getConstraint()
	{
		return constraint;
	}

	@Override
	public void setConstraint( Constraint<? super T> constraint )
	{
		if( !Objects.equal( this.constraint, constraint ) )
		{
			this.constraint = constraint;

			try
			{
				addonSupport.setAttribute( CONSTRAINT, SerializationUtils.serializeBase64( constraint ) );
			}
			catch( IOException e )
			{
				log.error( "Unable to serialize Constraint!", e );
			}

			sourceSupport.setData( createData() );
		}
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

		sourceSupport.setData( createData() );
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

	@Override
	public Class<AssertionFailureEvent> getType()
	{
		return AssertionFailureEvent.class;
	}

	@Override
	public byte[] getData()
	{
		return sourceSupport.getData();
	}

	@Override
	public String getHash()
	{
		return sourceSupport.getHash();
	}

	private byte[] createData()
	{
		try
		{
			String valueLabel = listenableValue instanceof Labeled ? ( ( Labeled )listenableValue ).getLabel() : String
					.valueOf( listenableValue );

			return SerializationUtils.serialize( new String[] { getId(), valueLabel, String.valueOf( constraint ) } );
		}
		catch( IOException e )
		{
			return new byte[0];
		}
	}

	private class ValueAsserter implements ValueListener<T>
	{
		private final TestEventManager manager = BeanInjector.getBean( TestEventManager.class );

		@Override
		public void update( T value )
		{
			if( !constraint.validate( value ) )
			{
				long timestamp = System.currentTimeMillis();
				if( toleranceSupport.occur( timestamp ) )
				{
					AssertionFailureEvent testEvent = new AssertionFailureEvent( timestamp, AssertionItemImpl.this,
							String.valueOf( value ) );

					manager.logTestEvent( AssertionItemImpl.this, testEvent );
				}
			}
		}
	}
}