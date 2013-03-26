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

import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.RegionBuilder;

import com.sun.javafx.scene.control.skin.ToggleButtonSkin;

public class StyleableGraphicToggleButtonSkin extends ToggleButtonSkin
{

	public StyleableGraphicToggleButtonSkin( ToggleButton button )
	{
		super( button );
		
		button.setGraphic( HBoxBuilder.create().children(RegionBuilder.create().styleClass( "graphic" ).build(), RegionBuilder.create().styleClass( "secondary-graphic" ).build()).build());
	}
}
