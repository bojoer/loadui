package com.eviware.loadui.ui.fx.views.result;

import java.io.Closeable;
import java.io.IOException;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ContextMenuBuilder;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.MenuItemsProvider;
import com.eviware.loadui.ui.fx.MenuItemsProvider.Options;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.DragNode;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.NodeUtils;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.views.result.ResultView.ExecutionState;

public class ExecutionView extends Pane
{

	protected static final Logger log = LoggerFactory.getLogger( ExecutionView.class );

	@FXML
	private MenuButton menuButton;

	private final boolean isDragIcon;
	private final Execution execution;
	private final ExecutionState state;
	private final Closeable toClose;

	private final Runnable closeWindowRunnable = new Runnable()
	{

		@Override
		public void run()
		{
			closeWindow();
		}
	};

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

		FXMLUtils.load( this );

	}

	@FXML
	private void initialize()
	{
		if( !isDragIcon )
		{
			log.debug( "Initializing Execution " + execution.getLabel() );
			menuButton.textProperty().bind( Properties.forLabel( execution ) );

			DragNode.install( this, new ExecutionView( execution, state, true, null ) ).setData( execution );

			Options menuOptions = Options.are().open( closeWindowRunnable );

			if( state == ExecutionState.RECENT )
				menuOptions.noRename();

			MenuItem[] menuItems = MenuItemsProvider.createWith( this, execution, menuOptions ).items();
			menuButton.getItems().setAll( menuItems );
			final ContextMenu ctxMenu = ContextMenuBuilder.create().items( menuItems ).build();

			setOnContextMenuRequested( new EventHandler<ContextMenuEvent>()
			{
				@Override
				public void handle( ContextMenuEvent event )
				{
					// never show contextMenu when on top of the menuButton
					if( !NodeUtils.isMouseOn( menuButton ) )
					{
						MenuItemsProvider.showContextMenu( menuButton, ctxMenu );
						event.consume();
					}
				}
			} );

		}
	}

	@FXML
	protected void openExecution( MouseEvent event )
	{
		if( event == null || event.getClickCount() == 2 )
		{
			fireEvent( IntentEvent.create( IntentEvent.INTENT_OPEN, execution ) );
			log.debug( "Finished open of execution: " + execution.getLabel() );

			closeWindow();

		}
	}

	private void closeWindow()
	{
		if( toClose != null )
		{
			// cannot run this now or will have ConcurrentModificationException
			Platform.runLater( new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
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

	@Override
	public String toString()
	{
		return execution.getLabel();
	}

	public Execution getExecution()
	{
		return execution;
	}

}
