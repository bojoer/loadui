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
package com.eviware.loadui.ui.fx.control.skin;

import javafx.scene.control.Button;
import javafx.scene.layout.RegionBuilder;

import com.sun.javafx.scene.control.skin.ButtonSkin;

public class StyleableGraphicButtonSkin extends ButtonSkin
{
	public StyleableGraphicButtonSkin( Button button )
	{
		super( button );

		button.setGraphic( RegionBuilder.create().styleClass( "graphic" ).build() );
	}
}
