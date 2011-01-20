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
package com.eviware.loadui.fx.ui.form.fields;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.geometry.VPos;
import javafx.util.Math;

import com.javafx.preview.control.MenuItem;
import com.javafx.preview.control.MenuButton;

import com.eviware.loadui.fx.ui.form.FormField;

/**
 * Constructs a new SelectField using the supplied arguments.
 */
public function build( id:String, label:String, value:Object ) {
     SelectField { id:id, label:label, value:value }
}

/**
 * A FormField for any type of value, with a fixed selection of choices. 
 * 
 * @author dain.nilsson
 */
public class SelectField extends MenuButton, FormField {

	/**
	 * The available options to choose from.
	 */
	public var options:Object[] on replace {
		resetItems()
	}
	
	/**
	 * Provides a String representation of each option to display.
	 * The default value simply utilizes the toString-method of each object.
	 */
	public var labelProvider = function( o:Object ):String {
		"{o}"
	} on replace {
		resetItems()
	}
	
	/**
	 * Provides a Node representation of each option to display.
	 * The default value returns null.
	 */
	public var graphicProvider = function( o:Object ):Node {
		null
	} on replace {
		resetItems()
	}
	
	override var styleClass = "select-field";
	
	override var text = bind labelProvider( value );
	override var graphic = bind graphicProvider( value );
	
	function resetItems():Void {
		items = for( option in options ) MenuItem {
			text: labelProvider( option )
			graphic: graphicProvider( option )
			action: function() { value = option }
		}
	}
} 
