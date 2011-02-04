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
 
package com.eviware.loadui.fx.dialogs;

import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Panel;
import javafx.scene.layout.LayoutInfo;

import org.slf4j.LoggerFactory;
public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.ui.dialogs.StatisticsWrenchDialog" );

import com.eviware.loadui.fx.ui.tabs.*;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.form.fields.*;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.statistics.StatisticsWindow;

import org.jfxtras.scene.layout.XMigLayout;
import org.jfxtras.scene.layout.XMigLayout.*;
import net.miginfocom.layout.*;

import com.eviware.loadui.api.model.WorkspaceItem;

public class StatisticsWrenchDialog  {
	
	public var title:String = "Statistics";
	var formT1: Form;
	var dialogRef: Dialog;
	
	function ok():Void {
					dialogRef.close();
	         }
	         
	public function show() {
		dialogRef = TabDialog {
         title: title
         scene: StatisticsWindow.getInstance().scene
         subtitle: "Settings"
         helpUrl: "http://www.loadui.org/interface/statistics-view.html"
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
								value: ""
								action: ok
								layoutInfo: LayoutInfo { width: 300, height: 150, vfill: true, hfill: true }
							}							
						]
					}
				}
			]
         onOk: ok
		}
	}
}
