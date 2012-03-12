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
*NumericWidgetBase.fx
*
*Created on mar 24, 2010, 16:04:36 em
*/

package com.eviware.loadui.fx.ui.layout.widgets.support;

import javafx.util.Math;

import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.popup.TooltipHolder;
import com.eviware.loadui.fx.ui.layout.Widget;

import java.lang.NumberFormatException;

/**
 * Base class for Widgets for Number Properties.
 *
 * @author dain.nilsson
 */
public abstract class NumericWidgetBase extends BaseNode, Widget, TooltipHolder {
	public var stepping:Number = if( plc.has("step") ) plc.get("step") as Number else 1.0;
	public var span:Number = if( plc.has("span") ) plc.get("span") as Number else 100.0 * stepping;
	public var min:Number = if( plc.has("min") ) plc.get("min") as Number else Long.MIN_VALUE; //Not working with Number.MIN_VALUE for some reason
	public var max:Number = if( plc.has("max") ) plc.get("max") as Number else Number.MAX_VALUE;
	public var nullable = if( plc.has("nullable") ) plc.get("nullable") as Boolean else false;
	
	public def floating = ( plc.getProperty().getType() == Double.class or plc.getProperty().getType() == Float.class );
	public def lowerBound = plc.has("min");
	public def upperBound = plc.has("max");
	public def bounded = lowerBound and upperBound;
	
	public def angle = bind 2 * Math.PI * ( 0.375 + (if( lowerBound ) numberValue-min else numberValue) / span );
	
	var decimals = 0;
	var noApply = true;
	
	public var numberValue:Number on replace {
		if( not noApply ) {
			if( numberValue < min ) {
				numberValue = min;
			} else if( numberValue > max ) {
				numberValue = max;
			} else {
				plc.getProperty().setValue( numberValue );
			}
		}
	}
	
	public var textValue = String.format("%%.{decimals}f", value) on replace oldVal {
		if( not noApply ) {
		    if (oldVal != textValue) {
				if( nullable and textValue == "" ) {
					property.setValue( null );
				} else if( textValue != null ) {
					try {
						numberValue = Double.parseDouble( textValue );
						textValue = String.format("%%.{decimals}f", numberValue);
					} catch( e:NumberFormatException ) {
						textValue = oldVal;
					}
				}
		    }
		}
	}
	
	override var value on replace {
		def oldVal = noApply;
		noApply = true;
		numberValue = value as Number;
		textValue = String.format("%%.{decimals}f", numberValue);
		noApply = oldVal;
	}
	
	protected def text = bind String.format("%%.{decimals}f", numberValue);
	
	override var tooltip = bind lazy "{label}: {value}";
	
	init {
		if( bounded )
			span = (max - min) * (4.0/3.0);
		
		if( not plc.has("step") )
			stepping = span / 100;
		
		if( floating )
			while( stepping * Math.pow( 10, decimals ) < 0.9 )
				decimals++;
		
		noApply = false;
	}
}
