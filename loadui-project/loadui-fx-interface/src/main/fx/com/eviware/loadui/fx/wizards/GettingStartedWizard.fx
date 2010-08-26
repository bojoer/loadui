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
package com.eviware.loadui.fx.wizards;

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
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.api.model.ProjectRef;

import java.io.*;
import java.util.HashMap;

/**
 * @author robert
 * 
 * GettingStartedWizard...
 */


public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.wizards.GettingStartedWizard" );

public def SHOW_GETTING_STARTED = "gui.getting_started_wizard";

public class GettingStartedWizard {
    
    	public var title:String = "WELCOME TO LOADUI";
    	public var x:Number = 100;
    	public var y:Number = 100;
    	var workspace: WorkspaceItem = MainWindow.instance.workspace;
    	var dialogRef: Dialog;
    	var stackLayoutInfo:LayoutInfo;
    	var cb:CheckBox = CheckBox {
             selected: workspace.getAttribute( SHOW_GETTING_STARTED, "true" ) != "true"
             text: "Don't show again"
         };
    	
    	var tmpX = bind cb.selected on replace {
    		workspace.setAttribute( SHOW_GETTING_STARTED, "{not cb.selected}" );
    	}
    	
    	public function show() {
    
    		dialogRef = Dialog {
    		 x: x
    		 y: y
             title: title
             noOk: true
             noCancel: true
             content: [
                 Label{
                     font: Font.font("Ariel", FontWeight.BOLD, 11 )
 				   	 text: "Get started with one of the following options          "
 				 },
                 Stack {
	 				 layoutInfo: LayoutInfo { margin: Insets { left: 10, top: 15, right: 10, bottom: 10 } }
	 				 padding: Insets { left: 18, top: 20, right: 68, bottom: 8 }
	 				 content: [
		             	 DialogBorder {
		 	 			     layoutInfo: LayoutInfo { vfill: true hfill: true, margin: Insets { left: -30, top: -20, right: -80, bottom: -42 } }
		 	 			 }, 
		 	 			 VBox {
		 	 			    spacing: 12
		 	 			 	content: [    
					             Label{
					                 font: Font.font("Ariel", FontWeight.BOLD, 11 )
					             	 text: "Get Started"
					             },
					             Button {
 					                 text: "View a demo movie"
 					                 action: function () {
 					                     openURL("http://www.loadui.org/loadUI-Demo-Movies.html")
 					                 }
 					             },
					             Button {
					                 text: "Read the Getting Started Tutorial"
					                 action: function () {
  					                     openURL("http://www.loadui.org/Getting-Started-with-loadUI/your-first-load-test.html")
  					                 }
					             },
					             Button {
					                 text: "Open the included sample project"
					                 action: function() {
					                     def samplesDir = new File("samples");
					                     def sampleFile = new File(samplesDir,"getting-started-project.xml");
					                     if( sampleFile.exists() ) {
					                         def projectRef:ProjectRef = workspace.importProject(sampleFile, true);
					                         projectRef.setEnabled(true);
					                         com.eviware.loadui.fx.AppState.instance.setActiveCanvas( projectRef.getProject() );
			                     			
					                     } else {
					                        def dialog:Dialog = Dialog {
 				                     			title: "Warning"
 				                     			content: [
 				                     				Label {
 				                     					text: "Project file is missing"
 				                     				}
 				                     			]
 				                     			okText: "OK"
 				                     			onOk: function() {
 				                     			   dialog.close()
 				                     			}
 				                     		} 
					                     }
					                     dialogRef.close();
					                 }
					             },
					             Label {
 					                 text: "             " 
 					             },
					             Label {
					                 font: Font.font("Ariel", FontWeight.BOLD, 11 )
					                 text: "New Project" 
					             },
					             Button {
					                 text: "Create a new loadUI Project"
					                 action: function() {
					                     dialogRef.close();
					                     com.eviware.loadui.fx.dialogs.CreateNewProjectDialog { 
                     	                      workspace: workspace
                     	                  }
					                 }
					             },
					             Button {
					                 text: "Leave me be, I know what I'm doing!"
					                 action: function() {
					                     dialogRef.close()
					                 }
					             },
					             cb
			             	]
		 	 			 }
	             ]
                 }
             ]
    		}
    		
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

