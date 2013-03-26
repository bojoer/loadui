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
package com.eviware.loadui.ui.fx.control;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.GaussianBlurBuilder;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.ui.fx.api.intent.IntentEvent;

public class Dialog extends Stage
{
	public static final String INVALID_CLASS = "invalid";

	private static final GaussianBlur BLUR = GaussianBlurBuilder.create().radius( 8 ).build();
	private final Node owner;
	private final Pane rootPane;
	private final Window parentWindow;

	@SuppressWarnings( "unused" )
	private static final Logger log = LoggerFactory.getLogger( Dialog.class );

	public Dialog( @Nonnull final Node owner, @Nonnull String title )
	{
		this.owner = owner;
		final Scene ownerScene = owner.getScene();

		rootPane = VBoxBuilder.create().styleClass( "dialog" ).minWidth( 300 ).build();

		Scene scene = new Scene( rootPane );
		Bindings.bindContent( scene.getStylesheets(), ownerScene.getStylesheets() );
		setScene( scene );

		setResizable( false );
		initStyle( StageStyle.UTILITY );
		initModality( Modality.APPLICATION_MODAL );
		setTitle( title );

		parentWindow = ownerScene.getWindow();

		// Set a good estimated position before the dialog is shown to avoid flickering. Might not be needed.
		//		setX( getCenterXOfParentWindow() - getScene().getRoot().prefWidth( -1 ) / 2 );
		//		setY( getCenterYOfParentWindow() - getScene().getRoot().prefHeight( -1 ) / 2 );

		//Forward unhandled IntentEvents to the parent window.
		addEventHandler( IntentEvent.ANY, new EventHandler<IntentEvent<?>>()
		{
			@Override
			public void handle( IntentEvent<?> event )
			{
				owner.fireEvent( event );
			}
		} );

		addEventHandler( WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>()
		{
			@Override
			public void handle( WindowEvent arg0 )
			{
				blurParentWindow();
				setX( getCenterXOfParentWindow() - getWidth() / 2 );
				setY( getCenterYOfParentWindow() - getHeight() / 2 );
			}
		} );
	}

	protected void addStyleClass( String styleClass )
	{
		rootPane.getStyleClass().add( styleClass );
	}

	protected Node lookup( String selector )
	{
		return rootPane.lookup( selector );
	}

	private double getCenterYOfParentWindow()
	{
		return parentWindow.getY() + parentWindow.getHeight() / 2;
	}

	private double getCenterXOfParentWindow()
	{
		return parentWindow.getX() + parentWindow.getWidth() / 2;
	}

	public ObservableList<Node> getItems()
	{
		return rootPane.getChildren();
	}

	private void blurParentWindow()
	{
		final Parent root = owner.getScene().getRoot();
		if( root.getEffect() == null )
		{
			root.setEffect( BLUR );
			addEventHandler( WindowEvent.WINDOW_HIDING, new EventHandler<WindowEvent>()
			{
				@Override
				public void handle( WindowEvent arg0 )
				{
					if( root.getEffect() == BLUR )
					{
						root.setEffect( null );
					}
				}
			} );
		}
	}
}
