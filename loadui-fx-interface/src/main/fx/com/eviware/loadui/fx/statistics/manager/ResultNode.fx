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
package com.eviware.loadui.fx.statistics.manager;

import javafx.scene.Node;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import javafx.geometry.Insets;
import com.javafx.preview.control.MenuButton;
import com.javafx.preview.control.MenuItem;

import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.Deletable;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.resources.DialogPanel;
import com.eviware.loadui.fx.statistics.StatisticsWindow;

import com.eviware.loadui.api.statistics.store.Execution;

public class ResultNode extends ResultNodeBase, Draggable, Deletable {
	override var opacity = bind if( dragging ) 0.8 else 1;
	
	override function doDelete():Void {
		execution.delete();
	}
	
	postinit {
		insert MenuItem {
			text: ##[DELETE]"Delete"
			action: function() {
				deleteObject();
			}
		} into menuButton.items
	}
}