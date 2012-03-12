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
*ReadOnlyWidget.fx
*
*Created on apr 19, 2010, 17:08:14 em
*/

package com.eviware.loadui.fx.ui.layout.widgets;

import com.eviware.loadui.fx.ui.layout.Widget;

import com.eviware.loadui.util.layout.FormattedString;

public class ReadOnlyWidget extends FormattedStringLabel, Widget {	
	override var plc on replace {
		value = plc.getProperty().getValue();
		text = plc.getLabel();
		
		def pattern = if( plc.has("pattern") ) plc.get("pattern") as String else "%s";
		
		formattedString = new FormattedString( pattern, plc.getProperty().getValue() );
	}
	
	override var value on replace {
		formattedString.setArgs( value );
		formattedString.update();
	}
}
