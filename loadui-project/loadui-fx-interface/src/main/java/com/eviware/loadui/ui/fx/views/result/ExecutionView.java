package com.eviware.loadui.ui.fx.views.result;

import java.io.Closeable;
import java.io.IOException;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.control.SeparatorMenuItemBuilder;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.DragNode;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.views.result.ResultView.ExecutionState;

public class ExecutionView extends Pane
{

	protected static final Logger log = LoggerFactory.getLogger( ExecutionView.class );

	@FXML
	private MenuButton menuButton;

	private final Task<Void> loadAndOpenExecution;
	private final boolean isDragIcon;
	private final Execution execution;
	private final ExecutionState state;
	private final Closeable toClose;

	public ExecutionView( final Execution execution, ExecutionState state, @Nullable Closeable toClose )
	{
		this( execution, state, false, toClose );
	}

	private ExecutionView( final Execution execution, ExecutionState state, boolean isDragIcon, Closeable toClose )
	{
		this.execution = execution;
		this.isDragIcon = isDragIcon;
		this.state = state;
		this.toClose = toClose;

		if( !isDragIcon )
		{
			loadAndOpenExecution = new Task<Void>()
			{
				{
					updateMessage( "Loading execution: " + execution.getLabel() );
				}

				@Override
				protected Void call() throws Exception
				{
					execution.getTestEventCount();
					return null;
				}
			};
			loadAndOpenExecution.setOnSucceeded( new EventHandler<WorkerStateEvent>()
			{
				@Override
				public void handle( WorkerStateEvent workserStateEvent )
				{
					fireEvent( IntentEvent.create( IntentEvent.INTENT_OPEN, execution ) );
				}
			} );
			log.debug( "Created Execution View " + execution.getLabel() );
		}
		else
		{
			loadAndOpenExecution = null;
		}

		FXMLUtils.load( this );

	}

	@FXML
	private void initialize()
	{
		if( !isDragIcon )
		{
			log.debug( "Initializing Execution " + execution.getLabel() );
			menuButton.textProperty().bind( Properties.forLabel( execution ) );
			initMenu();
			DragNode.install( this, new ExecutionView( execution, state, true, null ) ).setData( execution );
		}
	}

	private void initMenu()
	{
		if( state == ExecutionState.ARCHIVED )
		{
			addToMenuButton( -1, "Rename", new EventHandler<ActionEvent>()
			{
				@Override
				public void handle( ActionEvent arg0 )
				{
					rename();
				}
			} );
			menuButton.getItems().add( 0, SeparatorMenuItemBuilder.create().build() );
		}

		addToMenuButton( -1, "Delete", new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent arg0 )
			{
				deleteExecution();
			}
		} );

		addToMenuButton( 0, "Open", new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent arg0 )
			{
				openExecution( null );
			}
		} );

	}

	// set index to negative number to append menuItem as last item
	private void addToMenuButton( int index, String label, EventHandler<ActionEvent> handler )
	{
		MenuItem item = MenuItemBuilder.create().text( label ).id( "menu-" + label ).build();
		if( index >= 0 )
			menuButton.getItems().add( index, item );
		else
			menuButton.getItems().add( item );
		if( handler != null )
			item.setOnAction( handler );
	}

	private void rename()
	{
		log.debug( "Rename" );
		fireEvent( IntentEvent.create( IntentEvent.INTENT_RENAME, execution ) );
	}

	private void deleteExecution()
	{
		log.debug( "Delete" );
		execution.delete();
	}

	@FXML
	protected void openExecution( MouseEvent event )
	{
		if( event == null || event.getClickCount() == 2 )
		{
			log.debug( "Opening Execution " + execution.getLabel() );
			fireEvent( IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING, loadAndOpenExecution ) );

			// cannot run this now or will have ConcurrentModificationException
			Platform.runLater( new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						if( toClose != null )
							toClose.close();
					}
					catch( IOException e )
					{
						log.warn( "Problem closing ResultsPopup", e );
					}
				}
			} );
		}
	}

	public Execution getExecution()
	{
		return execution;
	}

}
