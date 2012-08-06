package com.eviware.loadui.ui.fx.views.workspace;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxBuilder;
import javafx.scene.control.Label;
import javafx.scene.layout.VBoxBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.ButtonDialog;

public class GettingStartedDialog extends ButtonDialog
{
	@SuppressWarnings( "unused" )
	private static final Logger log = LoggerFactory.getLogger( GettingStartedDialog.class );

	public static final String SHOW_GETTING_STARTED = "gui.getting_started_wizard";

	public GettingStartedDialog( final WorkspaceItem workspace, Node owner )
	{
		super( owner, "Welcome to loadUI" );

		boolean showWizard = workspace.getAttribute( SHOW_GETTING_STARTED, "true" ).equals( "true" );

		final CheckBox showCheckBox = CheckBoxBuilder.create().text( "Don't show this dialog again" )
				.selected( !showWizard ).build();
		showCheckBox.setOnAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				workspace.setAttribute( SHOW_GETTING_STARTED, Boolean.toString( !showCheckBox.isSelected() ) );
			}
		} );

		getItems().setAll(
				new Label( "Get started with one of the following options" ),
				VBoxBuilder
						.create()
						.styleClass( "frame" )
						.children(
								new Label( "Get Started" ),
								ButtonBuilder.create().text( "View a demo video" ).onAction( new EventHandler<ActionEvent>()
								{
									@Override
									public void handle( ActionEvent event )
									{
										try
										{
											Desktop.getDesktop().browse(
													new URI( "http://www.loadui.org/Getting-Started-with-loadUI/videos.html" ) );
										}
										catch( IOException | URISyntaxException e )
										{
											e.printStackTrace();
										}
									}
								} ).build(),
								ButtonBuilder.create().text( "Read the Getting Started tutorial" )
										.onAction( new EventHandler<ActionEvent>()
										{
											@Override
											public void handle( ActionEvent event )
											{
												try
												{
													Desktop
															.getDesktop()
															.browse(
																	new URI(
																			"http://loadui.org/Getting-Started-with-loadUI/your-first-load-test.html" ) );
												}
												catch( IOException | URISyntaxException e )
												{
													e.printStackTrace();
												}
											}
										} ).build(),
								ButtonBuilder.create().text( "Import the included sample projects" )
										.onAction( new EventHandler<ActionEvent>()
										{
											@Override
											public void handle( ActionEvent event )
											{
												close();
												File sampleDirectory = new File( "samples" );
												for( String filename : Arrays.asList( "sample1.xml", "sample2.xml", "sample3.xml" ) )
												{
													fireEvent( IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING,
															new ImportProjectTask( workspace, new File( sampleDirectory, filename ) ) ) );
												}
											}
										} ).build(),
								new Label( "New Project" ),
								ButtonBuilder.create().text( "Create a new loadUI project" )
										.onAction( new EventHandler<ActionEvent>()
										{
											@Override
											public void handle( ActionEvent event )
											{
												close();
												fireEvent( IntentEvent.create( IntentEvent.INTENT_CREATE, ProjectItem.class ) );
											}
										} ).build(), showCheckBox ).build() );
	}
}
