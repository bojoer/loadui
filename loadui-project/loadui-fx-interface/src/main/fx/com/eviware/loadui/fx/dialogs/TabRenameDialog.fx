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
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Panel;
import javafx.scene.layout.LayoutInfo;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.scene.control.RadioButton;
import javafx.scene.paint.Color;

import com.eviware.loadui.fx.ui.tabs.*;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.form.fields.*;
import com.eviware.loadui.api.discovery.AgentDiscovery.*;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.statistics.StatisticsWindow;

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

public class TabRenameDialog {
	
	public var tabToRename: RadioButton;
	
	public var onOk: function(renamedTab: RadioButton, newName:String): Void;
	
	public var tabButtons: RadioButton[];
	
	var textField: TextField;
	
	var dialogRef: Dialog;
	
	var title: String = "Rename Tab";
	
	var errorMessage: String = "";
	
	def messageDialog: Dialog = Dialog {
		title: "Error while renaming tab"
		scene: StatisticsWindow.getInstance().scene
		showPostInit: false
		content: LabelField {
			value: bind errorMessage
		}
		okText: "Ok"
		onOk: function() {
			messageDialog.close();
			dialogRef.show();
			textField.requestFocus();
		}
		noCancel: true
	}
			    				    
	public function show() {
		dialogRef = Dialog {
		  scene: StatisticsWindow.getInstance().scene
		  noCancel: false
        modal: true
        title: title
        showPostInit: true
        closable: true
        helpUrl: null //"http://www.loadui.org/Working-with-loadUI/rename-tabs.html"
        content: [
        		Form {
        			layoutInfo: LayoutInfo { width: 300 }
					formContent: [
						textField = TextField { 
							label: "New name:", 
							value: tabToRename.text,
							action: ok
						}
					]
				}
			]
			onOk: ok
		};
		FX.deferAction( function() { textField.requestFocus() } );
		return dialogRef;
	}
	
	function ok(): Void {
		if(validateLength( textField.value as String )){
		    if(validateUniqueness( textField.value as String )){
			    tabToRename.text = textField.value as String; 
			    onOk(tabToRename, textField.value as String);
			    dialogRef.close();
		    }
		    else{
		        dialogRef.close();
		        errorMessage = "Tab named '{textField.value}' already exist. Tab name must be unique!";
		        messageDialog.show();
		    }
		}
		else{
		    dialogRef.close();
		    errorMessage = "Tab name can not be empty!";
		    messageDialog.show();
		}
	}
	
	function validateUniqueness(name: String): Boolean {
	    if(tabButtons != null){
	        for(t in tabButtons){
	            if(t != tabToRename and t.text.equals(name)){
	                return false;
	            }
	        }
	    }
	    true;
	}
	
	function validateLength(name: String): Boolean {
		if(name.length() > 0){
		    true;
		}
		else{
		    false;
		}
	}
	
}
