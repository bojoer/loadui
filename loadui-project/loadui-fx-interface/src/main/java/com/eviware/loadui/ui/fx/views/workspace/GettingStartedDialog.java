/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.ui.fx.views.workspace;

import java.io.File;
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

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.ButtonDialog;
import com.eviware.loadui.ui.fx.util.UIUtils;

public class GettingStartedDialog extends ButtonDialog
{
	@SuppressWarnings( "unused" )
	private static final Logger log = LoggerFactory.getLogger( GettingStartedDialog.class );

	public static final String SHOW_GETTING_STARTED = "gui.getting_started_wizard";

	public GettingStartedDialog( final WorkspaceItem workspace, Node owner )
	{
		super( owner, "Welcome to LoadUI" );

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
										UIUtils
												.openInExternalBrowser( "http://www.loadui.org/Getting-Started-with-loadUI/videos.html" );
									}
								} ).build(),
								ButtonBuilder.create().text( "Read the Getting Started tutorial" )
										.onAction( new EventHandler<ActionEvent>()
										{
											@Override
											public void handle( ActionEvent event )
											{
												UIUtils
														.openInExternalBrowser( "http://loadui.org/Getting-Started-with-loadUI/your-first-load-test.html" );
											}
										} ).build(),
								ButtonBuilder.create().text( "Import the included sample projects" )
										.onAction( new EventHandler<ActionEvent>()
										{
											@Override
											public void handle( ActionEvent event )
											{
												close();
												File sampleDirectory = LoadUI.relativeFile( "samples" );
												for( String filename : Arrays.asList( "sample1.xml", "sample2.xml", "sample3.xml" ) )
												{
													fireEvent( IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING,
															new ImportProjectTask( workspace, new File( sampleDirectory, filename ) ) ) );
												}
											}
										} ).build(),
								new Label( "New Project" ),
								ButtonBuilder.create().text( "Create a new LoadUI project" )
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
