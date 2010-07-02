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
import javafx.scene.paint.Color;

import org.slf4j.LoggerFactory;
public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.ui.dialogs.SimpleModalDialog" );

import com.eviware.loadui.api.ui.tabbedpane.SelectMode;
import com.eviware.loadui.fx.ui.tabs.*;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.form.fields.*;
import com.eviware.loadui.api.model.ProjectItem;

import org.jfxtras.scene.layout.XMigLayout;
import org.jfxtras.scene.layout.XMigLayout.*;
import net.miginfocom.layout.*;

import java.io.File;

/**
 * SettingsDialog, basic modal dialog with ok, help and cancel button.
 */
public class ProjectSettingsDialog  {
	
	public function show(item:ProjectItem) {
	
		var form:Form;
		var descriptionForm:Form;
		var cb:CheckBoxField;
		
		def dialogRef: Dialog = Dialog {
		 width: 500
		 height: 400
         modal: true
         title: item.getLabel()
         showPostInit: true
         stripeVisible: true
         closable: true
         helpUrl: "http://www.loadui.org/interface/project-view.html"
         content: TabPanel {
		         	tabs: [
		         		Tab {
		         			label: "Description",
		         			content: descriptionForm = Form {
		         				singleColumn: true
									formContent: [
										TextField { 
											width: bind 430
											height: bind 200
											id: "description"
											label: "Description"
											description: "This is a description of the description field."
											multiline: true
											value: item.getDescription() }
									]
								}
							},
						Tab {
							label: "Reports",
							content: form = Form {
							singleColumn: true
							formContent: [
								cb = CheckBoxField { 
									id: "saveReport"
									label: "Export summary reports to file system"
									value: item.isSaveReport();
								},
								FileInputField {
								    id: "savePath"
								    label:"Folder for exported reports"
								    value: if (not (item.getReportFolder() == null)) new File(item.getReportFolder()) else null
								    disable: bind not (cb.value as Boolean)
								    directoryOnly: true
								}
							]
							}
						}
						]
					}
         onOk: function() {
				item.setDescription(descriptionForm.getField('description').value as String);
				item.setSaveReport(form.getField('saveReport').value as Boolean);
				item.setReportFolder((form.getField('savePath').value as File).getAbsolutePath());
				dialogRef.close();
         }
		}
	}
}
