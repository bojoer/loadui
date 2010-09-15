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
public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.ui.dialogs.AgentConfigurationDialog" );

import javafx.scene.paint.Color;
import javafx.scene.layout.LayoutInfo;
import com.eviware.loadui.fx.ui.tabs.*;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.form.fields.*;

import com.eviware.loadui.fx.MainWindow;
import java.io.File;

import com.eviware.loadui.api.model.AgentItem;

public class AgentConfigurationDialog {

	public var title:String = "Agent Config";
	public var agent:AgentItem;
	var formT1: Form;
			var formT2: Form;
			var formT3: Form;
	var dialogRef:Dialog;
			
	function ok():Void {
					agent.setDescription(formT1.getField('description').value as String);
					agent.setUrl( formT1.getField('url').value as String );
					agent.getProperty(agent.MAX_THREADS_PROPERTY).setValue(formT3.getField('maxThreads').value as Long);
					
					// add here seting a soapUI ext folder, hermes folder and soapUI settings file to properties.
					
					dialogRef.close();
	         }
	
	public function show() {
		
		var maxThreads: Long = agent.getProperty(agent.MAX_THREADS_PROPERTY).getValue() as Long;
		// support for this need to be added in api
		var soapUIExt: String = "ext folder";
		var soapUIHermes:String = "hermes folder";
		var soapUISettings: String = "settings folder";
		
		dialogRef = TabDialog {
         modal: true
         title: title
         showPostInit: true
         closable: true
         helpUrl: "http://www.loadui.org/Working-with-loadUI/agents-and-testcases.html"
         tabs: [
      		Tab {
      			label: "Description",
      			content: formT1 = Form {
         			singleColumn: true
						formContent: [
							TextField {
								id: "url"
								label: "URL"
								value: agent.getUrl()
							}, TextField { 
								id: "description"
								label: "Description"
								description: "This is a description of the description field."
								multiline: true
								value: agent.getDescription() 
								action: ok
								layoutInfo: LayoutInfo { width: 300, height: 150, vfill: true, hfill: true }
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
				//Tab {
         		//	label: "soapUI", content: formT2 = Form {
				//		formContent: [
				//			TextField { id: "soapUIExt", label: "soapUI ext folder", description: "Path to soapUI ext folder", value: soapUIExt, width: bind 200, action: ok } as FormField,
				//			TextField { id: "soapUIHermes", label: "Hermes folder", description: "Path to Hermes folder", value: soapUIHermes, width: bind 200, action: ok } as FormField,
				//			TextField { id: "soapUISettings", label: "soapUI settings file", description: "Path to soapUI settings file", value: soapUISettings, width: bind 200, action: ok } as FormField,
				//		]
				//	}
				//}
			]
         onOk: ok
		}
	}
};
