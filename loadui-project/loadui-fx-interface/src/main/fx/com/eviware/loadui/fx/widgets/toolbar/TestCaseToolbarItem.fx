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
*TestCaseToolbarItem.fx
*
*Created on mar 11, 2010, 15:37:53 em
*/

package com.eviware.loadui.fx.widgets.toolbar;

import com.eviware.loadui.fx.ui.toolbar.ToolbarItem;
import com.eviware.loadui.fx.FxUtils.*;

import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.fx.AppState;

import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.MainWindow;
import javafx.scene.text.Text;

def iconImage = Image { url: "{__ROOT__}images/png/testcase-icon.png" };

public class TestCaseToolbarItem extends ToolbarItem {
	override var icon = iconImage;
	
	override var tooltip = "Creates a new TestCase in the Project";
	
	override var label = "TestCase";
	
	override var category = "TestCases";
	
	override def onMouseClicked = function (me:MouseEvent) {
					  if( me.button == MouseButton.PRIMARY and me.clickCount == 2) {
					     var project:ProjectItem = AppState.instance.getActiveCanvas() as ProjectItem;
					     var name = "TestCase";
					     var i=0;
					     while( sizeof project.getScenes()[c|c.getLabel() == name] > 0 )
					     	name = "TestCase ({++i})";
					     			
					     if (not MainWindow.instance.workspace.isLocalMode()) {
					     	def warning:Dialog = Dialog {
					     		title: "Warning!"
					     		content: Text {
					     		    content: "Switch to local mode, or place {name} on an agent in order to run it"
					     		}
					     		okText: "Ok"
					     		onOk: function() {
					     		    warning.close();
					     		}
					     		noCancel: true
					     	}
					     }
					    project.createScene( name );
					  }  
				}
}
