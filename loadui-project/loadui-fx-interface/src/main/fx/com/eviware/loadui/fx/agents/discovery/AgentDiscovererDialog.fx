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
package com.eviware.loadui.fx.agents.discovery;

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
import com.eviware.loadui.api.discovery.AgentDiscovery.*;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.fx.ui.dialogs.Dialog;

import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.MainWindow;

import org.jfxtras.scene.layout.XMigLayout;
import org.jfxtras.scene.layout.XMigLayout.*;
import net.miginfocom.layout.*;
import javafx.scene.layout.Priority;

import org.slf4j.LoggerFactory;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.agents.discovery.AgentDiscovererDialog" );

public class AgentDiscovererDialog  {
	
	var agentsDiscovered: Boolean = false;
	
	public function show() {
		var agents: AgentReference[] = AgentDiscoverer.instance.getNewAgents();
		
		agentsDiscovered = agents.size() > 0;
		
		var checkBoxes: CheckBoxField[] = [];
		for(r in agents){
			insert CheckBoxField { 
				id: r.getUrl()
				label: "{r.getDefaultLabel()} ({r.getUrl()})"
				description: r.getUrl()
				value: false
			} into checkBoxes; 
		}
		
		def dialogRef: Dialog = Dialog {
			noCancel: not agentsDiscovered
	        modal: true
	        title: "Auto detect agents in network"
	        showPostInit: true
	        closable: true
	        helpUrl: if(agentsDiscovered) "http://www.loadui.org/Working-with-loadUI/agents-and-testcases.html" else null
	        content: [
        		Form {
        			layoutInfo: LayoutInfo { width: 300 }
					formContent: [
						LabelField {
							value: if(agentsDiscovered) "The following agents were detected:" else "No new agents detected!"
						}
						checkBoxes
					]
				}
			]
			onOk: function() {
				if(agentsDiscovered){
		        	for(i in [0..checkBoxes.size()-1]){
		        		if(checkBoxes[i].selected){
		        			MainWindow.instance.workspace.createAgent(agents[i], getValidName(agents[i].getDefaultLabel()));
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
		for(agent in workspace.getAgents()){
			if(agent.getLabel().equals(name)){
				return false;
			}
		}
		true;
	}
	
}
