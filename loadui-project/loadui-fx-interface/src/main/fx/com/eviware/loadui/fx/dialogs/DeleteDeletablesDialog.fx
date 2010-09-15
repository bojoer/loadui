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
*DeleteSelectionDialog.fx
*
*Created on feb 10, 2010, 15:11:25 em
*/

package com.eviware.loadui.fx.dialogs;

import javafx.scene.control.Label;

import com.eviware.loadui.fx.ui.node.Deletable;
import com.eviware.loadui.fx.ui.dialogs.Dialog;

import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.dialogs.DeleteDeletablesDialog" );

/**
 * Dialog allowing the user to confirm deletion of a number of Deletables.
 *
 * @author dain.nilsson
 */
public class DeleteDeletablesDialog {
	public var onOk: function(): Void;
	
	public-init var deletables: Deletable[];
	
	postinit {
		def dialog:Dialog = Dialog {
			title: "Delete objects"
			content: [
				Label { text: if( sizeof deletables == 1 ) "Delete this item?" else "Delete these { sizeof deletables } items?" }
			]
			okText: "Delete"
			onOk: function() {
				for( deletable in deletables )
					deletable.doDelete();
				dialog.close();
				onOk();
			}
		}
	}
}
