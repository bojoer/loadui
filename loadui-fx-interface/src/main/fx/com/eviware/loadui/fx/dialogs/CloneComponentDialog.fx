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

package com.eviware.loadui.fx.dialogs;


import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.form.fields.*;
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.AppState;

import com.eviware.loadui.api.model.*;
import com.eviware.loadui.api.terminal.*;
import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.MainWindow;

import javafx.scene.text.*;

import org.slf4j.LoggerFactory;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.dialogs.CloneComponentDialog" );

public class CloneComponentDialog {
public-init var canvasObject:CanvasObjectItem;
	
	def component = bind canvasObject instanceof ComponentItem;
	var copy:CanvasObjectItem;
	var level:SelectField;
	var dialog:Dialog;
	var name:TextField;
	def copyIn = CheckBoxField { 
				disable: bind level.value == "PROJECT"
				label: "Clone incomming connections?"
				value: true
			 };
			def copyOut = CheckBoxField { 	
				disable: bind level.value == "PROJECT"
				label: "Clone outgoing connections?"
				value: true
			 };
	function ok():Void {
			    				if( validateLabel( name.value as String ) ) {
			    					log.debug( "Cloning: '\{\}''", canvasObject );
			    					if( AppState.instance.state == AppState.TESTCASE_FRONT and level.value == "PROJECT" ) 
			    						copy = (MainWindow.instance.projectCanvas.canvasItem as ProjectItem).duplicate(canvasObject)
			    					else
			    						copy = canvasObject.getCanvas().duplicate( canvasObject );
			    					copy.setLabel(name.value as String);
			    					def layoutX = Integer.parseInt( canvasObject.getAttribute( "gui.layoutX", "0" ) ) + 50;
			    					def layoutY = Integer.parseInt( canvasObject.getAttribute( "gui.layoutY", "0" ) ) + 50;
			    					copy.setAttribute( "gui.layoutX", "{ layoutX as Integer }" );
			    					copy.setAttribute( "gui.layoutY", "{ layoutY as Integer }" );
			    					if ( copyIn.value as Boolean and not copyIn.disabled) {
			    						for( terminal in canvasObject.getTerminals()[p|p instanceof InputTerminal] ) {
			    							for(connection in terminal.getConnections()) {
			    								var inputName = connection.getInputTerminal().getLabel();
			    								for( input in copy.getTerminals()[k|k.getLabel() == inputName] ) {
			    									copy.getCanvas().connect(connection.getOutputTerminal(), input as InputTerminal);
			    								}
			    							}
			    						}
			    					}
			    					if ( copyOut.value as Boolean and not copyOut.disabled) {
			    						for( terminal in canvasObject.getTerminals()[p|p instanceof OutputTerminal] ) {
			    							for(connection in terminal.getConnections()) {
			    								var outputName = connection.getOutputTerminal().getLabel();
			    								for( output in copy.getTerminals()[k|k.getLabel() == outputName] ) {
			    									copy.getCanvas().connect(output as OutputTerminal, connection.getInputTerminal());
			    								}	
			    							}
			    						}
			    					}
			    					dialog.close();
			    				} else {
			    				    def warning:Dialog = Dialog {
			    				    	title: "Warning!"
			    				    	content: Text {
			    				    		content: "Item already exists with label: '{name.value}'!"
			    				    	}
			    				    	okText: "Ok"
			    				    	onOk: function() {
			    				    		warning.close();
			    				    	}
			    				    	noCancel: true
			    				    }
			    					log.error( "Item already exists with label: '{name.value}'!" );
			    				}
			    		}		
	postinit {
		var form:Form;
		//name = TextField { label: "Name of clone", value: "copy-of-{canvasObject.getLabel()}", columns: 30, action:ok};
		level = SelectField { label: "Level to clone", options: ["TEST CASE", "PROJECT"], value: "TEST CASE"};
		
		
		
		dialog = Dialog {
			title: "Clone {canvasObject.getLabel()}"
			content: form = Form {
				formContent: [
					name = TextField { label: "Name of clone", value: "copy-of-{canvasObject.getLabel()}", action:ok}, if( AppState.instance.state == AppState.TESTCASE_FRONT)
							 level as FormField
						  else [], copyIn as FormField, copyOut as FormField
				]
			}
			okText: "Clone"
			onOk: ok
		}
	}
	
	function validateLabel( label:String ):Boolean {
		def canvas = canvasObject.getCanvas();
		for( obj in canvas.getComponents() ) {
			if( obj.getLabel() == label )
				return false;
		}
		
		if ( level.value == "PROJECT" ) {
			def project = (MainWindow.instance.projectCanvas.canvasItem as ProjectItem);
			for( obj in project.getComponents() ) {
				if( obj.getLabel() == label )
					return false;
			}
		}
		return true;
	}
	 
};
