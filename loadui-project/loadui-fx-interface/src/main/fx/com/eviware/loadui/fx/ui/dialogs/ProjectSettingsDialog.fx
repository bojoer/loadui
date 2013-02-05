/* 
 * Copyright 2011 SmartBear Software
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
public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.ui.dialogs.ProjectSettingsDialog" );

import com.eviware.loadui.fx.ui.tabs.*;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.form.fields.*;
import com.eviware.loadui.api.model.ProjectItem;

import org.jfxtras.scene.layout.XMigLayout;
import org.jfxtras.scene.layout.XMigLayout.*;
import net.miginfocom.layout.*;

import java.io.File;

public def IGNORE_INVALID_CANVAS = "gui.ignore_invalid_canvas";
public def IGNORE_UNASSIGNED_TESTCASES = "gui.ignore_unassigned_testcases";

/**
 * SettingsDialog, basic modal dialog with ok, help and cancel button.
 */
public class ProjectSettingsDialog  {
    var theItem:ProjectItem;
    function ok():Void {
    				theItem.setDescription(descriptionForm.getField('description').value as String);
    				theItem.setSaveReport(form.getField('saveReport').value as Boolean);
    				theItem.setReportFolder((form.getField('savePath').value as File).getAbsolutePath());
    				theItem.setAbortOnFinish(executionForm.getField('abortOnFinish').value as Boolean);
    				theItem.setAttribute( IGNORE_INVALID_CANVAS, "{miscForm.getField(IGNORE_INVALID_CANVAS).value as Boolean}" );
    				theItem.setAttribute( IGNORE_UNASSIGNED_TESTCASES, "{miscForm.getField(IGNORE_UNASSIGNED_TESTCASES).value as Boolean}" );
    				dialogRef.close();
             }
             
    var form:Form;
    var descriptionForm:Form;
    var miscForm:Form;
    var executionForm:Form;
    var dialogRef: Dialog;
	
	public function show(item:ProjectItem) {
	
		var cb:CheckBoxField;
		theItem = item;
		
		dialogRef = TabDialog {
         modal: true
         title: item.getLabel()
         subtitle: "Settings"
         showPostInit: true
         closable: true
         helpUrl: "http://www.loadui.org/interface/project-view.html"
			tabs: [
      		Tab {
      			label: "Description",
      			content: descriptionForm = Form {
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
							    value: if (not (item.getReportFolder() == null)) new File(item.getReportFolder()) else null
							    disable: bind not (cb.value as Boolean)
							    selectMode: FileInputField.DIRECTORIES_ONLY
							}
						]
					}
				},
				Tab {
					label: "Misc",
					content: miscForm = Form {
						singleColumn: true
						formContent: [
							CheckBoxField {
								id: IGNORE_INVALID_CANVAS
								value: item.getAttribute( IGNORE_INVALID_CANVAS, "false" ) != "false"
								label: "Do not warn when starting a Canvas without both a Generator and a Runner."
							}, CheckBoxField {
								id: IGNORE_UNASSIGNED_TESTCASES
								value: item.getAttribute( IGNORE_INVALID_CANVAS, "false" ) != "false"
								label: "Do not warn when creating a TestCase in Distributed Mode."
							}
						]
					}
				}
			]
         onOk: ok
		}
	}
}
