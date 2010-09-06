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

import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.dialogs.CreateNewWebProjectDialog;
import com.eviware.loadui.fx.dialogs.CreateNewSoapUIProjectDialog;
import com.eviware.loadui.fx.FxUtils;

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.Stack;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.effect.InnerShadow;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import org.slf4j.LoggerFactory;


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
	        x: com.eviware.loadui.fx.MainWindow.instance.getWidth()/3
	        y: com.eviware.loadui.fx.MainWindow.instance.getHeight()/3
	        noCancel: true
	        noOk: true
	        onClose: function():Void {
	            workspace.setAttribute( SHOW_PROJECT_WIZARD, "{alwaysShow.selected}" );
	        }

	        title: "New Project Wizard"
	        content: [
	            Label{
                     font: Font.font("Ariel", FontWeight.BOLD, 11 )
 				   	 text: "Get started with one of the following options          "
 				},
	        	Stack {
 	 				 layoutInfo: LayoutInfo { margin: Insets { left: 10, top: 15, right: 10, bottom: 0 } }
 	 				 padding: Insets { left: 18, top: 20, right: 68, bottom: 0 }
 	 				 content: [ 
			         	 DialogBorder {
			             	layoutInfo: LayoutInfo { vfill: true hfill: true, margin: Insets { left: -30, top: -20, right: -80, bottom: -20 } }
			         	 },
				         VBox {
				         	spacing: 12;
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
				                 	text: "Show this every time an empty Project is opened"
				                 	selected: workspace.getAttribute( SHOW_PROJECT_WIZARD, "true" ) == "true"
				             	}
				        	]
				         }
				     ]
	        	}
	        ]
	    }
	}
	
	function close() {
	    workspace.setAttribute( SHOW_PROJECT_WIZARD, "{alwaysShow.selected}" );
	    dialog.close();
	}
}

class DialogBorder extends Resizable, CustomNode {
	override function create():Node {
		Rectangle {
			width: bind width
			height: bind height
			fill: Color.rgb( 0xe4, 0xe4, 0xe4 )
			arcWidth: 5
			arcHeight: 5
			effect: InnerShadow {
				radius: 5
				color: Color.rgb( 0x99, 0x99, 0x99 )
			}
		}
	}
	
	override function getPrefHeight( width:Number ) { -1 }
	override function getPrefWidth( height:Number ) { -1 }
}
