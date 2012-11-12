package com.eviware.loadui.ui.fx.views.statistics;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.layout.StackPane;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.statistics.ProjectExecutionManager;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.views.analysis.AnalysisView;
import com.eviware.loadui.ui.fx.views.result.ResultView;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;

public class StatisticsView extends StackPane
{
	private final ProjectItem project;
	private final ObservableList<Execution> executionList;

	public StatisticsView( final ProjectItem project )
	{
		this.project = project;

		final ExecutionManager executionManager = BeanInjector.getBean( ExecutionManager.class );
		final ProjectExecutionManager projectExecutionManager = BeanInjector.getBean( ProjectExecutionManager.class );

		executionList = ObservableLists.filter( ObservableLists.ofCollection( executionManager,
				ExecutionManager.EXECUTIONS, Execution.class, executionManager.getExecutions() ),
				new Predicate<Execution>()
				{
					@Override
					public boolean apply( Execution input )
					{
						return Objects.equal( projectExecutionManager.getProjectId( input ), project.getId() );
					}
				} );

		addEventHandler( IntentEvent.ANY, new EventHandler<IntentEvent<?>>()
		{
			@Override
			public void handle( IntentEvent<?> event )
			{
				if( event.getArg() instanceof Execution )
				{
					if( event.getEventType() == IntentEvent.INTENT_OPEN )
					{
						AnalysisView analysisView = new AnalysisView( project, executionList );
						analysisView.setCurrentExecution( ( Execution )event.getArg() );
						getChildren().setAll( analysisView );
						event.consume();
					}
					else if( event.getEventType() == IntentEvent.INTENT_CLOSE )
					{
						getChildren().setAll( new ResultView( executionList ) );
						event.consume();
					}
				}
			}
		} );

		getChildren().setAll( new ResultView( executionList ) );
	}
}
