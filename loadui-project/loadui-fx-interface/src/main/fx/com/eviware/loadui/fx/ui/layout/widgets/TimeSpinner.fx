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

public def ANY_TIME = "*";

public class TimeSpinner extends SpinnerBase {
	public var range:Integer = 60;
	
	override var value = 0;
	
	override function valueFromText( string:String ):Object {
		if( string == ANY_TIME )
			return ANY_TIME;
		
		try {
			def newVal = Integer.parseInt( string );
			if( newVal > 0 and newVal < range ) {
				return newVal;
			}
		} catch( e ) {
		}
		return 0;
	}
	
	override function textFromValue( value:Object ):String {
		if( value == ANY_TIME ) ANY_TIME else "{%02d value}";
	}
	
	override function nextValue():Object {
		if( value instanceof Integer ) {
			def newVal = (value as Integer) + 1;
			return if( newVal >= range ) ANY_TIME else newVal;
		}
		return 0;
	}
	
	override function prevValue():Object {
		if( value instanceof Integer ) {
			def newVal = (value as Integer) - 1;
			return if( newVal < 0 ) ANY_TIME else newVal;
		}
		return range - 1;
	}
}