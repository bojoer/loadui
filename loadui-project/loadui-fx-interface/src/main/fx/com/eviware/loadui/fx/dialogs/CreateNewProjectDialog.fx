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
*CreateNewProjectDialog.fx
*
*Created on feb 10, 2010, 13:06:43 em
*/

package com.eviware.loadui.fx.dialogs;

import javafx.scene.layout.LayoutInfo;

import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.form.fields.*;
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.AppState;

import com.eviware.loadui.api.model.WorkspaceItem;
import java.io.File;
import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.dialogs.CreateNewProjectDialog" );

/**
 * Asks the user for a name and file for a new ProjectItem to be created.
 *
 * @author dain.nilsson
 */
public class CreateNewProjectDialog {
	/**
	 * The currently loaded WorkspaceItem. This needs to be set during initialization.
	 */
	public-init var workspace: WorkspaceItem;
	public-init var layoutX:Number;
	public-init var layoutY:Number;
	
	def projectDir = new File("{FX.getProperty('javafx.user.home')}{File.separator}.loadui");
	
	var form:Form;
			var name:TextField;
			var file:TextField;
			var open:CheckBoxField;
			
			function ok():Void  {
							if( validateFile( file.value as String ) ) {
								log.debug( "Creating new project: '\{\}'  with path: '\{\}'", name.value, file.value );
								def p = workspace.createProject( new File( projectDir, file.value as String ), name.value as String, true );
								//p.getStatisticPages().createPage( "General" );
								//TODO: Add content to the General statistics page.
								p.save();
								dialog.close();
								if( open.value as Boolean ) {
									AppState.byName("MAIN").setActiveCanvas( p );
								} else {
									for( ref in workspace.getProjectRefs() ) {
										if( ref.isEnabled() and ref.getProject() == p ) {
											ref.setEnabled( false );
											break;
										}
									}
								}
							} else {
								log.error( "Unable to create project with filename: '{file.value}'!" );
							}
						}
						
	var dialog:Dialog;
	
	postinit {
		if( not FX.isInitialized( workspace ) )
			throw new RuntimeException( "Workspace is null!" );
		
		form = Form {
			layoutInfo: LayoutInfo { width: 250 }
			formContent: [
				name = TextField { label: "Project Name", action: ok },
				file = TextField { label: "Filename", action: ok },
				open = CheckBoxField { label: "Open the new Project?", value: false }
			]
		};
		
		dialog = if( FX.isInitialized( layoutX ) and FX.isInitialized( layoutY ) )
			Dialog {
				title: "Create new project"
				x:layoutX
				y:layoutY
				content: form
				okText: "Create"
				onOk: ok
			} 
		else
			Dialog {
				title: "Create new project"
				content: form
				okText: "Create"
				onOk: ok
			};
		
		var c = 0;
		while( true ) {
			c++;
			def filename = "project-{c}.xml";
			if( validateFile( filename ) ) {
				name.value = "Project {c}";
				file.value = filename;
				break;
			}
		}
	}
	
	function validateFile( file:String ):Boolean {
		not new File( projectDir, file ).exists()
	}
}
