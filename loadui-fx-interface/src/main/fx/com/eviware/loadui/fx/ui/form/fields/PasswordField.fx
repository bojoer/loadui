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
import javafx.util.Math;

import com.eviware.loadui.fx.ui.form.FormField;

/**
 * Constructs a new TimeFile using the supplied arguments. Input is Long time in seconds.
 */
public function build( id:String, label:String, value:Object ) {
	PasswordField { id:id, label:label, value: value }
}

/**
 * A password field FormField.
 *
 * @author predrag
 */
public class PasswordField extends TextBox, FormField {

	override var value on replace {
		if( value != null and not ( value instanceof String ) )
			throw new IllegalArgumentException( "Value must be of type String!" );
		
		if(not password.equals(value as String)){
			def currDot = dot;
			def currMark = mark;
			password = value as String;
			selectAll();
			super.replaceSelection(getStars(password.length()));
			selectRange(currDot, currMark);
		}
	}
	
    var password = "";

    override function replaceSelection(arg) {
        var pos1 = Math.min(dot, mark);
        var pos2 = Math.max(dot, mark);
        password = "{password.substring(0, pos1)}{arg}{password.substring(pos2)}";
        super.replaceSelection(getStars(arg.length()));
    }

    override function deleteNextChar() {
        if ((mark == dot) and (dot < password.length())) {
            password = "{password.substring(0, dot)}{password.substring(dot + 1)}";
        }
        super.deleteNextChar();
    }

    override function deletePreviousChar() {
        if ((mark == dot) and (dot > 0)) {
            password = "{password.substring(0, dot - 1)}{password.substring(dot)}";
        }
        super.deletePreviousChar();
    }

	override var focused on replace oldVal {
		if(oldVal and not focused){
			value = password;
		}
	}
	
	override function cut(): Void {}

	override function copy(): Void {}
	
	override function paste(): Void {}
	
    function getStars(len: Integer): String {
        var result: String = "";
        for (i in [1..len]) {
            result = "{result}*";
        }
        result;
    }
    
 	
}
