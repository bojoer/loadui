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
package com.eviware.loadui.impl.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.springframework.core.convert.ConversionService;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.component.ActivityStrategy;
import com.eviware.loadui.api.component.ComponentBehavior;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.counter.Counter;
import com.eviware.loadui.api.counter.CounterSynchronizer;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.ActivityEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.CollectionEvent.Event;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.events.RemoteActionEvent;
import com.eviware.loadui.api.events.TerminalConnectionEvent;
import com.eviware.loadui.api.events.TerminalEvent;
import com.eviware.loadui.api.events.TerminalMessageEvent;
import com.eviware.loadui.api.events.TerminalSignatureEvent;
import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.layout.SettingsLayoutContainer;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.Assignment;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.summary.MutableChapter;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.DualTerminal;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.api.terminal.TerminalHolder;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.config.ComponentItemConfig;
import com.eviware.loadui.impl.counter.CounterSupport;
import com.eviware.loadui.impl.counter.RemoteAggregatedCounterSupport;
import com.eviware.loadui.impl.statistics.StatisticHolderSupport;
import com.eviware.loadui.impl.terminal.InputTerminalImpl;
import com.eviware.loadui.impl.terminal.OutputTerminalImpl;
import com.eviware.loadui.impl.terminal.TerminalHolderSupport;
import com.eviware.loadui.impl.terminal.TerminalMessageImpl;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.ReleasableUtils;

public class ComponentItemImpl extends ModelItemImpl<ComponentItemConfig> implements ComponentItem
{
	private final ExecutorService executor;
	private final ConversionService conversionService;
	private final CanvasItem canvas;
	private final TerminalHolderSupport terminalHolderSupport;
	private final StatisticHolderSupport statisticHolderSupport;
	private final Context context = new Context();
	private final CanvasListener canvasListener = new CanvasListener();
	private final WorkspaceListener workspaceListener;
	private final ProjectListener projectListener;
	private final CounterSupport counterSupport;

	private ComponentBehavior behavior;
	private LayoutComponent layout;
	private LayoutComponent compactLayout;
	private final Set<SettingsLayoutContainer> settingsTabs = new LinkedHashSet<SettingsLayoutContainer>();
	private boolean nonBlocking = false;
	private String helpUrl = "http://www.loadui.org";
	private boolean invalid = false;
	private boolean busy = false;

	private boolean propagate = true;
	private final DualTerminal remoteTerminal = new RemoteTerminal();
	private final DualTerminal controllerTerminal = new ControllerTerminal();
	private final Map<AgentItem, AgentTerminal> agentTerminals = new HashMap<AgentItem, AgentTerminal>();

	private ActivityStrategy activityStrategy;
	private final ActivityListener activityListener = new ActivityListener();

	public ComponentItemImpl( CanvasItem canvas, ComponentItemConfig config )
	{
		super( config );
		this.canvas = canvas;

		executor = BeanInjector.getBean( ExecutorService.class );
		conversionService = BeanInjector.getBean( ConversionService.class );

		counterSupport = LoadUI.CONTROLLER.equals( System.getProperty( LoadUI.INSTANCE ) ) ? new RemoteAggregatedCounterSupport(
				BeanInjector.getBean( CounterSynchronizer.class ) ) : new CounterSupport();

		workspaceListener = LoadUI.CONTROLLER.equals( System.getProperty( LoadUI.INSTANCE ) )
				&& canvas instanceof SceneItem ? new WorkspaceListener() : null;

		projectListener = LoadUI.CONTROLLER.equals( System.getProperty( LoadUI.INSTANCE ) )
				&& canvas instanceof SceneItem ? new ProjectListener() : null;

		terminalHolderSupport = new TerminalHolderSupport( this );
		statisticHolderSupport = new StatisticHolderSupport( this );
	}

	@Override
	public void init()
	{
		counterSupport.init( this );
		super.init();
		canvas.addEventListener( ActionEvent.class, canvasListener );

		statisticHolderSupport.init();

		if( workspaceListener != null )
		{
			WorkspaceItem workspace = getCanvas().getProject().getWorkspace();
			workspace.addEventListener( PropertyEvent.class, workspaceListener );
			propagate = workspace.isLocalMode();
		}

		if( projectListener != null )
		{
			getCanvas().getProject().addEventListener( CollectionEvent.class, projectListener );
			for( AgentItem agent : getCanvas().getProject().getAgentsAssignedTo( ( SceneItem )getCanvas() ) )
				getAgentTerminal( agent );
		}
	}

