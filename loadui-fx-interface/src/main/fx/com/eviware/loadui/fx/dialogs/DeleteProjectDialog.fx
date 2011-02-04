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
/*
*DeleteProjectDialog.fx
*
*Created on feb 10, 2010, 15:11:25 em
*/

package com.eviware.loadui.fx.dialogs;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.HPos;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.fields.*;

import com.eviware.loadui.api.model.ProjectRef;

import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.dialogs.DeleteProjectDialog" );

/**
 * Dialog allowing the user to confirm deletion of a ProjectItem from the Workspace.
 * Asks the user if the project file should be deleted from disk as well as removed from the Workspace.
 * 
 * @author dain.nilsson
 */
public class DeleteProjectDialog {
	/**
	 * The ProjectRef to delete.
	 */
	public-init var projectRef:ProjectRef;
	
	postinit {
		if( not FX.isInitialized( projectRef ) )
			throw new RuntimeException( "projectRef must not be null!" );
		
		var form:Form;
		def dialog:Dialog = Dialog {
			title: "Remove project: {projectRef.getLabel()}"
			content: [
				form = Form {
					formContent: [
						LabelField { value: "Are you sure you want to delete '{projectRef.getLabel()}'?" },
						CheckBoxField { id: "deleteFile", label: "Delete project file on disk \n (cannot be undone!)" }
					]
				}
			]
			okText: "Delete"
			onOk: function() {
				projectRef.delete( form.getValue( "deleteFile" ) as Boolean );
				dialog.close();
			}
		}
	}
}
