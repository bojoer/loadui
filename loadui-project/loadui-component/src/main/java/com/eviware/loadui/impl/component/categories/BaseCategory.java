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
package com.eviware.loadui.impl.component.categories;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.component.ComponentBehavior;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.CollectionEvent.Event;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.Assignment;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.serialization.Value;
import com.eviware.loadui.api.summary.MutableChapter;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.google.common.collect.Maps;

/**
 * Base class for component Categories which defines default implementations of
 * all required ComponentBehavior methods (which do nothing).
 * 
 * @author dain.nilsson
 */
public abstract class BaseCategory implements ComponentBehavior
{
	public static final Logger log = LoggerFactory.getLogger( BaseCategory.class );

	private static final String TOTAL_DATA = BaseCategory.class.getName() + "@totalData";

	private final ComponentContext context;
	private final ActionListener listener = new ActionListener();
	private final AssignmentListener assignmentListener;
	private final Map<String, Callable<Number>> totalCallables = Maps.newHashMap();
	private final Map<String, Map<String, Number>> totalValues = Maps.newHashMap();

	@GuardedBy( "this" )
	private ScheduledExecutorService executor = null;

	/**
	 * Constructs a new BaseCategory.
	 * 
	 * @param context
	 *           A ComponentContext to bind the BaseCategory to.
	 */
	public BaseCategory( ComponentContext context )
	{
		this.context = context;
		context.getComponent().addEventListener( ActionEvent.class, listener );
		if( LoadUI.isController() )
		{
			context.getComponent().getCanvas().getProject()
					.addEventListener( CollectionEvent.class, assignmentListener = new AssignmentListener() );
		}
		else
		{
			assignmentListener = null;
		}
	}

	/**
	 * Returns the bound ComponentContext.
	 * 
	 * @return The ComponentContext.
	 */
	protected ComponentContext getContext()
	{
		return context;
	}

	/**
	 * Invoked when a request is made to cancel the current action and bring the
	 * Component into a non-busy state.
	 */
	protected void cancel()
	{
	}

	@Override
	public synchronized void onRelease()
	{
		context.removeEventListener( ActionEvent.class, listener );
		totalCallables.clear();
		totalValues.clear();
		if( executor != null )
		{
			executor.shutdownNow();
			executor = null;
		}
		if( assignmentListener != null )
		{
			context.getComponent().getCanvas().getProject()
					.removeEventListener( CollectionEvent.class, assignmentListener );
		}
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void onTerminalConnect( OutputTerminal output, InputTerminal input )
	{
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void onTerminalDisconnect( OutputTerminal output, InputTerminal input )
	{
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void onTerminalMessage( OutputTerminal output, InputTerminal input, TerminalMessage message )
	{
		if( message.containsKey( TOTAL_DATA ) )
		{
			String source = output.getId();
			if( !context.getCanvas().getProject().getWorkspace().isLocalMode() && isAssigned( source ) )
			{
				for( String key : totalCallables.keySet() )
				{
					totalValues.get( key ).put( source, ( Number )message.get( key ) );
				}
			}
		}
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void onTerminalSignatureChange( OutputTerminal output, Map<String, Class<?>> signature )
	{
	}

	@Override
	public Object collectStatisticsData()
	{
		return null;
	}

	@Override
	public void handleStatisticsData( Map<AgentItem, Object> statisticsData )
	{
	}

	@Override
	public void generateSummary( MutableChapter summary )
	{
	}

	private boolean isAssigned( String id )
	{

		String agentId = id.split( "/" )[1];
		CanvasItem canvasItem = context.getCanvas();
		for( AgentItem agent : canvasItem.getProject().getAgentsAssignedTo( ( SceneItem )canvasItem ) )
		{
			if( agent.getId().equals( agentId ) )
			{
				return true;
			}
		}

		return false;
	}

	public synchronized void removeTotal( String name )
	{
		totalCallables.remove( name );
		totalValues.remove( name );
	}

	public synchronized Value<Number> createTotal( String name, Callable<Number> value )
	{
		totalCallables.put( name, value );
		totalValues.put( name, new HashMap<String, Number>() );

		if( !LoadUI.isController() && executor == null )
		{
			executor = Executors.newSingleThreadScheduledExecutor();
			executor.scheduleAtFixedRate( new Runnable()
			{
				@Override
				public void run()
				{
					if( totalCallables.isEmpty() )
					{
						return;
					}

					TerminalMessage message = context.newMessage();
					message.put( TOTAL_DATA, null );
					for( Map.Entry<String, Callable<Number>> entry : totalCallables.entrySet() )
					{
						try
						{
							message.put( entry.getKey(), entry.getValue().call() );
						}
						catch( Exception e )
						{
							log.error( "Error getting value for total: " + entry.getKey(), e );
						}
					}
					context.send( context.getControllerTerminal(), message );
				}
			}, 1, 1, TimeUnit.SECONDS );
		}

		return new TotalValue( name );
	}

	private class ActionListener implements WeakEventHandler<ActionEvent>
	{
		@Override
		public void handleEvent( ActionEvent event )
		{
			if( ComponentItem.CANCEL_ACTION.equals( event.getKey() ) )
			{
				cancel();
			}
			else if( ComponentItem.COUNTER_RESET_ACTION.equals( event.getKey() ) )
			{
				for( Map<String, Number> value : totalValues.values() )
				{
					value.clear();
				}
			}
		}
	}

	private class AssignmentListener implements EventHandler<CollectionEvent>
	{
		@Override
		public void handleEvent( CollectionEvent event )
		{
			if( ProjectItem.ASSIGNMENTS.equals( event.getKey() ) && event.getEvent() == Event.REMOVED )
			{
				Assignment assignment = ( Assignment )event.getElement();
				String agentId = assignment.getAgent().getId();
				for( Map.Entry<String, Map<String, Number>> entry : totalValues.entrySet() )
				{
					entry.getValue().remove( agentId );
				}
			}
		}
	}

	private class TotalValue implements Value<Number>
	{
		private final String key;

		private TotalValue( String key )
		{
			this.key = key;
		}

		@Override
		public Class<Number> getType()
		{
			return Number.class;
		}

		@Override
		public Number getValue()
		{
			double doubleSum = 0;
			long longSum = 0;
			boolean useDouble = false;

			try
			{
				Callable<Number> callable = totalCallables.get( key );
				if( callable == null )
				{
					//Total has been removed.
					return 0;
				}

				Number value = callable.call();
				doubleSum = value.doubleValue();
				useDouble = value instanceof Double || value instanceof Float;
				if( !useDouble )
				{
					longSum = value.longValue();
				}
			}
			catch( Exception e )
			{
				log.error( "Error getting value for total: " + key, e );
			}

			for( Number number : totalValues.get( key ).values() )
			{
				doubleSum += number.doubleValue();
				if( !useDouble )
				{
					useDouble = number instanceof Double || number instanceof Float;
					longSum += number.longValue();
				}
			}

			if( useDouble )
			{
				return doubleSum;
			}
			else
			{
				return longSum;
			}
		}
	}
}