	public void setBehavior( ComponentBehavior behavior )
	{
		if( isReleased() )
			return;

		this.behavior = behavior;
	}

	@Override
	public ComponentContext getContext()
	{
		return context;
	}

	@Override
	public void handleTerminalEvent( InputTerminal input, TerminalEvent event )
	{
		if( !propagate && event instanceof TerminalMessageEvent )
			return;

		doHandleTerminalEvent( input, event );
	}

	private void doHandleTerminalEvent( final InputTerminal input, final TerminalEvent event )
	{
		TerminalEventHandler terminalEventHandler = new TerminalEventHandler( event, input );
		if( nonBlocking )
			terminalEventHandler.run();
		else
			executor.execute( terminalEventHandler );
	}

	@Override
	public void fireEvent( EventObject event )
	{
		if( !propagate && event instanceof ActionEvent )
		{
			if( event.getSource() == this )
				super.fireEvent( new RemoteActionEvent( this, ( ActionEvent )event ) );
			if( COUNTER_RESET_ACTION.equals( ( ( ActionEvent )event ).getKey() ) )
				super.fireEvent( event );
		}
		else
			super.fireEvent( event );
	}

	@Override
	public CanvasItem getCanvas()
	{
		return canvas;
	}

	@Override
	public Collection<Terminal> getTerminals()
	{
		return terminalHolderSupport.getTerminals();
	}

	@Override
	public Terminal getTerminalByName( String name )
	{
		return terminalHolderSupport.getTerminalByName( name );
	}

	@Override
	public String getType()
	{
		return getAttribute( TYPE, "" );
	}

	@Override
	public String getCategory()
	{
		return getConfig().getCategory();
	}

	@Override
	public String getColor()
	{
		return behavior == null ? "#000000" : behavior.getColor();
	}

	public void setCategory( String category )
	{
		if( !category.equals( getCategory() ) )
		{
			getConfig().setCategory( category );
			fireBaseEvent( CATEGORY );
		}
	}

	@Override
	public void release()
	{
		canvas.removeEventListener( ActionEvent.class, canvasListener );
		if( workspaceListener != null )
			getCanvas().getProject().getWorkspace().removeEventListener( PropertyEvent.class, workspaceListener );
		if( projectListener != null )
			getCanvas().getProject().removeEventListener( CollectionEvent.class, projectListener );
		if( getCanvas().isRunning() )
			triggerAction( CanvasItem.STOP_ACTION );
		if( behavior != null )
			behavior.onRelease();

		ReleasableUtils.releaseAll( terminalHolderSupport, statisticHolderSupport, layout, compactLayout, settingsTabs );

		settingsTabs.clear();
		layout = null;
		compactLayout = null;

		super.release();

		behavior = null;
		if( activityStrategy != null )
			activityStrategy.removeEventListener( ActivityEvent.class, activityListener );
	}

	@Override
	public void delete()
	{
		for( Terminal terminal : getTerminals() )
			for( Connection connection : terminal.getConnections().toArray(
					new Connection[terminal.getConnections().size()] ) )
				connection.disconnect();

		super.delete();
	}

	@Override
	public String getHelpUrl()
	{
		return helpUrl;
	}

	@Override
	public LayoutComponent getLayout()
	{
		return layout;
	}

	@Override
	public LayoutComponent getCompactLayout()
	{
		return compactLayout;
	}

	@Override
	public Collection<SettingsLayoutContainer> getSettingsTabs()
	{
		return Collections.unmodifiableSet( settingsTabs );
	}

	@Override
	public ComponentBehavior getBehavior()
	{
		return behavior;
	}

	@Override
	public Counter getCounter( String counterName )
	{
		if( CanvasItem.TIMER_COUNTER.equals( counterName ) )
			return getCanvas().getCounter( CanvasItem.TIMER_COUNTER );

		return counterSupport.getCounter( counterName );
	}

	@Override
	public Collection<String> getCounterNames()
	{
		return counterSupport.getCounterNames();
	}

	@Override
	public boolean isInvalid()
	{
		return invalid;
	}

	@Override
	public void setInvalid( boolean state )
	{
		if( invalid != state )
		{
			invalid = state;
			fireBaseEvent( INVALID );
		}
	}

	@Override
	public boolean isBusy()
	{
		return busy;
	}

	@Override
	public void setBusy( boolean state )
	{
		if( busy != state )
		{
			busy = state;
			fireBaseEvent( BUSY );
		}
	}

