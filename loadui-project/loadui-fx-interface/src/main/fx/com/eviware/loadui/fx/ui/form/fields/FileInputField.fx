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

import javafx.scene.layout.HBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.control.TextBox;
import javafx.scene.control.Button;

import javax.swing.JFileChooser;
import java.io.File;
import java.lang.IllegalArgumentException;

import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.util.StringUtils;

/**
 * Constructs a new FileInputField using the supplied arguments.
 */
public function build( id:String, label:String, value:Object ) {
	FileInputField { id:id, label:label, value:value }
}

public def FILES_ONLY = JFileChooser.FILES_ONLY;
public def DIRECTORIES_ONLY = JFileChooser.DIRECTORIES_ONLY;
public def FILES_AND_DIRECTORIES = JFileChooser.FILES_AND_DIRECTORIES;

/**
 * A File FormField.
 *
 * @author robert
 * @author dain.nilsson
 */
public class FileInputField extends HBox, FormField {	
	public-init var selectMode = FILES_ONLY;

	override var value on replace {
		if( value != null and not ( value instanceof File ) )
			throw new IllegalArgumentException( "Value must be of type File!" );
		
		textBox.text = if( value == null) "" else (value as File).getPath();
	}
	
	override var spacing = 4;
	
	def chooser = new JFileChooser(); 
	
	def textBox = TextBox {
		layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS, vfill: true }
		text: if( value == null) "" else (value as File).getPath();
	}
	def textBoxText = bind textBox.text on replace {
		value = if( StringUtils.isNullOrEmpty( textBoxText ) ) null else new File( textBoxText );
	}
	
	def button = Button {
		layoutInfo: LayoutInfo { vfill: true }
		text: "Browse..."
		action: function() {
			chooser.setSelectedFile( value as File );
			if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog( null ) ) {
				value = chooser.getSelectedFile();
			}
		}
	}
	
	init {
		chooser.setFileSelectionMode( selectMode );
		if( selectMode == DIRECTORIES_ONLY ) {
			chooser.setAcceptAllFileFilterUsed( false );
		}
		
		content = [ textBox, button ];
	}
}
