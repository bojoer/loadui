/* 
 * Copyright 2011 eviware software ab
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
/*
*TextField.fx
*
*Created on feb 22, 2010, 12:55:05 em
*/
package com.eviware.loadui.fx.ui.form.fields;

import javafx.scene.control.TextBox;
import java.lang.IllegalArgumentException;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;

import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.dialogs.Dialog;

/**
 * Constructs a new TextFile using the supplied arguments.
 */
public function build( id:String, label:String, value:Object ) {
	TextField { id:id, label:label, value: value }
}

/**
 * A text field FormField.
 *
 * @author dain.nilsson
 */
public class TextField extends TextBox, FormField {
	override var value on replace {
		if( value != null and not ( value instanceof String ) )
			throw new IllegalArgumentException( "Value must be of type String!" );
			
		text = value as String;
	}
	
	override var text on replace {
		value = text;
	}
}
