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
*SaveProjectDialog.fx
*
*Created on apr 8, 2010, 11:36:14 fm
*/

package com.eviware.loadui.fx.dialogs;

import javafx.scene.text.Text;
import javafx.scene.control.Button;
import com.eviware.loadui.fx.ui.dialogs.Dialog;

import com.eviware.loadui.api.model.ProjectRef;

import org.jfxtras.scene.layout.XMigLayout.*;
import net.miginfocom.layout.*;
import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;
import com.eviware.loadui.fx.MainWindow;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.dialogs.SaveProjectDialog" );

public class SaveProjectDialog {
	
	public-init var projectRef:ProjectRef;
	
	public-init var onDone: function():Void;
	
	postinit {
		if( not ( FX.isInitialized( projectRef ) and projectRef.isEnabled() ) )
			throw new RuntimeException( "ProjectRef needs to be set and enabled!" );
			
		def project = projectRef.getProject(); 
		
		def dialog:Dialog = Dialog {
			title: "Close Project: {project.getLabel()}"
			content: [
				Text { content: "Save '{project.getLabel()}' before closing?" },
			]
			noOk: true
			cancelText: "Cancel"
			onCancel: function() {
				dialog.close();
			}
			extraButtons: [
				Button {
					translateX: - 19
					translateY: - 38
					text: "Save"
					action: function() {
						MainWindow.instance.projectCanvas.generateMiniatures(); 
						project.save();
						dialog.close();
						onDone();
					}
					layoutInfo: nodeConstraints(new CC().tag( "yes" ).width("60!"))
				}, Button {
					translateX: - 19
					translateY: - 38
					text: "Don't Save"
					action: function() {
						dialog.close();
						onDone();
					}
					layoutInfo: nodeConstraints(new CC().tag( "no" ).width("60!"))
				}
			]
			
			width : 250
			height : 150
		}
	}

};
