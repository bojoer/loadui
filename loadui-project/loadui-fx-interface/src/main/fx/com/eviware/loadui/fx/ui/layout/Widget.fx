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
/*
*Widget.fx
*
*Created on mar 22, 2010, 10:45:06 fm
*/

package com.eviware.loadui.fx.ui.layout;

import javafx.scene.Node;
import javafx.scene.layout.Resizable;

import com.eviware.loadui.fx.ui.layout.widgets.*;
import com.eviware.loadui.fx.ui.form.fields.FileInputField;

import com.eviware.loadui.api.layout.PropertyLayoutComponent;

public function buildWidgetFor( plc:PropertyLayoutComponent ):Widget {
	if( plc.isReadOnly() )
		ReadOnlyWidget { plc:plc }
	else if( plc.has("options") )
		SliderSelectWidget { plc: plc }
	else if( java.lang.Number.class.isAssignableFrom( plc.getProperty().getType() ) )
		Knob { plc: plc }
	else if( java.io.File.class.isAssignableFrom( plc.getProperty().getType() ) and plc.has("mode") )
		FormFieldWidget {
			plc: plc
			field: FileInputField {
				label: plc.getLabel()
				value: plc.getProperty().getValue()
				selectMode : if( plc.get("mode") == "files" ) FileInputField.FILES_ONLY else if( plc.get("mode") == "directories" ) FileInputField.DIRECTORIES_ONLY else FileInputField.FILES_AND_DIRECTORIES
			}
		}
	else
		FormFieldWidget { plc: plc }
}

public mixin class Widget extends Resizable {
	public-init protected var plc:PropertyLayoutComponent;
	
	public-read def property = bind lazy plc.getProperty();
	
	public-read def label = bind lazy plc.getLabel();
	
	public var value:Object = plc.getProperty().getValue();
	
	init {
		if( plc.has("class") )
			(this as Node).styleClass = plc.get("class") as String;
		
		if( plc.has("style") )
			(this as Node).style = plc.get("style") as String;
		
		value = plc.getProperty().getValue();
	}
}