	@Override
	public boolean isActive()
	{
		return activityStrategy == null ? false : activityStrategy.isActive();
	}

	@Override
	public void generateSummary( MutableChapter summary )
	{
		if( behavior != null )
			behavior.generateSummary( summary );
	}

	@Override
	public StatisticVariable getStatisticVariable( String statisticVariableName )
	{
		return statisticHolderSupport.getStatisticVariable( statisticVariableName );
	}

	@Override
	public Set<String> getStatisticVariableNames()
	{
		return statisticHolderSupport.getStatisticVariableNames();
	}

	public void sendAgentMessage( AgentItem agent, TerminalMessage message )
	{
		if( agent.isReady() )
		{
			doHandleTerminalEvent( remoteTerminal, new TerminalMessageEvent( getAgentTerminal( agent ), message ) );
		}
	}

	private AgentTerminal getAgentTerminal( AgentItem agent )
	{
		if( !agentTerminals.containsKey( agent ) )
		{
			AgentTerminal agentTerminal = new AgentTerminal( agent );
			agentTerminals.put( agent, agentTerminal );
			fireCollectionEvent( ComponentContext.AGENT_TERMINALS, Event.ADDED, agentTerminal );
		}

		return agentTerminals.get( agent );
	}

	private class TerminalEventHandler implements Runnable
	{
		private final TerminalEvent event;
		private final InputTerminal input;

		public TerminalEventHandler( TerminalEvent event, InputTerminal input )
		{
			this.event = event;
			this.input = input;
		}

		@Override
		public void run()
		{
			if( behavior == null )
				return;
			OutputTerminal output = event.getOutputTerminal();
			if( event instanceof TerminalMessageEvent )
				behavior.onTerminalMessage( output, input, ( ( TerminalMessageEvent )event ).getMessage().copy() );
			else if( event instanceof TerminalSignatureEvent )
				behavior.onTerminalSignatureChange( output, ( ( TerminalSignatureEvent )event ).getSignature() );
			else if( event instanceof TerminalConnectionEvent )
			{
				if( ( ( TerminalConnectionEvent )event ).getEvent() == TerminalConnectionEvent.Event.CONNECT )
					behavior.onTerminalConnect( output, input );
				else if( ( ( TerminalConnectionEvent )event ).getEvent() == TerminalConnectionEvent.Event.DISCONNECT )
					behavior.onTerminalDisconnect( output, input );
				fireEvent( event );
			}
		}
	}

	private class CanvasListener implements EventHandler<ActionEvent>
	{
		@Override
		public void handleEvent( ActionEvent event )
		{
			if( CanvasItem.COMPLETE_ACTION.equals( event.getKey() ) )
			{
				fireEvent( new ActionEvent( event.getSource(), CanvasItem.STOP_ACTION ) );
			}
			fireEvent( event );
		}
	}

	private class WorkspaceListener implements EventHandler<PropertyEvent>
	{
		@Override
		public void handleEvent( PropertyEvent event )
		{
			if( WorkspaceItem.LOCAL_MODE_PROPERTY.equals( event.getProperty().getKey() ) )
				propagate = ( Boolean )event.getProperty().getValue();
		}
	}

	private class ProjectListener implements EventHandler<CollectionEvent>
	{
		@Override
		public void handleEvent( CollectionEvent event )
		{
			if( ProjectItem.ASSIGNMENTS.equals( event.getKey() ) )
			{
				Assignment assignment = ( Assignment )event.getElement();
				SceneItem scene = assignment.getScene();
				if( scene == getCanvas() )
				{
					AgentItem agent = assignment.getAgent();
					AgentTerminal agentTerminal;
					if( CollectionEvent.Event.ADDED == event.getEvent() && !agentTerminals.containsKey( agent ) )
					{
						agentTerminal = new AgentTerminal( agent );
						agentTerminals.put( agent, agentTerminal );
						fireCollectionEvent( ComponentContext.AGENT_TERMINALS, event.getEvent(), agentTerminal );
					}
					else if( CollectionEvent.Event.REMOVED == event.getEvent() && agentTerminals.containsKey( agent ) )
					{
						agentTerminal = agentTerminals.remove( agent );
						fireCollectionEvent( ComponentContext.AGENT_TERMINALS, event.getEvent(), agentTerminal );
					}
				}
			}
		}
	}

	private class ActivityListener implements EventHandler<ActivityEvent>
	{
		@Override
		public void handleEvent( ActivityEvent event )
		{
			fireBaseEvent( ACTIVITY );
		}
	}

