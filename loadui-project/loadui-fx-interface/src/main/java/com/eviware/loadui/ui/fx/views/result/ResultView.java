package com.eviware.loadui.ui.fx.views.result;

import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import static javafx.beans.binding.Bindings.bindContent;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.control.PageList;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.google.common.base.Function;

public class ResultView extends StackPane
{
	enum ExecutionState
	{
		CURRENT( "current" ), RECENT( "result" ), ARCHIVED( "archive" );

		private String idPrefix;

		private ExecutionState( String idPrefix )
		{
			this.idPrefix = idPrefix;
		}
	};

	protected static final Logger log = LoggerFactory.getLogger( ResultView.class );

	@FXML
	private PageList<ExecutionView> resultNodeList;

	@FXML
	private PageList<ExecutionView> currentResultNode;

	@FXML
	private PageList<ExecutionView> archiveNodeList;

	private final Property<Execution> currentExecution;

	private final ObservableList<Execution> recentExList;
	private ObservableList<ExecutionView> recentExViews;

	private final ObservableList<Execution> archivedExList;
	private ObservableList<ExecutionView> archivedExViews;

	public ResultView( Property<Execution> currentExecution, ObservableList<Execution> recentExecutions,
			ObservableList<Execution> archivedExecutions )
	{
		this.currentExecution = currentExecution;
		this.recentExList = recentExecutions;
		this.archivedExList = archivedExecutions;

		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		currentExecution.addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable _ )
			{
				currentResultNode.getItems().setAll(
						new ExecutionView( currentExecution.getValue(), ExecutionState.CURRENT ) );
			}
		} );

		recentExViews = createExecutionViewsFor( recentExList, ExecutionState.RECENT );
		bindContent( resultNodeList.getItems(), recentExViews );

		archivedExViews = createExecutionViewsFor( archivedExList, ExecutionState.ARCHIVED );
		bindContent( archiveNodeList.getItems(), archivedExViews );

		initArchiveNodeList();

	}

	private ObservableList<ExecutionView> createExecutionViewsFor( final ObservableList<Execution> executions,
			final ExecutionState state )
	{
		final ObservableList<ExecutionView> result = fx( transform( executions, new Function<Execution, ExecutionView>()
		{
			@Override
			public ExecutionView apply( Execution e )
			{
				ExecutionView view = new ExecutionView( e, state );
				view.setId( idFor( state, executions.indexOf( e ) ) );
				return view;
			}
		} ) );

		result.addListener( new ListChangeListener<ExecutionView>()
		{
			@Override
			public void onChanged( Change<? extends ExecutionView> c )
			{
				for( ExecutionView e : result )
					e.setId( idFor( state, result.indexOf( e ) ) );
			}

		} );
		return result;
	}

	private static String idFor( final ExecutionState state, int index )
	{
		return state.idPrefix + "-" + index;
	}

	private void initArchiveNodeList()
	{
		archiveNodeList.addEventHandler( DraggableEvent.ANY, new EventHandler<DraggableEvent>()
		{
			@Override
			public void handle( DraggableEvent event )
			{
				if( event.getData() instanceof Execution )
				{
					Execution execution = ( Execution )event.getData();

					if( event.getEventType() == DraggableEvent.DRAGGABLE_ENTERED )
					{
						if( !execution.isArchived() )
							event.accept();
					}
					else if( event.getEventType() == DraggableEvent.DRAGGABLE_DROPPED )
					{
						log.info( "Archiving test run results" );
						execution.archive();
					}
				}
			}
		} );

	}
}
