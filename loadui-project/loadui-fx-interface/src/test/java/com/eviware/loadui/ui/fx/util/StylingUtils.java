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
