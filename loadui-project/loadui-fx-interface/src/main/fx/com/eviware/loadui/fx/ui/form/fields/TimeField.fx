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
*TextField.fx
*
*Created on feb 22, 2010, 12:55:05 em
*/
package com.eviware.loadui.fx.ui.form.fields;

import javafx.scene.control.TextBox;
import javafx.scene.control.Label;
import java.lang.IllegalArgumentException;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.animation.Timeline;
import java.lang.NumberFormatException;

import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.layout.widgets.NumericSpinner;

/**
 * Constructs a new TimeFile using the supplied arguments. Input is Long time in seconds.
 */
public function build( id:String, label:String, value:Object ) {
	TimeField { id:id, label:label, value: value }
}

/**
 * A time field FormField in hhmmss format.
 *
 * @author predrag
 */
public class TimeField extends HBox, FormField {

	var hSpinner: NumericSpinner = NumericSpinner {
		minimum: 0
		layoutInfo: LayoutInfo { hfill: true vfill: false hgrow: Priority.ALWAYS vgrow: Priority.NEVER}
	}
	var mSpinner: NumericSpinner = NumericSpinner {
		minimum: 0
		maximum: 59
		layoutInfo: LayoutInfo { hfill: true vfill: false hgrow: Priority.ALWAYS vgrow: Priority.NEVER}
	}
	var sSpinner: NumericSpinner = NumericSpinner {
		minimum: 0
		maximum: 59
		layoutInfo: LayoutInfo { hfill: true vfill: false hgrow: Priority.ALWAYS vgrow: Priority.NEVER}
	}
	
	var hValue = bind hSpinner.value on replace {
		buildValue();	
	}

	var mValue = bind mSpinner.value on replace {
		buildValue();	
	}

	var sValue = bind sSpinner.value on replace {
		buildValue();	
	}
	
	var initialValue: Long;
	var initialized: Boolean = false;
	
	override var value on replace {
		if( value != null and not ( value instanceof Long ) )
			throw new IllegalArgumentException( "Value must be of type Long!" );

		if(not initialized){
			initialValue = value as Long;
			initialized = true;
		}
		parseValue(value as Long);
	}
	
	override var layoutInfo = LayoutInfo { hfill: true vfill: false hgrow: Priority.ALWAYS vgrow: Priority.NEVER}
	
	init {
    	padding = Insets { top: 0 right: 0 bottom: 0 left: 0}
    	spacing = 2;
    	nodeVPos = VPos.CENTER;
    	content = [
    		hSpinner,
	    	Label { 
				text: ":"
			}
	    	mSpinner,
	    	Label { 
				text: ":"
			}
	    	sSpinner
    	];
    }
    
    postinit{
   		parseValue(initialValue);
    }
    
    function parseValue(total: Long): Void {
        var seconds = total;
        def hours = seconds / 3600;
        seconds -= hours * 3600;
        def minutes = seconds / 60;
        seconds -= minutes * 60;
        
		hSpinner.value = hours as Integer;
		mSpinner.value = minutes as Integer;
		sSpinner.value = seconds as Integer;
    }
    
    function buildValue(): Void {
     	try {
			var h: Long = hSpinner.value as Long; 
			var m: Long = mSpinner.value as Long;
			var s: Long = sSpinner.value as Long;
			value = h * 3600 + m * 60 + s;
		} 
		catch(e: NumberFormatException) {
			value = null;
		}
    }
    
}

