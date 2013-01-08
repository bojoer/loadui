package com.eviware.loadui.ui.fx.util;

import java.io.File;
import java.io.IOException;

import com.sun.javafx.PlatformUtil;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ErrorHandler
{
	public static void promptRestart()
	{
		Stage dialogStage = new Stage();
		dialogStage.setResizable( false );
		dialogStage.initStyle( StageStyle.UTILITY );
		dialogStage.initModality( Modality.APPLICATION_MODAL );

		Button restartButton = ButtonBuilder.create().text( "Restart" ).onAction( restartAction ).defaultButton( true )
				.build();
		Button quitButton = ButtonBuilder.create().text( "Quit" ).onAction( quitAction ).build();
		dialogStage
				.setScene( new Scene( VBoxBuilder
						.create()
						.children( new Text( "A problem occured during startup and LoadUI needs to restart." ),
								HBoxBuilder.create().children( restartButton, quitButton ).build() ).alignment( Pos.CENTER )
						.build() ) );
		dialogStage.show();
	}

	private final static EventHandler<ActionEvent> restartAction = new EventHandler<ActionEvent>()
	{
		@Override
		public void handle( ActionEvent _ )
		{
			String classPath = ".;lib/*;";
			String java = "java";

			if( PlatformUtil.isWindows() )
			{
				File f = new File( "jre/bin/java.exe" );
				if( f.exists() )
					java = "jre/bin/java.exe";
			}
			else if( PlatformUtil.isLinux() )
			{
				classPath = classPath.replace( ";", ":" );
				File f = new File( "jre/bin/java" );
				if( f.exists() )
					java = "jre/bin/java";
			}

			try
			{
				Runtime.getRuntime().exec(
						java + " -Xms128m -Xmx1024m -XX:MaxPermSize=256m -cp " + classPath + " com.javafx.main.Main" );
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
