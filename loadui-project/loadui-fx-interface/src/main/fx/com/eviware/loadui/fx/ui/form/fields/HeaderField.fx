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
package com.eviware.loadui.fx.ui.form.fields;

/**
* @author henrik.olsson
*
* Label header for Forms.
*/
import javafx.scene.control.Label;
import com.eviware.loadui.fx.ui.form.FormField;

public class HeaderField extends Label, FormField {
	
	override var value on replace {
		text = if(value == null) "null" else value.toString();
	}
	
	override var skipLabel = true;
	
	override var styleClass = "header-field";
}