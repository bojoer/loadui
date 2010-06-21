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
package com.eviware.loadui.fx.ui.form.fields;

import javafx.scene.control.Button;

import javax.swing.JFileChooser;
import java.io.File;
import java.lang.IllegalArgumentException;

import com.eviware.loadui.fx.ui.form.FormField;

/**
 * Constructs a new FileInputField using the supplied arguments.
 */
public function build( id:String, label:String, value:Object ) {
	FileInputField { id:id, label:label, value:value }
}

/**
 * A File FormField.
 *
 * @author robert
 * @author dain.nilsson
 */
public class FileInputField extends Button, FormField {	
	override var value on replace {
		if( value != null and not ( value instanceof File ) )
			throw new IllegalArgumentException( "Value must be of type File!" );
	}

	override var text = bind if(value == null) "Choose file" else (value as File).getName();
	
	override var action = function() {
		def chooser = new JFileChooser( value as File );
		chooser.setSelectedFile( value as File );
		if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog( null )) {
			value = chooser.getSelectedFile();
		}
	}
	
	override function getPrefWidth( height:Float ) {
		100
	}
}
