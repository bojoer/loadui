/* 
 * Copyright 2011 eviware software ab
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

import org.slf4j.LoggerFactory;
public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.ui.dialogs.WorkspaceWrenchDialog" );

import javafx.scene.paint.Color;
import com.eviware.loadui.fx.ui.tabs.*;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.form.fields.*;

import org.jfxtras.scene.layout.XMigLayout;
import org.jfxtras.scene.layout.XMigLayout.*;
import net.miginfocom.layout.*;

import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.fx.MainWindow;
import java.io.File;

import com.eviware.loadui.fx.ui.menu.SoapUIButton;

public class WorkspaceWrenchDialog  {
	
	public var title:String = "Workspace";
	var workspace: WorkspaceItem = MainWindow.instance.workspace;
	var formT1: Form;
	var formT2: Form;
	var formT3: Form;
	var dialogRef: Dialog;
	
	function ok():Void {
					workspace.getProperty(WorkspaceItem.SOAPUI_PATH_PROPERTY).setValue(formT2.getField('soapUIPath').value as File);
					workspace.getProperty(WorkspaceItem.SOAPUI_SYNC_PROPERTY).setValue(formT2.getField('soapUISync').value as Boolean);
					workspace.getProperty(WorkspaceItem.SOAPUI_CAJO_PORT_PROPERTY).setValue(formT2.getField('soapUICajoPort').value as Integer);
					workspace.getProperty(WorkspaceItem.LOADUI_CAJO_PORT_PROPERTY).setValue(formT2.getField('loadUICajoPort').value as Integer);
					workspace.getProperty(WorkspaceItem.MAX_THREADS_PROPERTY).setValue(formT3.getField('maxThreads').value as Long);
					workspace.getProperty(WorkspaceItem.MAX_THREAD_QUEUE_PROPERTY).setValue(formT3.getField('maxQueue').value as Long);
					workspace.setDescription(formT1.getField('description').value as String); 
					workspace.getProperty(WorkspaceItem.AUTO_GARBAGE_COLLECTION_INTERVAL).setValue(formT3.getField('gcInterval').value as Long);
	
					if( workspace.getProperty(WorkspaceItem.SOAPUI_PATH_PROPERTY).getValue != null)
						SoapUIButton.instance.image.opacity = 1;
					dialogRef.close();
	         }
	         
	public function show() {

		
		
		var soapUIPath: File = workspace.getProperty(WorkspaceItem.SOAPUI_PATH_PROPERTY).getValue() as File;
		var soapUISync: Boolean = workspace.getProperty(WorkspaceItem.SOAPUI_SYNC_PROPERTY).getValue() as Boolean;
		var soapUICajoPort: Long = workspace.getProperty(WorkspaceItem.SOAPUI_CAJO_PORT_PROPERTY).getValue() as Long;
		var loadUICajoPort: Long = workspace.getProperty(WorkspaceItem.LOADUI_CAJO_PORT_PROPERTY).getValue() as Long;
		var maxThreads: Long = workspace.getProperty(WorkspaceItem.MAX_THREADS_PROPERTY).getValue() as Long;
		var maxQueue: Long = workspace.getProperty(WorkspaceItem.MAX_THREAD_QUEUE_PROPERTY).getValue() as Long;
		var gcInterval: Long = workspace.getProperty(WorkspaceItem.AUTO_GARBAGE_COLLECTION_INTERVAL).getValue() as Long;
		
		dialogRef = TabDialog {
         title: title
         subtitle: "Settings"
         helpUrl: "http://www.loadui.org/interface/workspace-view.html"
         tabs: [
      		Tab {
      			label: "Description",
      			content: formT1 = Form {
      				singleColumn: true
						formContent: [
							TextField {
								id: "description"
								label: "Description"
								description: "This is a description of the description field."
								multiline: true
								value: workspace.getDescription() 
								action: ok
								layoutInfo: LayoutInfo { width: 300, height: 150, vfill: true, hfill: true }
							}							
						]
					}
				},
				Tab {
         			label: "soapUI", content: formT2 = Form {
						formContent: [
							FileInputField { id: "soapUIPath", label: "Path to soapUI", description: "Path to soapUI", value: soapUIPath, filter: FileInputField.FILE_FILTER_XML },
							LongInputField { id: "loadUICajoPort", label: "Integration Port", description: "Integration Port", value: loadUICajoPort },
							LongInputField { id: "soapUICajoPort", label: "soapUI Port", description: "soapUI Port", value: soapUICajoPort },
							CheckBoxField{ id: "soapUISync", label: "Automatically reload updated Projects", description: "Automatically detects and reloads updated soapUI Projects", value: soapUISync }
						]
					}
				},
				Tab {
         			label: "Execution", content: formT3 = Form {
						formContent: [
							LongInputField { id: "maxThreads", label: "Max internal threads", description: "Max internal threads", value: maxThreads },
							LongInputField { id: "maxQueue", label: "Max internal thread queue size", description: "Max internal thread queue size", value: maxQueue },
						   LongInputField { id: "gcInterval", label: "Schedule garbage collection interval", description: "Time interval after which garbage collection will be done.", value: gcInterval }
						]
					}
				}
			]
         onOk: ok
		}
	}
}
