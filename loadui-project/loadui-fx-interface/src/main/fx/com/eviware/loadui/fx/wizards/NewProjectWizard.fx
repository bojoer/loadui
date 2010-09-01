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
/*
*NewProjectWizard.fx
*
*Created on feb 10, 2010, 15:11:25 em
*/

package com.eviware.loadui.fx.wizards;

import com.eviware.loadui.fx.StylesheetAware;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import javafx.scene.layout.VBox;
import javafx.geometry.HPos;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.fx.dialogs.CreateNewWebProjectDialog;
import com.eviware.loadui.fx.dialogs.CreateNewSoapUIProjectDialog;
import com.eviware.loadui.fx.FxUtils;

import com.eviware.loadui.api.model.WorkspaceItem;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.dialogs.NewProjectWizard" );

public def SHOW_PROJECT_WIZARD = "gui.new_project_wizard";

/**
 * The Wizard displayed when a new project is created
 * 
 * @author nenad.ristic
 */
public class NewProjectWizard {
	var dialog:Dialog;
	var alwaysShow:CheckBox;
	public-init var workspace:WorkspaceItem;
	
	public function show() {

	    dialog = Dialog {
	        x: com.eviware.loadui.fx.MainWindow.instance.getWidth()/3 + 50
	        y: com.eviware.loadui.fx.MainWindow.instance.getHeight()/3 + 50
	        noCancel: true
	        noOk: true
	        onClose: function():Void {
	            workspace.setAttribute( SHOW_PROJECT_WIZARD, "{alwaysShow.selected}" );
	        }

	        title: "New Project Wizard"
	         content: VBox {
	         	spacing: 10;
	         	nodeHPos: HPos.CENTER
	        	 content:[
	             	Button {
	                 	text:"Create a simple Web LoadTest "
	                 	action: function (): Void {
	                 	   var dial = CreateNewWebProjectDialog {};
	                 	   close();
	                 	}
	             	},
	             	Button {
	             		text:"Create a soapUI LoadTest "
	             		action: function (): Void {
	             			                 	   var dial = CreateNewSoapUIProjectDialog {};
	             			                 	   close();
	             			                 	}
	             	},
	             	Button {
	             		text:"Show me a \"Getting Started\" tutorial video"
	             		action: function():Void {
	             		    FxUtils.openURL("http://loadui.org/loadUI-Demo-Movies.html");
	             		    close();
	             		}
	             	},
	             	
	             	alwaysShow = CheckBox {
	                 	text: "Show this every time a new project is created"
	                 	selected: workspace.getAttribute( SHOW_PROJECT_WIZARD, "true" ) == "true"
	             	}
	        	]
	         
	         }
	    }
	}
	
	function close() {
	    workspace.setAttribute( SHOW_PROJECT_WIZARD, "{alwaysShow.selected}" );
	    dialog.close();
	}
}
