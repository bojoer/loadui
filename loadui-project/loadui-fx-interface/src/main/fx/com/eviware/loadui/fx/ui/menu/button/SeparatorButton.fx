/* 
 * Copyright 2010 eviware software ab
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.fx.ui.menu.button;

import javafx.fxd.FXDNode;

import javafx.scene.paint.Color;
import javafx.scene.CustomNode;
import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.transform.Scale;

import com.eviware.loadui.fx.FxUtils.*;

import org.slf4j.LoggerFactory;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.ui.menu.button.SeparatorButton" );

/**
 * Vertical separator
 */
public class SeparatorButton extends CustomNode {
	
	public var height:Number = 30;
	
	public var iconUrl: String = "images/delimiter.fxz";

	override function create():Node {
		return Group {
					transforms: Scale {
								y: 0.9
							}
					content: FXDNode {
								url: "{__ROOT__}{iconUrl}"
							}
				}
	}
}
