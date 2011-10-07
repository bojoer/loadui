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
*OnOffSwitchWidget.fx
*
*Created on apr 16, 2010, 11:42:23 fm
*/

package com.eviware.loadui.fx.ui.layout.widgets;

import com.eviware.loadui.fx.ui.layout.Widget;

public class OnOffSwitchWidget extends OnOffSwitch, Widget {
	override var value on replace {
		state = if( value instanceof Boolean ) value as Boolean else false;
		plc.getProperty().setValue( value );
	}
	
	override var state on replace {
		value = state;
	}
}
