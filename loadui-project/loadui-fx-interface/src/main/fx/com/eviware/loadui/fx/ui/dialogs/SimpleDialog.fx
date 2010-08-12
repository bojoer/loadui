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
package com.eviware.loadui.fx.ui.dialogs;

import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Panel;

import org.slf4j.LoggerFactory;
public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.ui.dialogs.SimpleModalDialog" );

import javafx.scene.paint.Color;
import com.eviware.loadui.fx.ui.tabs.*;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.form.fields.*;

import org.jfxtras.scene.layout.XMigLayout;
import org.jfxtras.scene.layout.XMigLayout.*;
import net.miginfocom.layout.*;

/**
 * SimpleModalDialog, basic modal dialog with ok, help and cancel button.
 * 
 * @param title - dialog title
 * @param dialogContent - dialog content
 * @param ok - action that will be executed when ok button is pressed
 * @param help - action that will be executed when help button is pressed
 */
public class SimpleDialog  {
	
	public var title:String = "Project 1";
	public var dialogContent:Node = Label {
									    text: "Modal Dialog !"
									};
	
	public function show() {
	
		var form:Form;
		
		def dialogRef: Dialog = TabDialog {
         modal: true
         title: title
         showPostInit: true
         closable: true
         helpUrl: "http://www.loadui.org"
      	tabs: [
      		Tab {
      			label: "A form",
      			content: form = Form {
						formContent: [
							TextField { id: "name", label: "Full name", description: "Your full name, stupid!", value: "John Doe" },
							TextField { id: "description", label: "Describe yourself", description: "This is a description of the description field." },
							CheckBoxField { id: "agree", label: "I agree", description: "Do you agree?", value: true }
						]
					}
					onSelect: function() { log.debug( "Selected tab 1" ) }
				}, Tab {
      			label: "Another form", content: Form {
						formContent: [
							CheckBoxField { id: "agree", label: "I agree", description: "Do you agree?", value: true },
							CheckBoxField { id: "agree2", label: "My previsous statement was true, I swear it was!", description: "Do you agree?", value: true },
							CheckBoxField { id: "agree3", label: "I do not agree", description: "Do you agree?", value: false },
							TextField { id: "first name", label: "First name", description: "Your first name." },
							TextField { id: "last name", label: "Last name", description: "Your last name." },
						]
					}
				}
			]
         onOk: function() {
				log.debug( "Values: {form.getField('name').value}, {form.getField('description').value}, {form.getField('agree').value}" );
				dialogRef.close();
         }
		}
	}
}
