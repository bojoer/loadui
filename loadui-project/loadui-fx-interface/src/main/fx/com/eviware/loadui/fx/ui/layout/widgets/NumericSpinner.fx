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

package com.eviware.loadui.fx.ui.layout.widgets;

import com.eviware.loadui.fx.ui.layout.widgets.support.SpinnerBase;

public class NumericSpinner extends SpinnerBase {
	public var minimum:Integer;
	public var maximum:Integer;
	
	override var value = 0;
	
	override function clean( newValue:Object ):Object {
		if( newValue instanceof Integer ) {
			def intVal = newValue as Integer;
			if( FX.isInitialized( minimum ) and intVal < minimum ) {
				return minimum;
			} else if( FX.isInitialized( maximum ) and intVal > maximum ) {
				return maximum;
			} else {
				return intVal;
			}
		} else {
			return value;
		}
	}
	
	override function valueFromText( string:String ):Object {
		try {
			return Integer.parseInt( string );
		} catch( e ) {
		}
		return value;
	}
	
	override function textFromValue( value:Object ):String {
		"{value}"
	}
	
	override function nextValue():Object {
		if( value instanceof Integer ) {
			return (value as Integer) + 1;
		}
		return value;
	}
	
	override function prevValue():Object {
		if( value instanceof Integer ) {
			return (value as Integer) - 1;
		}
		return value;
	}
}