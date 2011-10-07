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

package com.eviware.loadui.fx.ui.layout.widgets;

import com.eviware.loadui.fx.ui.layout.widgets.support.SpinnerBase;

public def ANY_TIME = "*";

public class TimeSpinner extends SpinnerBase {
	public var range:Integer = 60;
	
	public var allowAnyTime = true;
	
	override var value = 0;
	
	override function clean( newValue:Object ):Object {
		if( newValue instanceof Integer ) {
			def intVal = newValue as Integer;
			if( intVal >= range and range > 0 ) {
				return range - 1;
			} else if( intVal < 0 ) {
				return 0;
			} else {
				return intVal;
			}
		} else if( newValue != ANY_TIME or not allowAnyTime ) {
			return value;
		} else {
			return newValue;
		}
	}
	
	override function valueFromText( string:String ):Object {
		if( string == ANY_TIME )
			return ANY_TIME;
		
		try {
			return Integer.parseInt( string );
		} catch( e ) {
			return null;
		}
	}
	
	override function textFromValue( value:Object ):String {
		if( value == ANY_TIME ) ANY_TIME else "{%02d value}";
	}
	
	override function nextValue():Object {
		if( value instanceof Integer ) {
			def newVal = (value as Integer) + 1;
			return if( newVal < range ) newVal else if( allowAnyTime ) ANY_TIME else 0;
		}
		return 0;
	}
	
	override function prevValue():Object {
		if( value instanceof Integer ) {
			def newVal = (value as Integer) - 1;
			return if( newVal >= 0 ) newVal else if( allowAnyTime ) ANY_TIME else range - 1;
		}
		return range - 1;
	}
}