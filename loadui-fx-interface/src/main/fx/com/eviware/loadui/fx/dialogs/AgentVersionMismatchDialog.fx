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

import javafx.scene.control.Label;
import javafx.scene.layout.LayoutInfo;
import javafx.geometry.Insets;

import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.ui.dialogs.Dialog;

import com.eviware.loadui.api.model.AgentItem;

public class AgentVersionMismatchDialog {
	public-init var agent:AgentItem;
	
	def dialog: Dialog = Dialog {
		title: "Agent version mismatch"
		onOk: function() {
			dialog.close();
		}
		noCancel: true
		content: [
			Label {
				text: "Cannot connect to agent {agent.getLabel()} due to a version mismatch. Please make sure that both the main loadUI application and the loadUI agents are all running the same version of the software."
				textWrap: true
				layoutInfo: LayoutInfo { width: 300, margin: Insets { bottom: 10, top: 20 } }
			}
		]
	}
}