	private class Context implements ComponentContext
	{
		private final List<EventHandlerRegistration<?>> handlerRegistrations = new ArrayList<EventHandlerRegistration<?>>();

		@Override
		public InputTerminal createInput( String name, String label, String description )
		{
			return terminalHolderSupport.createInput( name, label, description );
		}

		@Override
		public InputTerminal createInput( String name, String label )
		{
			return createInput( name, label, null );
		}

		@Override
		public InputTerminal createInput( String name )
		{
			return createInput( name, name, null );
		}

		@Override
		public OutputTerminal createOutput( String name, String label, String description )
		{
			return terminalHolderSupport.createOutput( name, label, description );
		}

		@Override
		public OutputTerminal createOutput( String name, String label )
		{
			return createOutput( name, label, null );
		}

		@Override
		public OutputTerminal createOutput( String name )
		{
			return createOutput( name, name, null );
		}

		@Override
		public void deleteTerminal( Terminal terminal )
		{
			terminalHolderSupport.deleteTerminal( terminal );
		}

		@Override
		public void send( OutputTerminal terminal, TerminalMessage message )
		{
			if( isRunning() && terminal instanceof OutputTerminalImpl && terminalHolderSupport.containsTerminal( terminal ) )
			{
				( ( OutputTerminalImpl )terminal ).sendMessage( message );
			}
			else if( terminal == controllerTerminal )
			{
				if( isController() )
					doHandleTerminalEvent( remoteTerminal, new TerminalMessageEvent( controllerTerminal, message ) );
				else
					send( remoteTerminal, message );
			}
			else if( canvas instanceof SceneItem )
			{
				if( terminal == remoteTerminal )
				{
					if( isController() && propagate )
					{
						doHandleTerminalEvent( remoteTerminal, new TerminalMessageEvent( controllerTerminal, message ) );
					}
					else
					{
						Object[] data = new Object[] { getId(), message.serialize() };
						( ( SceneItem )canvas ).broadcastMessage( ComponentContext.COMPONENT_CONTEXT_CHANNEL, data );
					}
				}
				else if( terminal instanceof AgentTerminal )
				{
					Object[] data = new Object[] { getId(), message.serialize() };
					( ( AgentTerminal )terminal ).agent.sendMessage( ComponentContext.COMPONENT_CONTEXT_CHANNEL, data );
				}
			}
		}

		@Override
		public void setSignature( OutputTerminal terminal, Map<String, Class<?>> signature )
		{
			if( terminal instanceof OutputTerminalImpl && terminalHolderSupport.containsTerminal( terminal ) )
				( ( OutputTerminalImpl )terminal ).setMessageSignature( signature );
		}

		@Override
		public void setLikeFunction( InputTerminal terminal, LikeFunction likeFunction )
		{
			if( terminal instanceof InputTerminalImpl && terminalHolderSupport.containsTerminal( terminal ) )
				( ( InputTerminalImpl )terminal ).setLikeFunction( likeFunction );
		}

		@Override
		public Collection<Terminal> getTerminals()
		{
			return ComponentItemImpl.this.getTerminals();
		}

		@Override
		public Terminal getTerminalByName( String name )
		{
			return ComponentItemImpl.this.getTerminalByName( name );
		}

		@Override
		public <T> Property<T> createProperty( String propertyName, Class<T> propertyType )
		{
			return ComponentItemImpl.this.createProperty( propertyName, propertyType );
		}

		@Override
		public <T> Property<T> createProperty( String propertyName, Class<T> propertyType, Object initialValue )
		{
			return ComponentItemImpl.this.createProperty( propertyName, propertyType, initialValue );
		}

		@Override
		public <T> Property<T> createProperty( String propertyName, Class<T> propertyType, Object initialValue,
				boolean propagates )
		{
			return ComponentItemImpl.this.createProperty( propertyName, propertyType, initialValue, propagates );
		}

		@Override
		public Collection<Property<?>> getProperties()
		{
			return ComponentItemImpl.this.getProperties();
		}

		@Override
		public Property<?> getProperty( String propertyName )
		{
			return ComponentItemImpl.this.getProperty( propertyName );
		}

		@Override
		public void renameProperty( String oldName, String newName )
		{
			ComponentItemImpl.this.renameProperty( oldName, newName );
		}

		@Override
		public void deleteProperty( String propertyName )
		{
			ComponentItemImpl.this.deleteProperty( propertyName );
		}

		@Override
		public TerminalMessage newMessage()
		{
			return new TerminalMessageImpl( conversionService );
		}

