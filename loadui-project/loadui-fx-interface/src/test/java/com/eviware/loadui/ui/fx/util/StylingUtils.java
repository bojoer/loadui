package com.eviware.loadui.ui.fx.util;

import java.io.File;
import java.io.IOException;

import javafx.scene.Scene;

public class StylingUtils
{
	public static void applyLoaduiStyling( Scene scene ) throws IOException
	{
		final File styleSheet = File.createTempFile( "style", ".css" );
		final String externalForm = new File( "src/main/resources/com/eviware/loadui/ui/fx/loadui-style.css" ).toURI()
				.toURL().toExternalForm();
		scene.getStylesheets().setAll( externalForm, styleSheet.toURI().toURL().toExternalForm() );
	}
}
