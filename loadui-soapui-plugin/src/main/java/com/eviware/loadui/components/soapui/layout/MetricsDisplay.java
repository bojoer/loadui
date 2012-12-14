package com.eviware.loadui.components.soapui.layout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.layout.LayoutContainer;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.components.soapui.SoapUISamplerComponent;
import com.eviware.loadui.impl.layout.LayoutComponentImpl;
import com.eviware.loadui.impl.layout.LayoutContainerImpl;
import com.eviware.loadui.impl.layout.PropertyLayoutComponentImpl;
import com.eviware.loadui.util.layout.DelayedFormattedString;
import com.google.common.collect.ImmutableMap;

public class MetricsDisplay
{
	private final DelayedFormattedString displayQueue;
	private final DelayedFormattedString displayDiscarded;
	private final DelayedFormattedString displayRequests;
	private final DelayedFormattedString displayRunning;
	private final DelayedFormattedString displayTotal;
	private final DelayedFormattedString displayFailed;
	private long sampleResetValue;
	private long discardResetValue;
	private long failedResetValue;

	@SuppressWarnings( "unused" )
	private static final Logger log = LoggerFactory.getLogger( MetricsDisplay.class );

	public MetricsDisplay( final SoapUISamplerComponent component )
	{

		displayRequests = new DelayedFormattedString( "%d", 0 )
		{
			@Override
			public void update()
			{
				setValue( String.valueOf( component.getCurrentlyRunning()
						+ ( component.getSampleCounter().get() - sampleResetValue ) ) );
			}
		};

		displayRunning = new DelayedFormattedString( "%d", 0 )
		{
			@Override
			public void update()
			{
				setValue( String.valueOf( component.getCurrentlyRunning() ) );
			}
		};
		displayTotal = new DelayedFormattedString( "%d", 0 )
		{
			@Override
			public void update()
			{
				setValue( String.valueOf( component.getSampleCounter().get() - sampleResetValue ) );
			}
		};
		displayQueue = new DelayedFormattedString( "%d", 0 )
		{
			@Override
			public void update()
			{
				setValue( String.valueOf( component.getQueueSize() ) );
			}
		};
		displayDiscarded = new DelayedFormattedString( "%d", 0 )
		{
			@Override
			public void update()
			{
				setValue( String.valueOf( component.getDiscardCounter().get() - discardResetValue ) );
			}
		};
		displayFailed = new DelayedFormattedString( "%d", 0 )
		{
			@Override
			public void update()
			{
				setValue( String.valueOf( component.getFailureCounter().get() - failedResetValue ) );
			}
		};
	}

	public void setResetValues( long samples, long discarded, long failed )
	{
		sampleResetValue = samples;
		discardResetValue = discarded;
		failedResetValue = failed;
	}

	public void appendMetricsToMessage( final TerminalMessage message )
	{
		message.put( "Requests", Integer.parseInt( displayRequests.getCurrentValue() ) );
		message.put( "Running", Integer.parseInt( displayRunning.getCurrentValue() ) );
		message.put( "Discarded", Integer.parseInt( displayDiscarded.getCurrentValue() ) );
		message.put( "Failed", Integer.parseInt( displayFailed.getCurrentValue() ) );
		message.put( "Queued", Integer.parseInt( displayQueue.getCurrentValue() ) );
		message.put( "Completed", Integer.parseInt( displayTotal.getCurrentValue() ) );
	}

	public LayoutContainer buildLayout()
	{
		LayoutContainer metricsDisplay = new LayoutContainerImpl( ImmutableMap.<String, Object> builder()
				.put( LayoutContainerImpl.LAYOUT_CONSTRAINTS, "wrap 2, align right, h 125!" ) //
				.put( "widget", "display" ) //
				.build() );

		metricsDisplay.add( new LayoutComponentImpl( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.LABEL, "Requests" ) //
				.put( PropertyLayoutComponentImpl.CONSTRAINTS, "w 50!" ).put( "fString", displayRequests ).build() ) ); //

		metricsDisplay.add( new LayoutComponentImpl( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.LABEL, "Running" ) //
				.put( PropertyLayoutComponentImpl.CONSTRAINTS, "w 50!" ).put( "fString", displayRunning ).build() ) ); //

		metricsDisplay.add( new LayoutComponentImpl( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.LABEL, "Completed" ) //
				.put( PropertyLayoutComponentImpl.CONSTRAINTS, "w 60!" ).put( "fString", displayTotal ).build() ) ); //

		metricsDisplay.add( new LayoutComponentImpl( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.LABEL, "Queued" ) //
				.put( PropertyLayoutComponentImpl.CONSTRAINTS, "w 50!" ).put( "fString", displayQueue ).build() ) ); //

		metricsDisplay.add( new LayoutComponentImpl( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.LABEL, "Discarded" ) //
				.put( PropertyLayoutComponentImpl.CONSTRAINTS, "w 50!" ).put( "fString", displayDiscarded ).build() ) ); //

		metricsDisplay.add( new LayoutComponentImpl( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.LABEL, "Failed" ) //
				.put( PropertyLayoutComponentImpl.CONSTRAINTS, "w 60!" ).put( "fString", displayFailed ).build() ) ); //

		return metricsDisplay;
	}

	public void release()
	{
		displayDiscarded.release();
		displayQueue.release();
		displayRunning.release();
		displayTotal.release();
		displayFailed.release();
		displayRequests.release();
	}
}
