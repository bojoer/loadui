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
package com.eviware.loadui.fx.ui.layout;

import javafx.scene.control.Label;
import com.eviware.loadui.api.layout.LabelLayoutComponent;

public class LabelLayoutComponentNode extends LayoutComponentNode {
	
	def labelLayoutComponent = layoutComponent as LabelLayoutComponent on replace {
		text = labelLayoutComponent.getLabel()
	}
	
	var text:String;
	
	def label = Label { text: bind text }
	
	override function create() {
		label
	}
	
	override function getPrefHeight( width:Float ) {
		label.getPrefHeight( width )
	}
	
	override function getPrefWidth( height:Float ) {
		label.getPrefWidth( height )
	}
}
