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
package com.eviware.loadui.fx.runners.discovery;

import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Panel;
import javafx.scene.layout.LayoutInfo;
import javafx.geometry.VPos;
import javafx.geometry.HPos;

import javafx.scene.paint.Color;
import com.eviware.loadui.fx.ui.tabs.*;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.form.fields.*;
import com.eviware.loadui.fx.dummy.*;
import com.eviware.loadui.api.discovery.RunnerDiscovery.*;
import com.eviware.loadui.api.model.RunnerItem;
import com.eviware.loadui.fx.ui.dialogs.Dialog;

import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.RunnerItem;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.MainWindow;

import org.jfxtras.scene.layout.XMigLayout;
import org.jfxtras.scene.layout.XMigLayout.*;
import net.miginfocom.layout.*;
import javafx.scene.layout.Priority;

import org.slf4j.LoggerFactory;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.runners.discovery.RunnerDiscovererDialog" );

public class RunnerDiscovererDialog  {
	
	var runnersDiscovered: Boolean = false;
	
	public function show() {
		var runners: RunnerReference[] = RunnerDiscoverer.instance.getNewRunners();
		
		runnersDiscovered = runners.size() > 0;
		
		var checkBoxes: CheckBoxField[] = [];
		for(r in runners){
			insert CheckBoxField { 
				id: r.getUrl()
				label: "{r.getDefaultLabel()} ({r.getUrl()})"
				description: r.getUrl()
				value: false
				layoutInfo: LayoutInfo { 
					width: 400  
				} 
				width: 400
				layoutX: 20
			} into checkBoxes; 
		}
		
		def dialogRef: Dialog = Dialog {
			width: if(runnersDiscovered) 500 else 300
			height: if(runnersDiscovered) 400 else 100
			noCancel: not runnersDiscovered
	        modal: true
	        title: "Auto detect agents in network"
	        showPostInit: true
	        stripeVisible: false
	        closable: true
	        helpUrl: if(runnersDiscovered) "http://www.loadui.org/Working-with-loadUI/agents-and-testcases.html" else null
	        content: [
        		Form {
        			layoutInfo: LayoutInfo { 
        				hgrow: Priority.ALWAYS vgrow: Priority.ALWAYS
        				hfill: true vfill: true
					}
					formContent: [
						LabelField {
							value: if(runnersDiscovered) "Following agents were detected:" else "No new agents detected!"
							layoutInfo: LayoutInfo { 
								hgrow: Priority.ALWAYS vgrow: Priority.NEVER
        						hfill: true vfill: false 
							}
							vpos: bind if(runnersDiscovered) VPos.CENTER else VPos.TOP 
						}
						checkBoxes
					]
				}
			]
			onOk: function() {
				if(runnersDiscovered){
		        	for(i in [0..checkBoxes.size()-1]){
		        		if(checkBoxes[i].selected){
		        			MainWindow.instance.workspace.createRunner(runners[i], getValidName(runners[i].getDefaultLabel()));
		        		}
		        	}
	        	}
				dialogRef.close();
			}
		}
	}
	
	function getValidName(name: String): String {
		if(validateName(name)){
			return name;
		}
		var c = 1;
		while(not validateName("{name} {c}")){
			c++;
		}
		"{name} {c}";
	}
	
	function validateName(name: String): Boolean {
		var workspace: WorkspaceItem = MainWindow.instance.workspace;
		for(runner in workspace.getRunners()){
			if(runner.getLabel().equals(name)){
				return false;
			}
		}
		true;
	}
	
}
