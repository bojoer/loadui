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
/*
*CreateNewTestCaseDialog.fx
*
*Created on May 26, 2010, 11:00:28 AM
*/

package com.eviware.loadui.fx.dialogs;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.HPos;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.fields.*;
import javafx.scene.text.Text;

import com.eviware.loadui.api.model.*;

import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.dialogs.CreateNewTestCaseDialog" );

public class CreateNewTestCaseDialog {

	public-init var project:ProjectItem;
	
	var warningMessage: String;
	var allowZeroLengthName: Boolean = true;
	
	public var onOk: function( testCase:SceneItem ):Void;
	var dialog:Dialog;
	var warning: Dialog;
	var form:Form;
	
	function ok():Void {
		dialog.close();
		if(validateZeroLength()){
			if(validateUniqueness()){
		   		def tc: SceneItem = project.createScene( form.getValue( "newTC" ) as String );
				tc.setAttribute( "gui.layoutX", "200" );
				tc.setAttribute( "gui.layoutY", "200" );
				onOk( tc );
			}
			else{
			    warningMessage = "Test case with the specified name already exist in project!";
			    warning.show();
			}
		}
		else{
		    warningMessage = "Zero length name is not allowed!";
		    warning.show();
		}
	}
	
	postinit {
		if( not FX.isInitialized( project ) )
			throw new RuntimeException( "project must not be null!" );
		
		warning = Dialog {
	    	title: "Warning!"
	    	content: Text {
	    		content: bind warningMessage
	    	}
	    	okText: "Ok"
	    	onOk: function() {
	    		warning.close();
	    		dialog.show();
	    	}
	    	noCancel: true
	    	showPostInit: false
	    }
	    
		dialog = Dialog {
			title: "New Scenario for: {project.getLabel()}"
			content: [
				form = Form {
					formContent: TextField { id: "newTC", label: "Scenario Name", action: ok }
				}
			]
			okText: "Ok"
			onOk: ok
		}
	}
	
	function validateZeroLength(): Boolean {
	   	def newName: String = form.getValue("newTC") as String;
	    return allowZeroLengthName or newName.trim().length() > 0;
	}
	
	function validateUniqueness(): Boolean {
	    def newName: String = form.getValue("newTC") as String;
		for( item in project.getChildren() )	{
		    if( item.getLabel().compareToIgnoreCase(newName) == 0 ){
		        return false;
		    }
		} 
		return true;       
	}	
}
