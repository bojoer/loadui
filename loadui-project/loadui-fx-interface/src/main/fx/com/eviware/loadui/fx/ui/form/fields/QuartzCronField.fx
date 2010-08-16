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

public function build( id:String, label:String, value:Object ) {
	QuartzCronField { id:id, label:label, value: value }
}

/**
 * A field for entering time for quartz cron trigger.
 *
 * @author predrag
 */
public class QuartzCronField extends HBox, FormField {

	var hTextBox: CustomTextBox = CustomTextBox {
		columns: 2
		selectOnFocus: true
		focusLost: function(): Void {
			parseHours();
			buildValue();
		}
	}
	
	var mTextBox: CustomTextBox = CustomTextBox {
		columns: 2
		selectOnFocus: true
		focusLost: function(): Void {
			parseMinutes();
			buildValue();
		}
	}
	
	var sTextBox: CustomTextBox = CustomTextBox {
		columns: 2
		selectOnFocus: true
		focusLost: function(): Void {
			parseSeconds();
			buildValue();
		}
	}
	
	function parseHours(): Void {
		try {
			var h: Long = Long.valueOf(hTextBox.text);
			if(h>=0 and h<=23){
				hTextBox.text = "{%02d h}";
			} 
			else{
				hTextBox.text = "*";
			}
		} 
		catch(e: NumberFormatException) {
			hTextBox.text = "*";
		}
	}

	function parseMinutes(): Void {
		try {
			var m: Long = Long.valueOf(mTextBox.text);
			if(m>=0 and m<=59){
				mTextBox.text = "{%02d m}";
			} 
			else{
				mTextBox.text = "*";
			}
		} 
		catch(e: NumberFormatException) {
			mTextBox.text = "*";
		}
	}

	function parseSeconds(): Void {
		try {
			var s: Long = Long.valueOf(sTextBox.text);
			if(s>=0 and s<=59){
				sTextBox.text = "{%02d s}";
			} 
			else{
				sTextBox.text = "00";
			}
		} 
		catch(e: NumberFormatException) {
			sTextBox.text = "00";
		}
	}
	
	override var value on replace {
		if( value != null and not ( value instanceof String ) )
			throw new IllegalArgumentException( "Value must be of type String!" );
		
		parseValue(value as String);
	}
	
	override var layoutInfo = LayoutInfo { hfill: true vfill: true hgrow: Priority.ALWAYS vgrow: Priority.ALWAYS}
	
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
    	
    	parseValue(value as String);
    }
    
    function parseValue(total: String): Void {
    	def ssmmhh: String[] = total.split(" ");
    	if(ssmmhh.size() == 3){
    		hTextBox.text = ssmmhh[2];
			mTextBox.text = ssmmhh[1];
			sTextBox.text = ssmmhh[0];
			parseHours();
			parseMinutes();
			parseSeconds();
    	}
    	else{
    		hTextBox.text = "*";
			mTextBox.text = "*";
			sTextBox.text = "00";
    	}
    }
    
    function buildValue(): Void {
		value = "{sTextBox.text} {mTextBox.text} {hTextBox.text}";
    }
    
    override function getPrefHeight( width:Float ) {
		hTextBox.getPrefHeight( width )
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
