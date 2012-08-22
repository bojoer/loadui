package com.eviware.loadui.ui.fx.views.workspace;

import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.Tab;
import javafx.scene.control.TabBuilder;
import javafx.scene.control.TabPaneBuilder;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBoxBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.ui.fx.control.ConfirmationDialog;

public class GlobalSettingsDialog extends ConfirmationDialog
{
	private static final Logger log = LoggerFactory.getLogger( GlobalSettingsDialog.class );

	private final List<Runnable> saveActions = new ArrayList<>();
	private final WorkspaceItem workspace;

	public GlobalSettingsDialog( Node owner, WorkspaceItem workspace )
	{
		super( owner, "Settings", "OK" );

		this.workspace = workspace;

		setOnConfirm( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				try
				{
					for( Runnable action : saveActions )
					{
						action.run();
					}

					close();
				}
				catch( Exception e )
				{
					log.error( "Error saving settings", e );
				}
			}
		} );

		getItems().setAll( TabPaneBuilder.create().minWidth( 500 ).minHeight( 320 ).tabs( createExecutionTab() ).build() );
	}

	private Tab createExecutionTab()
	{
		final Property<?> maxThreadsProperty = workspace.getProperty( WorkspaceItem.MAX_THREADS_PROPERTY );
		final Property<?> maxThreadQueueProperty = workspace.getProperty( WorkspaceItem.MAX_THREAD_QUEUE_PROPERTY );

		final TextField maxInternalThreads = new TextField( maxThreadsProperty.getStringValue() );
		final TextField maxInternalThreadQueueSize = new TextField( maxThreadQueueProperty.getStringValue() );

		saveActions.add( new Runnable()
		{
			@Override
			public void run()
			{
				maxThreadsProperty.setValue( maxInternalThreads.getText() );
				maxThreadQueueProperty.setValue( maxInternalThreadQueueSize.getText() );
			}
		} );

		return TabBuilder
				.create()
				.text( "Execution" )
				.closable( false )
				.content(
						VBoxBuilder
								.create()
								.children(
										LabelBuilder.create().text( "Max internal threads" ).labelFor( maxInternalThreads )
												.build(),
										maxInternalThreads,
										LabelBuilder.create().text( "Max internal thread queue size" )
												.labelFor( maxInternalThreadQueueSize ).build(), maxInternalThreadQueueSize )
								.build() ).build();
	}
}
