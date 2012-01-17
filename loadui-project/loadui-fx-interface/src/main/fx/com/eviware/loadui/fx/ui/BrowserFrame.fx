/* 
 * Copyright 2011 SmartBear Software
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
package com.eviware.loadui.fx.ui;

import javafx.scene.layout.Stack;
import javafx.scene.layout.LayoutInfo;
import javafx.geometry.Insets;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.util.BrowserControl;

public class BrowserFrame extends Stack {
	override var styleClass = "browser-frame";
	
	public var url:String;
	
	init {
		content = [
			Region { styleClass: "browser-frame" },
			BrowserControl { url: bind url, layoutInfo: LayoutInfo { margin: Insets { top: 26, right: 26, bottom: 26, left: 26 } } }
		]
	}
}