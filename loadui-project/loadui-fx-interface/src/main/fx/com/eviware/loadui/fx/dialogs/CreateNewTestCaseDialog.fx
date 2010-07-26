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
*CreateNewTestCaseDialog.fx
*
*Created on May 26, 2010, 11:00:28 AM
*/

package com.eviware.loadui.fx.dialogs;

import javafx.scene.text.Text;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.HPos;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.fields.*;

import com.eviware.loadui.api.model.*;

import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.dialogs.CreateNewTestCaseDialog" );

public class CreateNewTestCaseDialog {

	public-init var project:ProjectItem;
	
	public var onOk: function( testCase:SceneItem ):Void;
	
	postinit {
		if( not FX.isInitialized( project ) )
			throw new RuntimeException( "project must not be null!" );
		
		var form:Form;
		def dialog:Dialog = Dialog {
			title: "New TestCase for: {project.getLabel()}"
			content: [
				Text { content: "Enter name for new test case:" },
				form = Form {
					formContent: TextField { id: "newTC", columns: 20 }
				}
			]
			okText: "Ok"
			onOk: function() {
				def tc:SceneItem = project.createScene( form.getValue( "newTC" ) as String );
				tc.setAttribute( "gui.layoutX", "200" );
				tc.setAttribute( "gui.layoutY", "200" );
				
				dialog.close();
				onOk( tc );
			}
			
			width : 250
			height : 150
		}
	}
};
