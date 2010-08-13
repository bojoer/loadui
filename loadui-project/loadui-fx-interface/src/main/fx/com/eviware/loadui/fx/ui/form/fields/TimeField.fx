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

	var hTextBox: CustomTextBox = CustomTextBox {
		columns: 2
		selectOnFocus: true
		focusLost: function(): Void {
			buildValue();
		}
	}
	var mTextBox: CustomTextBox = CustomTextBox {
		columns: 2
		selectOnFocus: true
		focusLost: function(): Void {
			buildValue();
		}
	}
	var sTextBox: CustomTextBox = CustomTextBox {
		columns: 2
		selectOnFocus: true
		focusLost: function(): Void {
			buildValue();
		}
	}
	
	override var value on replace {
		if( value != null and not ( value instanceof Long ) )
			throw new IllegalArgumentException( "Value must be of type Long!" );
		
		parseValue(value as Long);
	}
	
	override var layoutInfo = LayoutInfo { hfill:true vfill:true hgrow: Priority.ALWAYS vgrow: Priority.ALWAYS}
	
	init {
    	padding = Insets { top: 0 right: 0 bottom: 0 left: 0}
    	spacing = 2;
    	nodeVPos = VPos.CENTER;
    	content = [
    		hTextBox,
	    	Label { 
				text: ":"
			}
	    	mTextBox,
	    	Label { 
				text: ":"
			}
	    	sTextBox
    	];
    	
    	parseValue(value as Long);
    }
    
    function parseValue(total: Long): Void {
        var seconds = total;
        def hours = seconds / 3600;
        seconds -= hours * 3600;
        def minutes = seconds / 60;
        seconds -= minutes * 60;
        
		hTextBox.text = "{%02d hours}";
		mTextBox.text = "{%02d minutes}";
		sTextBox.text = "{%02d seconds}";
    }
    
    function buildValue(): Void {
     	try {
			var h: Long = if(hTextBox.text.length() > 0) Long.valueOf(hTextBox.text) else 0; 
			var m: Long = if(mTextBox.text.length() > 0) Long.valueOf(mTextBox.text) else 0;
			var s: Long = if(sTextBox.text.length() > 0) Long.valueOf(sTextBox.text) else 0;
			value = h * 3600 + m * 60 + s;
		} 
		catch(e: NumberFormatException) {
			value = null;
		}
    }
    
    override function getPrefHeight( width:Float ) {
		hTextBox.getPrefHeight( width ) + 3
	}
	
}

public class CustomTextBox extends TextBox {

	public var focusLost: function();
	
	public var focusGained: function();
	
	override var focused on replace oldVal {
		if(oldVal and not focused){
			focusLost();
		}
		else{
			focusGained();
		}
	}
}
