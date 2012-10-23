package com.eviware.loadui.ui.fx.views.eventlog;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.ui.fx.api.Inspector;
import com.eviware.loadui.ui.fx.api.perspective.PerspectiveEvent;
import com.eviware.loadui.util.statistics.ExecutionListenerAdapter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class EventLogInspector implements Inspector
{
	private static final String FILTER = PerspectiveEvent.getPath( PerspectiveEvent.PERSPECTIVE_PROJECT ) + ".*";

	private final EventLogView panel = new EventLogView();
	private final ExecutionManager executionManager;
	private final TestEventManager testEventManager;

	private Property<Execution> execution = new SimpleObjectProperty<>( this, "execution" );

	public EventLogInspector( ExecutionManager executionManager, TestEventManager testEventManager )
	{
		this.executionManager = executionManager;
		this.testEventManager = testEventManager;

		executionManager.addExecutionListener( new CurrentExecutionListener() );
		execution.addListener( new ChangeListener<Execution>()
		{
			@Override
			public void changed( ObservableValue<? extends Execution> arg0, Execution oldExecution, Execution newExecution )
			{
				if( newExecution == null )
				{
					panel.getItems().clear();
				}
				else
				{
					panel.getItems().setAll( Lists.newArrayList( newExecution.getTestEventRange( 0, Long.MAX_VALUE ) ) );
				}
			}
		} );
	}

	@Override
	public String getName()
	{
		return "Event Log";
	}

	@Override
	public Node getPanel()
	{
		return panel;
	}

	@Override
	public void onShow()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onHide()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public String getHelpUrl()
	{
		return null;
	}

	@Override
	public String getPerspectiveRegex()
	{
		return FILTER;
	}

	private final class CurrentExecutionListener extends ExecutionListenerAdapter
	{
		private final TestEventManager.TestEventObserver entryObserver = new EntryObserver();

		@Override
		public void executionStarted( ExecutionManager.State oldState )
		{
			execution.bind( new ReadOnlyObjectWrapper<>( executionManager.getCurrentExecution() ) );
			testEventManager.registerObserver( entryObserver );
		}

		@Override
		public void executionStopped( ExecutionManager.State oldState )
		{
			execution.unbind();
			testEventManager.unregisterObserver( entryObserver );
		}

		private final class EntryObserver implements TestEventManager.TestEventObserver
		{
			@Override
			public void onTestEvent( final TestEvent.Entry eventEntry )
			{
				Platform.runLater( new Runnable()
				{
					@Override
					public void run()
					{
						panel.getItems()
								.add( Iterables.getFirst( execution.getValue().getTestEvents( panel.getItems().size(), false ),
										null ) );
					}
				} );
			}
		}
	}
}