		@Override
		public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<? super T> listener )
		{
			ComponentItemImpl.this.addEventListener( type, listener );
			handlerRegistrations.add( new EventHandlerRegistration<T>( listener, type ) );
		}

		@Override
		public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<? super T> listener )
		{
			ComponentItemImpl.this.removeEventListener( type, listener );
			handlerRegistrations.remove( new EventHandlerRegistration<T>( listener, type ) );
		}

		@Override
		public void setCategory( String category )
		{
			ComponentItemImpl.this.setCategory( category );
		}

		@Override
		public String getCategory()
		{
			return ComponentItemImpl.this.getCategory();
		}

		@Override
		public String getId()
		{
			return ComponentItemImpl.this.getId();
		}

		@Override
		public String getLabel()
		{
			return ComponentItemImpl.this.getLabel();
		}

		@Override
		public void setLabel( String label )
		{
			ComponentItemImpl.this.setLabel( label );
		}

		@Override
		public void setLayout( LayoutComponent layout )
		{
			ReleasableUtils.releaseAll( ComponentItemImpl.this.layout );
			ComponentItemImpl.this.layout = layout;
			refreshLayout();
		}

		@Override
		public void refreshLayout()
		{
			fireBaseEvent( LAYOUT_RELOADED );
		}

		@Override
		public void setCompactLayout( LayoutComponent layout )
		{
			ReleasableUtils.releaseAll( ComponentItemImpl.this.compactLayout );
			ComponentItemImpl.this.compactLayout = layout;
		}

		@Override
		public void setNonBlocking( boolean nonBlocking )
		{
			ComponentItemImpl.this.nonBlocking = nonBlocking;
		}

		@Override
		public void setHelpUrl( String helpUrl )
		{
			ComponentItemImpl.this.helpUrl = helpUrl;
		}

		@Override
		public void addSettingsTab( SettingsLayoutContainer tab )
		{
			ComponentItemImpl.this.settingsTabs.add( tab );
		}

		@Override
		public void clearSettingsTabs()
		{
			ReleasableUtils.releaseAll( ComponentItemImpl.this.settingsTabs );
			ComponentItemImpl.this.settingsTabs.clear();
		}

		@Override
		public void triggerAction( String actionName, Scope scope )
		{
			switch( scope )
			{
			case COMPONENT :
				ComponentItemImpl.this.triggerAction( actionName );
				break;
			case CANVAS :
				getCanvas().triggerAction( actionName );
				break;
			case PROJECT :
				getCanvas().getProject().triggerAction( actionName );
				break;
			case WORKSPACE :
				getCanvas().getProject().getWorkspace().triggerAction( actionName );
				break;
			}
		}

		@Override
		public Counter getCounter( String counterName )
		{
			return ComponentItemImpl.this.getCounter( counterName );
		}

		@Override
		public Collection<String> getCounterNames()
		{
			return ComponentItemImpl.this.getCounterNames();
		}

		@Override
		public boolean isRunning()
		{
			return getCanvas().isRunning();
		}

		@Override
		public boolean isInvalid()
		{
			return ComponentItemImpl.this.isInvalid();
		}

		@Override
		public void setInvalid( boolean state )
		{
			ComponentItemImpl.this.setInvalid( state );
		}

		@Override
		public boolean isBusy()
		{
			return ComponentItemImpl.this.isBusy();
		}

		@Override
		public void setBusy( boolean state )
		{
			ComponentItemImpl.this.setBusy( state );
		}

		@Override
		public CanvasItem getCanvas()
		{
			return ComponentItemImpl.this.getCanvas();
		}

		@Override
		public void handleTerminalEvent( InputTerminal input, TerminalEvent event )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void fireEvent( EventObject event )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public OutputTerminal getControllerTerminal()
		{
			return controllerTerminal;
		}

		@Override
		public DualTerminal getRemoteTerminal()
		{
			return remoteTerminal;
		}

		@Override
		public Collection<DualTerminal> getAgentTerminals()
		{
			List<DualTerminal> terminals = new ArrayList<DualTerminal>();
			if( isController() && getCanvas() instanceof SceneItem )
				for( AgentItem agent : getCanvas().getProject().getAgentsAssignedTo( ( SceneItem )getCanvas() ) )
					terminals.add( getAgentTerminal( agent ) );

			return terminals;
		}

		@Override
		public boolean isController()
		{
			return LoadUI.CONTROLLER.equals( System.getProperty( LoadUI.INSTANCE ) );
		}

		@Override
		public String getAttribute( String key, String defaultValue )
		{
			return ComponentItemImpl.this.getAttribute( key, defaultValue );
		}

		@Override
		public void setAttribute( String key, String value )
		{
			ComponentItemImpl.this.setAttribute( key, value );
		}

		@Override
		public void clearEventListeners()
		{
			for( EventHandlerRegistration<?> registration : handlerRegistrations )
				registration.remove();
			handlerRegistrations.clear();
		}

		@Override
		public ComponentItem getComponent()
		{
			return ComponentItemImpl.this;
		}

		@Override
		public void setActivityStrategy( ActivityStrategy strategy )
		{
			if( isReleased() )
				return;

			if( activityStrategy != null )
				activityStrategy.removeEventListener( ActivityEvent.class, activityListener );

			boolean active = isActive();

			activityStrategy = strategy;
			if( activityStrategy != null )
				activityStrategy.addEventListener( ActivityEvent.class, activityListener );

			if( active != isActive() )
				fireBaseEvent( ACTIVITY );
		}

		@Override
		public StatisticVariable.Mutable addStatisticVariable( String statisticVariableName, String description,
				String... writerTypes )
		{
			StatisticVariable.Mutable variable = statisticHolderSupport.addStatisticVariable( statisticVariableName,
					description );
			for( String writerType : writerTypes )
				statisticHolderSupport.addStatisticsWriter( writerType, variable );

			return variable;
		}

		@Override
		public void removeStatisticVariable( String statisticVariableName )
		{
			statisticHolderSupport.removeStatisticVariable( statisticVariableName );
		}
	}

	private abstract class DummyTerminal implements DualTerminal
	{
		@Override
		public Connection connectTo( InputTerminal input )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Map<String, Class<?>> getMessageSignature()
		{
			return Collections.emptyMap();
		}

		@Override
		public Collection<Connection> getConnections()
		{
			return Collections.emptyList();
		}

		@Override
		public TerminalHolder getTerminalHolder()
		{
			return ComponentItemImpl.this;
		}

		@Override
		public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<? super T> listener )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void fireEvent( EventObject event )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<? super T> listener )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void clearEventListeners()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public String getId()
		{
			return ComponentItemImpl.this.getId() + "/" + getName();
		}

		@Override
		public String getLabel()
		{
			return getName();
		}

		@Override
		public String getDescription()
		{
			return "A special Terminal which can be used to send messages to remote instances of the ComponentItem itself.";
		}

		@Override
		public void setDescription( String description )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void setLabel( String label )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean likes( OutputTerminal outputTerminal )
		{
			return false;
		}
	}

	private class RemoteTerminal extends DummyTerminal
	{
		@Override
		public String getName()
		{
			return ComponentContext.REMOTE_TERMINAL;
		}
	}

	private class AgentTerminal extends DummyTerminal
	{
		private final AgentItem agent;

		public AgentTerminal( AgentItem agent )
		{
			this.agent = agent;
		}

		@Override
		public String getName()
		{
			return agent.getLabel();
		}

		@Override
		public String getId()
		{
			return ComponentItemImpl.this.getId() + "/" + agent.getId();
		}
	}

	private class ControllerTerminal extends DummyTerminal
	{
		@Override
		public String getName()
		{
			return ComponentContext.CONTROLLER_TERMINAL;
		}
	}

	private class EventHandlerRegistration<T extends EventObject>
	{
		private final EventHandler<?> handler;
		private final Class<T> type;

		public EventHandlerRegistration( EventHandler<?> handler, Class<T> type )
		{
			this.handler = handler;
			this.type = type;
		}

		@SuppressWarnings( "unchecked" )
		public void remove()
		{
			removeEventListener( type, ( EventHandler<T> )handler );
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ( ( handler == null ) ? 0 : handler.hashCode() );
			return prime * result + ( ( type == null ) ? 0 : type.hashCode() );
		}

		@Override
		public boolean equals( Object obj )
		{
			if( this == obj )
				return true;
			if( obj == null )
				return false;
			if( getClass() != obj.getClass() )
				return false;
			EventHandlerRegistration<?> other = ( EventHandlerRegistration<?> )obj;

			if( handler == null )
			{
				if( other.handler != null )
					return false;
			}
			else if( !handler.equals( other.handler ) )
				return false;
			if( type == null )
			{
				if( other.type != null )
					return false;
			}
			else if( !type.equals( other.type ) )
				return false;
			return true;
		}
	}
}
