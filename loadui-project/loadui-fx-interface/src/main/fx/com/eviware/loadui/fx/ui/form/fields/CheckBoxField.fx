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
/*
*CheckBoxField.fx
*
*Created on feb 22, 2010, 13:10:32 em
*/
package com.eviware.loadui.fx.ui.form.fields;

import javafx.scene.control.CheckBox;
import java.lang.IllegalArgumentException;

import com.eviware.loadui.fx.ui.form.FormField;

import org.slf4j.LoggerFactory;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.ui.form.fields.CheckBoxField" );

/**
 * Constructs a new CheckBoxFile using the supplied arguments.
 */
public function build( id:String, label:String, value:Object ) {
	CheckBoxField { id:id, label:label, value:value }
}

/**
 * A checkbox FormField.
 *
 * @author dain.nilsson
 */
public class CheckBoxField extends CheckBox, FormField {

	override var value on replace {
		if( value != null and not ( value instanceof Boolean ) )
			throw new IllegalArgumentException( "Value must be of type Boolean!" );
			
		selected = value as Boolean;
	}
	
	override var selected on replace {
		value = selected;
		onSelect();
	}
	
	override var label on replace {
		text = label;
	}

	override var skipLabel = true;
	
	public-init var onSelect:function():Void = function():Void {};
}
