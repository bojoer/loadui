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

import com.eviware.loadui.api.model.CanvasObjectItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.ModelItem;

import javafx.scene.text.*;

import org.slf4j.LoggerFactory;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.dialogs.CloneTestCaseDialog" );

public class CloneTestCaseDialog {

	public-init var canvasObject:CanvasObjectItem;
	
	def testCase = bind canvasObject instanceof SceneItem;
	
	postinit {
		var form:Form;
		var label = canvasObject.getLabel();
		def name = TextField { label: "Name of clone", value: "copy-of-{label}", columns: 30 };
		def open = CheckBoxField { label: "Open the new TestCase?", value: true };
		def distribute = CheckBoxField { label: "Distibute to same agents?", value: true };
		
		def dialog:Dialog = Dialog {
			title: "Clone {canvasObject.getLabel()}"
			content: form = Form {
				formContent: [
					name, distribute as FormField, open
				]
			}
			okText: "Clone"
			onOk: function() {
				if( validateLabel( name.value as String ) ) {
					log.debug( "Cloning: '\{\}''", canvasObject );
					def copy = canvasObject.getCanvas().duplicate( canvasObject );
					copy.setLabel(name.value as String);
					def layoutX = Integer.parseInt( canvasObject.getAttribute( "gui.layoutX", "0" ) ) + 50;
					def layoutY = Integer.parseInt( canvasObject.getAttribute( "gui.layoutY", "0" ) ) + 50;
					copy.setAttribute( "gui.layoutX", "{ layoutX as Integer }" );
					copy.setAttribute( "gui.layoutY", "{ layoutY as Integer }" );
					dialog.close();
					if( distribute.value as Boolean ) {
						def project = (MainWindow.instance.projectCanvas.canvasItem as ProjectItem);
						for( agent in project.getAgentsAssignedTo(canvasObject as SceneItem) )
							project.assignScene(canvasObject as SceneItem, agent);
					}
					if( open.value as Boolean ) {
						AppState.instance.setActiveCanvas( copy.getCanvas() );
					}
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
				    	width: 300
				    	height: 150
				    
				    }
					log.error( "Item already exists with label: '{name.value}'!" );
				}
			}
			width: 400
			height: 150
			
		}
	}
	
	function validateLabel( label:String ):Boolean {
		def canvas = canvasObject.getCanvas();
		for( obj in canvas.getComponents() ) {
			if( obj.getLabel() == label )
				return false;
		}
		
		if( canvas instanceof ProjectItem ) {
			for( tc in (canvas as ProjectItem).getScenes() ) {
				if( tc.getLabel() == label )
					return false;
			}
		}
		
		return true;
	}
	 
}