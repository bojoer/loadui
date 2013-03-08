package com.eviware.loadui.ui.fx.views.statistics;

import static com.eviware.loadui.ui.fx.util.ObservableLists.filter;
import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static com.google.common.base.Objects.equal;

import java.util.Collection;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TimelineBuilder;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.statistics.ProjectExecutionManager;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.ManualObservable;
import com.eviware.loadui.ui.fx.views.analysis.AnalysisView;
import com.eviware.loadui.ui.fx.views.analysis.FxExecutionsInfo;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.execution.TestExecutionUtils;
import com.eviware.loadui.util.statistics.ExecutionListenerAdapter;
import com.google.common.base.Predicate;

public class StatisticsView extends StackPane
{
	protected static final Logger log = LoggerFactory.getLogger( StatisticsView.class );

	private final ObservableList<Execution> recentExecutions;
	private final ObservableList<Execution> archivedExecutions;
	private final Property<Execution> currentExecution;
	private final ExecutionManager executionManager;
	private final ManualObservable poll = new ManualObservable();
	private final BooleanProperty isExecutionRunning;
	private final CurrentExecutionListener execListener;

	private final TestExecutionTask executionTask = new TestExecutionTask()
	{
		@Override
		public void invoke( TestExecution execution, Phase phase )
		{
			if( phase == Phase.START )
			{
				Platform.runLater( new Runnable()
				{
					@Override
					public void run()
					{
						log.debug( " Phase.START fired, setting execution running to true" );
						isExecutionRunning.set( true );
					}

				} );

			}
			else if( phase == Phase.POST_STOP )
			{
				Platform.runLater( new Runnable()
				{
					@Override
					public void run()
					{
						log.debug( " Phase.POST_STOP fired, setting execution running from " + isExecutionRunning.getValue()
								+ " to false" );
						isExecutionRunning.set( false );
					}

				} );
			}
		}
	};

	public StatisticsView( final ProjectItem project, FxExecutionsInfo executionsInfo )
	{
		currentExecution = new SimpleObjectProperty<>( this, "currentExecution" );
		isExecutionRunning = new SimpleBooleanProperty( TestExecutionUtils.isExecutionRunning() );
		BeanInjector.getBean( TestRunner.class ).registerTask( executionTask, Phase.START, Phase.POST_STOP );

		executionManager = BeanInjector.getBean( ExecutionManager.class );
		final ProjectExecutionManager projectExecutionManager = BeanInjector.getBean( ProjectExecutionManager.class );

		final Collection<Execution> executions = executionManager.getExecutions();

		recentExecutions = fx( filter(
				ofCollection( executionManager, ExecutionManager.RECENT_EXECUTIONS, Execution.class, executions ),
				new Predicate<Execution>()
				{
					@Override
					public boolean apply( Execution input )
					{
						if( equal( projectExecutionManager.getProjectId( input ), project.getId() )
								&& !( input ).isArchived() )
							log.debug( "updated recent execution: " + input.getLabel() );

						return equal( projectExecutionManager.getProjectId( input ), project.getId() ) && !input.isArchived();
					}
				} ) );

		archivedExecutions = fx( filter(
				ofCollection( executionManager, ExecutionManager.ARCHIVE_EXECUTIONS, Execution.class, executions ),
				new Predicate<Execution>()
				{
					@Override
					public boolean apply( Execution input )
					{
						return equal( projectExecutionManager.getProjectId( input ), project.getId() ) && input.isArchived();
					}
				} ) );

		AnalysisView analysisView = new AnalysisView( project, poll, executionsInfo );
		getChildren().setAll( analysisView );

		executionsInfo.setCurrentExecution( currentExecution );
		executionsInfo.setRecentExecutions( recentExecutions );
		executionsInfo.setArchivedExecutions( archivedExecutions );
		executionsInfo.setMenuParent( analysisView.getButtonContainer() );

		addEventHandler( IntentEvent.INTENT_OPEN, new EventHandler<IntentEvent<?>>()
		{
			@Override
			public void handle( IntentEvent<?> event )
			{
				if( event.getArg() instanceof Execution )
				{
					Execution execution = ( Execution )event.getArg();
					log.debug( "Setting current execution to: " + ( execution == null ? "null" : execution.getLabel() ) );
					currentExecution.setValue( execution );
					event.consume();
				}
			}
		} );

		execListener = new CurrentExecutionListener();
		executionManager.addExecutionListener( execListener );

	}

	public Execution getCurrentExecution()
	{
		return currentExecution.getValue();
	}

	public ReadOnlyProperty<Execution> currentExecutionProperty()
	{
		return currentExecution;
	}

	public void close()
	{
		log.debug( "Closing StatisticView. Removing ExecutionListener" );
		executionManager.removeExecutionListener( execListener );
	}

	private final class CurrentExecutionListener extends ExecutionListenerAdapter
	{

		private final Timeline pollTimeline = TimelineBuilder.create().cycleCount( Timeline.INDEFINITE )
				.keyFrames( new KeyFrame( Duration.millis( 500 ), new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent arg0 )
					{
						poll.fireInvalidation();
					}
				} ) ).build();

		@Override
		public void executionStarted( ExecutionManager.State oldState )
		{
			Platform.runLater( new Runnable()
			{
				@Override
				public void run()
				{
					log.debug( "Execution has started" );
					currentExecution.setValue( executionManager.getCurrentExecution() );
					pollTimeline.playFromStart();
				}
			} );
		}

		@Override
		public void executionStopped( ExecutionManager.State oldState )
		{
			Platform.runLater( new Runnable()
			{
				@Override
				public void run()
				{
					log.debug( "Execution has stopped" );
					pollTimeline.stop();
				}
			} );
		}
	}

}