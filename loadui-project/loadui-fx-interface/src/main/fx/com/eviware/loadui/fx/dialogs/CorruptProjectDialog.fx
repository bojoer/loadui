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
*CorruptProjectDialog.fx
*
*Created on jun 8, 2010, 16:48:57 em
*/

package com.eviware.loadui.fx.dialogs;

import javafx.scene.control.Label;
import com.eviware.loadui.fx.ui.dialogs.Dialog;

import com.eviware.loadui.api.model.ProjectRef;


/**
 * Dialog allowing the user to decide whether or not to delete a corrupted project
 *
 * @author dain.nilsson
 */
public class CorruptProjectDialog {
	/**
	 * The Reference to the corrupt project
	 */
	public-init var project:ProjectRef;
	
	postinit {
		
		def dialog:Dialog = Dialog {
			title: "{project.getLabel()} is corrupted"
			content: [
				Label { text: "The file is corrupted. Would you like to remove  '{project.getLabel()}' from the Workspace?" },
			]
			okText: "Remove"
			onOk: function() {
				project.delete(false);
				dialog.close();
			}
		}
	}
}
