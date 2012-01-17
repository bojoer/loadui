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
import java.util.EventObject;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.addon.AddonItem;
import com.eviware.loadui.api.addressable.Addressable;
import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.assertion.Constraint;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.serialization.ListenableValue.ValueListener;
import com.eviware.loadui.api.serialization.Resolver;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.assertion.ToleranceSupport;
import com.eviware.loadui.util.events.EventSupport;
import com.eviware.loadui.util.serialization.SerializationUtils;
import com.eviware.loadui.util.testevents.TestEventSourceSupport;
import com.google.common.base.Objects;

public class AssertionItemImpl<T> implements AssertionItem.Mutable<T>, TestEvent.Source<AssertionFailureEvent>,
		EventFirer, Releasable
{
	protected static final Logger log = LoggerFactory.getLogger( AssertionItemImpl.class );

	private static final String PARENT_ID = "parentId";
	private static final String VALUE_REFERENCE = "valueReference";
	private static final String CONSTRAINT = "constraint";
	private static final String TOLERANCE_ALLOWED_OCCURRENCES = "toleranceAllowedOccurrences";
	private static final String TOLERANCE_PERIOD = "tolerancePeriod";

	private final EventSupport eventSupport = new EventSupport();
	private final ToleranceSupport toleranceSupport = new ToleranceSupport();
	private final ValueAsserter valueAsserter = new ValueAsserter();
	private final LabelListener labelListener = new LabelListener();
	private final TestEventSourceSupport sourceSupport;
	private final CanvasItem canvas;
	private final AssertionAddonImpl addon;
	private final AddonItem.Support addonSupport;
	private final Addressable parent;
	private final ListenableValue<T> listenableValue;

	private Constraint<? super T> constraint;
	private String description;

	//Create new AssertionItem
	public AssertionItemImpl( @Nonnull CanvasItem canvas, @Nonnull AssertionAddonImpl addon,
			@Nonnull AddonItem.Support addonSupport, @Nonnull Addressable parent,
			@Nonnull Resolver<? extends ListenableValue<T>> listenableValueResolver )
	{
		this.canvas = canvas;
		this.addon = addon;
		this.addonSupport = addonSupport;
		addonSupport.init( this );

		this.parent = attachLabelListener( parent );
		addonSupport.setAttribute( PARENT_ID, parent.getId() );

		try
		{
			addonSupport.setAttribute( VALUE_REFERENCE, SerializationUtils.serializeBase64( listenableValueResolver ) );
		}
		catch( IOException e )
		{
			log.error( "Unable to serialize value resolver!", e );
		}
		listenableValue = attachLabelListener( listenableValueResolver.getValue() );

		sourceSupport = new TestEventSourceSupport( getLabel(), createData() );

		updateDescription();
	}

	//Load existing AssertionItem
	public AssertionItemImpl( @Nonnull CanvasItem canvas, @Nonnull AssertionAddonImpl addon,
			@Nonnull AddonItem.Support addonSupport )
	{
		this.canvas = canvas;
		this.addon = addon;
		this.addonSupport = addonSupport;
		addonSupport.init( this );

		parent = attachLabelListener( BeanInjector.getBean( AddressableRegistry.class ).lookup(
				addonSupport.getAttribute( PARENT_ID, "" ) ) );

		int tolerancePeriod = Integer.parseInt( addonSupport.getAttribute( TOLERANCE_PERIOD, "0" ) );
		int toleranceAllowedOccurrences = Integer.parseInt( addonSupport
				.getAttribute( TOLERANCE_ALLOWED_OCCURRENCES, "0" ) );
		toleranceSupport.setTolerance( tolerancePeriod, toleranceAllowedOccurrences );

		ListenableValue<T> tmpValue = null;
		try
		{
			@SuppressWarnings( "unchecked" )
			Resolver<? extends ListenableValue<T>> valueResolver = ( Resolver<? extends ListenableValue<T>> )SerializationUtils
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
		listenableValue = attachLabelListener( tmpValue );

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
		updateDescription();
	}

	private <Type> Type attachLabelListener( final Type object )
	{
		if( object instanceof Labeled && object instanceof EventFirer )
		{
			( ( EventFirer )object ).addEventListener( BaseEvent.class, labelListener );
		}

		return object;
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
		if( !Objects.equal( getLabel(), label ) )
		{
			addonSupport.setAttribute( LABEL, label );
			sourceSupport.setLabel( label );
			fireEvent( new BaseEvent( this, LABEL ) );
		}
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
		fireEvent( new BaseEvent( this, DELETED ) );
	}

	@Override
	public void release()
	{
		fireEvent( new BaseEvent( this, RELEASED ) );
		stop();
	}

	@Override
	public <T2 extends EventObject> void addEventListener( Class<T2> type, EventHandler<? super T2> listener )
	{
		eventSupport.addEventListener( type, listener );
	}

	@Override
	public <T2 extends EventObject> void removeEventListener( Class<T2> type, EventHandler<? super T2> listener )
	{
		eventSupport.removeEventListener( type, listener );
	}

	@Override
	public void clearEventListeners()
	{
		eventSupport.clearEventListeners();
	}

	@Override
	public void fireEvent( EventObject event )
	{
		eventSupport.fireEvent( event );
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

	private String labelOrToString( Object object )
	{
		return object instanceof Labeled ? ( ( Labeled )object ).getLabel() : String.valueOf( object );
	}

	private void updateDescription()
	{

		String newDescription = String.format( "%s > %s : %s : Tolerate %d times within %d seconds",
				labelOrToString( getParent() ), labelOrToString( getValue() ), getConstraint(),
				getToleranceAllowedOccurrences(), getTolerancePeriod() );
		if( !newDescription.equals( description ) )
		{
			description = newDescription;
			fireEvent( new BaseEvent( this, DESCRIPTION ) );
		}
	}

	@Override
	public String getDescription()
	{
		return description;
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
			if( constraint != null && !constraint.validate( value ) )
			{
				long timestamp = System.currentTimeMillis();
				if( toleranceSupport.occur( timestamp ) )
				{
					AssertionFailureEvent testEvent = new AssertionFailureEvent( timestamp, AssertionItemImpl.this,
							String.valueOf( value ) );

					manager.logTestEvent( AssertionItemImpl.this, testEvent );
					canvas.getCounter( CanvasItem.FAILURE_COUNTER ).increment();
				}
			}
		}
	}

	private class LabelListener implements WeakEventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( Objects.equal( Labeled.LABEL, event.getKey() ) )
			{
				updateDescription();
			}
		}
	}
}