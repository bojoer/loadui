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
package com.eviware.loadui.launcher.util;

import static com.sun.javafx.PlatformUtil.isLinux;
import static com.sun.javafx.PlatformUtil.isWindows;
import static java.lang.Runtime.getRuntime;

import java.io.File;
import java.io.IOException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.TextBuilder;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import com.sun.javafx.PlatformUtil;

public class ErrorHandler
{

	private static class ShowPrompt implements Runnable
	{
		final String message;

		ShowPrompt( String message )
		{
			this.message = message;
		}

		@Override
		public void run()
		{
			doShowPrompt( message );
		}

	}

	public static void promptRestart()
	{
		promptRestart( "A problem occured during startup and LoadUI needs to restart." );
	}

	public static void promptRestart( final String message )
	{
		System.err.println( message );

		Runnable showPrompt = new ShowPrompt( message );
		if( Platform.isFxApplicationThread() )
		{
			showPrompt.run();
		}
		else
		{
			Platform.runLater( showPrompt );
		}
	}

	private static void doShowPrompt( final String message )
	{
		Stage dialogStage = StageBuilder.create().resizable( false ).style( StageStyle.UTILITY ).title( "LoadUI" )
				.onCloseRequest( new EventHandler<WindowEvent>()
				{
					@Override
					public void handle( WindowEvent _ )
					{
						quitAction.handle( null );
					}
				} ).build();

		dialogStage.initModality( Modality.WINDOW_MODAL );

		Button restartButton = ButtonBuilder.create().text( "Restart" ).onAction( restartAction ).defaultButton( true )
				.build();
		Button quitButton = ButtonBuilder.create().text( "Quit" ).onAction( quitAction ).build();
		dialogStage.setScene( new Scene( VBoxBuilder
				.create()
				.spacing( 10 )
				.style( "-fx-padding: 10;" )
				.maxWidth( 600 )
				.alignment( Pos.CENTER )
				.children(
						TextBuilder.create().text( message ).wrappingWidth( 550 ).build(),
						HBoxBuilder.create().spacing( 20 ).alignment( Pos.CENTER ).children( restartButton, quitButton )
								.build() ).build() ) );
		dialogStage.show();
	}

	private final static EventHandler<ActionEvent> restartAction = new EventHandler<ActionEvent>()
	{
		@Override
		public void handle( ActionEvent _ )
		{
			String classPath = ".;lib/*;";
			String java = "java";

			if( isWindows() )
			{
				File f = new File( "jre/bin/java.exe" );
				if( f.exists() )
					java = "jre/bin/java.exe";
			}
			else if( isLinux() )
			{
				classPath = classPath.replace( ";", ":" );
				File f = new File( "jre/bin/java" );
				if( f.exists() )
					java = "jre/bin/java";
			}

			try
			{
				getRuntime().exec(
						java + " -Xms128m -Xmx1024m -XX:MaxPermSize=256m -cp " + classPath
								+ " com.javafx.main.Main --nofx=false" );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
			System.exit( 1 );
		}
	};

	private final static EventHandler<ActionEvent> quitAction = new EventHandler<ActionEvent>()
	{
		@Override
		public void handle( ActionEvent _ )
		{
			System.exit( 1 );
		}
	};
}
