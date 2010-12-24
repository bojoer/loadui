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
import javafx.scene.layout.LayoutInfo;
import javafx.scene.paint.Color;

import org.slf4j.LoggerFactory;
public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.ui.dialogs.TestCaseSettingsDialog" );

import com.eviware.loadui.fx.ui.tabs.*;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.form.fields.*;
import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.model.SceneItem;

import org.jfxtras.scene.layout.XMigLayout;
import org.jfxtras.scene.layout.XMigLayout.*;
import net.miginfocom.layout.*;

/**
 * TestCaseSettingsDialog
 */
public class TestCaseSettingsDialog  {
	var dialogRef: Dialog;
	var form:Form;
	var executionForm:Form;
	var theItem:SceneItem;

	function ok():Void {
		theItem.setDescription(form.getField('description').value as String);
		theItem.setAbortOnFinish(executionForm.getField('abortOnFinish').value as Boolean);
		dialogRef.close();
	}
	
	public function show(item:SceneItem) {
		theItem = item;
		
		dialogRef = TabDialog {
         title: item.getLabel()
         subtitle: "Settings"
         helpUrl: "http://www.loadui.org/Working-with-loadUI/project-view.html"
      	tabs: [
      		Tab {
      			label: "Description",
      			content: form = Form {
      				singleColumn: true
						formContent: [
							TextField { 
								id: "description"
								label: "Description"
								description: "This is a description of the description field."
								multiline: true
								value: item.getDescription() 
								action: ok
								layoutInfo: LayoutInfo { width: 300, height: 150, vfill: true, hfill: true }
							}
						]
					}
					//onSelect: function() { log.debug( "Selected tab 1" ) }
				},
				Tab {
      			label: "Execution",
      			content: executionForm = Form {
      				singleColumn: true
						formContent: [
							CheckBoxField { 
								id: "abortOnFinish"
								label: "Abort ongoing requests on finish"
								value: item.isAbortOnFinish();
							}
						]
					}
				}
			]
         onOk: ok
		}
	}
}
