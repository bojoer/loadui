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
package com.eviware.loadui.fx.ui.dialogs.items;

import javafx.scene.CustomNode;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

import com.eviware.loadui.api.ui.dialogs.items.CheckBox;
import com.eviware.loadui.api.ui.dialogs.items.Updateable;

import org.slf4j.LoggerFactory;
public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.ui.dialogs.CheckBoxItem" );

public class CheckBoxItem extends CustomNode, Updateable {
	
	public var checkBox:CheckBox;
	var cbox:javafx.scene.control.CheckBox;

	public override function create():Node {
		return cbox = javafx.scene.control.CheckBox {
						   			text: checkBox.getLabel()
						   			allowTriState: false
						   			defined: true
						   			selected: checkBox.getValue()
							    }
	}
	
	public function isSelected():Boolean {
		cbox.selected
	}
	
	public override function update(val:Object) {
		checkBox.setSelected(cbox.selected)
	}
}
