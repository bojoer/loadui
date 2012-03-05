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
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.addon.AddonItem;
import com.eviware.loadui.api.addressable.Addressable;
import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.assertion.Constraint;
import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.messaging.BroadcastMessageEndpoint;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.serialization.ListenableValue.ValueListener;
import com.eviware.loadui.api.serialization.Resolver;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.FormattingUtils;
import com.eviware.loadui.util.assertion.ToleranceSupport;
import com.eviware.loadui.util.events.EventSupport;
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

	private final EventSupport eventSupport = new EventSupport();
	private final ToleranceSupport conditionTolerance = new ToleranceSupport();
	private final FailureGrouper failureGrouper = new FailureGrouper();
	private final ValueAsserter valueAsserter = new ValueAsserter();
	private final LabelListener labelListener = new LabelListener();
	private final ResetListener resetListener = new ResetListener();
	private final String channel;
	private final TestEventSourceSupport sourceSupport;
	private final CanvasItem canvas;
	private final AssertionAddonImpl addon;
	private final AddonItem.Support addonSupport;
	private final Addressable parent;
	private final ListenableValue<T> listenableValue;

	private Constraint<? super T> constraint;
	private String description;
	private RemoteFailureListener failureListener;
	private long failures = 0;

	//Create new AssertionItem
	public AssertionItemImpl( @Nonnull CanvasItem canvas, @Nonnull AssertionAddonImpl addon,
			@Nonnull AddonItem.Support addonSupport, @Nonnull Addressable parent,
			@Nonnull Resolver<? extends ListenableValue<T>> listenableValueResolver )
	{
		this.canvas = canvas;
		this.addon = addon;
		this.addonSupport = addonSupport;
		addonSupport.init( this );
		channel = "/" + getId();

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
		canvas.addEventListener( ActionEvent.class, resetListener );
		updateDescription();

		BeanInjector.getBean( TestRunner.class ).registerTask( failureGrouper, Phase.STOP );

		if( LoadUI.isController() )
		{
			failureListener = new RemoteFailureListener();
			BeanInjector.getBean( BroadcastMessageEndpoint.class ).addMessageListener( channel, failureListener );
		}
	}

	//Load existing AssertionItem
	public AssertionItemImpl( @Nonnull CanvasItem canvas, @Nonnull AssertionAddonImpl addon,
			@Nonnull AddonItem.Support addonSupport )
	{
		this.canvas = canvas;
		this.addon = addon;
		this.addonSupport = addonSupport;
		addonSupport.init( this );
		channel = "/" + getId();

		parent = attachLabelListener( BeanInjector.getBean( AddressableRegistry.class ).lookup(
				addonSupport.getAttribute( PARENT_ID, "" ) ) );

		int tolerancePeriod = Integer.parseInt( addonSupport.getAttribute( TOLERANCE_PERIOD, "0" ) );
		int toleranceAllowedOccurrences = Integer.parseInt( addonSupport
				.getAttribute( TOLERANCE_ALLOWED_OCCURRENCES, "0" ) );
		conditionTolerance.setTolerance( tolerancePeriod, toleranceAllowedOccurrences );

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
		canvas.addEventListener( ActionEvent.class, resetListener );
		updateDescription();

		BeanInjector.getBean( TestRunner.class ).registerTask( failureGrouper, Phase.STOP );

		if( LoadUI.isController() )
		{
			failureListener = new RemoteFailureListener();
			BeanInjector.getBean( BroadcastMessageEndpoint.class ).addMessageListener( channel, failureListener );
		}
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
		conditionTolerance.clear();
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
			updateDescription();
		}
	}

	@Override
	public int getTolerancePeriod()
	{
		return conditionTolerance.getPeriod();
	}

	@Override
	public int getToleranceAllowedOccurrences()
	{
		return conditionTolerance.getAllowedOccurrences();
	}

	@Override
	public void setTolerance( int period, int allowedOccurrences )
	{
		addonSupport.setAttribute( TOLERANCE_PERIOD, String.valueOf( period ) );
		addonSupport.setAttribute( TOLERANCE_ALLOWED_OCCURRENCES, String.valueOf( allowedOccurrences ) );
		conditionTolerance.setTolerance( period, allowedOccurrences );

		sourceSupport.setData( createData() );
		updateDescription();
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
		canvas.removeEventListener( ActionEvent.class, resetListener );
		if( listenableValue instanceof EventFirer )
		{
			( ( EventFirer )listenableValue ).addEventListener( BaseEvent.class, labelListener );
		}

		if( failureListener != null )
		{
			BeanInjector.getBean( BroadcastMessageEndpoint.class ).removeMessageListener( failureListener );
		}

		BeanInjector.getBean( TestRunner.class ).unregisterTask( failureGrouper, Phase.values() );

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
	public long getFailureCount()
	{
		return failures;
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

		String newDescription = String.format( "%s > %s : %s : Tolerate %d times within %s seconds",
				labelOrToString( getParent() ), String.valueOf( getValue() ), getConstraint(),
				getToleranceAllowedOccurrences(), FormattingUtils.formatNumber( getTolerancePeriod() / 1000.0, 2 ) );
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
		@Override
		public void update( T value )
		{
			if( constraint != null )
			{
				if( !constraint.validate( value ) )
				{
					long timestamp = System.currentTimeMillis();

					if( conditionTolerance.occur( timestamp ) )
					{
						failureGrouper.append( value, timestamp );
						canvas.getCounter( CanvasItem.FAILURE_COUNTER ).increment();
						canvas.getCounter( CanvasItem.ASSERTION_FAILURE_COUNTER ).increment();
						failures++ ;
						fireEvent( new BaseEvent( AssertionItemImpl.this, FAILURE_COUNT ) );
					}
				}
				canvas.getCounter( CanvasItem.ASSERTION_COUNTER ).increment();
			}
		}
	}

	private class FailureGrouper implements Runnable, TestExecutionTask
	{
		private static final int GROUPING_PERIOD = 1000;
		private static final int GROUPING_COUNT = 4;

		private final ScheduledExecutorService executor = BeanInjector.getBean( ScheduledExecutorService.class );
		private final TestEventManager manager = BeanInjector.getBean( TestEventManager.class );
		private final LinkedBlockingDeque<Entry> entries = new LinkedBlockingDeque<Entry>();

		private ScheduledFuture<?> runFuture;
		private long deadline = 0;

		public void append( T value, long timestamp )
		{
			if( entries.isEmpty() )
			{
				deadline = timestamp + GROUPING_PERIOD;
				runFuture = executor.schedule( this, GROUPING_PERIOD, TimeUnit.MILLISECONDS );
			}

			entries.add( new Entry( value, timestamp ) );
		}

		@Override
		public void run()
		{
			int count = entries.size();
			if( count == 0 )
			{
				return;
			}

			if( count >= GROUPING_COUNT )
			{
				manager.logTestEvent( AssertionItemImpl.this, new AssertionFailureEvent.Group( entries.getFirst().timestamp
						+ GROUPING_PERIOD / 2, AssertionItemImpl.this, count ) );

				entries.clear();
			}
			else
			{
				while( entries.getFirst().timestamp < deadline )
				{
					Entry entry = entries.removeFirst();
					manager.logTestEvent( AssertionItemImpl.this, new AssertionFailureEvent( entry.timestamp,
							AssertionItemImpl.this, String.valueOf( entry.value ) ) );
				}

				if( !entries.isEmpty() )
				{
					long timeUntilNext = entries.getFirst().timestamp - deadline;
					deadline = entries.getFirst().timestamp + GROUPING_PERIOD;
					runFuture = executor.schedule( this, GROUPING_PERIOD + timeUntilNext, TimeUnit.MILLISECONDS );
				}
			}

			if( !LoadUI.isController() )
			{
				BeanInjector.getBean( BroadcastMessageEndpoint.class ).sendMessage( channel, count );
			}
		}

		@Override
		public void invoke( TestExecution execution, Phase phase )
		{
			if( phase == Phase.STOP )
			{
				if( runFuture != null && runFuture.cancel( true ) )
				{
					deadline += GROUPING_PERIOD;
					executor.execute( this );
				}
			}
		}

		private class Entry
		{
			private final T value;
			private final long timestamp;

			private Entry( T value, long timestamp )
			{
				this.value = value;
				this.timestamp = timestamp;
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

	private class ResetListener implements WeakEventHandler<ActionEvent>
	{
		@Override
		public void handleEvent( ActionEvent event )
		{
			if( Objects.equal( CounterHolder.COUNTER_RESET_ACTION, event.getKey() ) )
			{
				failures = 0;
				fireEvent( new BaseEvent( AssertionItemImpl.this, FAILURE_COUNT ) );
			}
		}
	}

	private class RemoteFailureListener implements MessageListener
	{
		@Override
		public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
		{
			failures += ( ( Number )data ).longValue();
			fireEvent( new BaseEvent( AssertionItemImpl.this, FAILURE_COUNT ) );
		}
	}
}