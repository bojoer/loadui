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
package com.eviware.loadui.fx.widgets;

import com.eviware.loadui.fx.ui.menu.button.MenuBarButton;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.Deletable;
import com.eviware.loadui.fx.ui.dnd.Droppable;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.widgets.ModelItemHolder;
import com.eviware.loadui.fx.dialogs.DeleteProjectDialog;
import com.eviware.loadui.fx.dialogs.DeleteModelItemDialog;
import com.eviware.loadui.fx.dialogs.DeleteDeletablesDialog;
import com.eviware.loadui.fx.ui.popup.TooltipHolder;

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.input.MouseEvent;

import org.slf4j.LoggerFactory;
public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.TrashHole" );

/**
 * @author robert
 */
public class TrashHole extends BaseNode, Droppable{

	public def iconUrl:String = "images/trash_hole.fxz";
	public var showTooltip:Boolean = true;
	
	override function create() {
		
		MenuBarButton {
			//text: ##[TRASH]"Trash"
			tooltip: if( showTooltip ) {"Drag items here to delete";} else {null;}
			graphicUrl:"{__ROOT__}{iconUrl}"
			onMouseClicked: function(e:MouseEvent) {
			}
		}
	}
	
	override var accept = function( d:Draggable ) {
		d instanceof Deletable or d instanceof ProjectNode
	}
	
	override var onDrop = function( d:Draggable ) {
		if( d.revert ) {
			d.revert = false;
			FX.deferAction( function():Void {
				d.revert = true;
			} );
		}
		
		if( d instanceof ProjectNode ) {
			//We need to treat ProjectNode a bit differenty.
			DeleteProjectDialog { projectRef: ( d as ProjectNode ).projectRef }
		} else if( d instanceof ModelItemHolder ) {
			DeleteModelItemDialog { modelItemHolder: d as ModelItemHolder }
		} else {
			(d as Deletable).deleteObject();
		}
	}
}
