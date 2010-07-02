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

import org.slf4j.LoggerFactory;
public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.ui.dialogs.RunnerConfigurationDialog" );

import com.eviware.loadui.api.ui.tabbedpane.SelectMode;
import javafx.scene.paint.Color;
import com.eviware.loadui.fx.ui.tabs.*;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.form.fields.*;
import com.eviware.loadui.fx.dummy.*;
import com.eviware.loadui.api.ui.dialogs.DialogModel;

import org.jfxtras.scene.layout.XMigLayout;
import org.jfxtras.scene.layout.XMigLayout.*;
import net.miginfocom.layout.*;

import com.eviware.loadui.fx.MainWindow;
import java.io.File;

import com.eviware.loadui.api.model.RunnerItem;

public class RunnerConfigurationDialog {

	public var title:String = "Runner Config";
	public var runner:RunnerItem;
	
	public function show() {
	
		var formT1: Form;
		var formT2: Form;
		var formT3: Form;
		
		var maxThreads: Long = runner.getProperty(runner.MAX_THREADS_PROPERTY).getValue() as Long;
		// support for this need to be added in api
		var soapUIExt: String = "ext folder";
		var soapUIHermes:String = "hermes folder";
		var soapUISettings: String = "settings folder";
		
		def dialogRef: Dialog = Dialog {
		 width: 500
		 height: 400
         modal: true
         title: title
         showPostInit: true
         stripeVisible: true
         closable: true
         helpUrl: "http://www.loadui.org/Working-with-loadUI/agents-and-testcases.html"
         content: TabPanel {
         	tabs: [
         		Tab {
         			label: "Description",
         			content: formT1 = Form {
         			singleColumn: true
						formContent: [
							TextField { 
								width: bind 430
								height: bind 200
								id: "description"
								label: "Description"
								description: "This is a description of the description field."
								multiline: true
								value: runner.getDescription() 
							}							
						]
					}
				}
				Tab {
         			label: "Execution", content: formT3 = Form {
						formContent: [
							LongInputField { id: "maxThreads", label: "Max internal threads", description: "Max internal threads", value: maxThreads, width: bind 200 } as FormField,
						]
					}
				}
				Tab {
         			label: "soapUI", content: formT2 = Form {
						formContent: [
							TextField { id: "soapUIExt", label: "soapUI ext folder", description: "Path to soapUI ext folder", value: soapUIExt, width: bind 200 } as FormField,
							TextField { id: "soapUIHermes", label: "Hermes folder", description: "Path to Hermes folder", value: soapUIHermes, width: bind 200 } as FormField,
							TextField { id: "soapUISettings", label: "soapUI settings file", description: "Path to soapUI settings file", value: soapUISettings, width: bind 200 } as FormField,
						]
					}
				}
				
				
			]
		}
         onOk: function() {
				runner.setDescription(formT1.getField('description').value as String);
				runner.getProperty(runner.MAX_THREADS_PROPERTY).setValue(formT3.getField('maxThreads').value as Long);
				
				// add here seting a soapUI ext folder, hermes folder and soapUI settings file to properties.
				
				dialogRef.close();
         }
		}
	}
	
};
