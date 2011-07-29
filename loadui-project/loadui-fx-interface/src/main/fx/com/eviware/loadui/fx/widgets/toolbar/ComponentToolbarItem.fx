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
*ComponentItem.fx
*
*Created on mar 11, 2010, 13:53:43 em
*/

package com.eviware.loadui.fx.widgets.toolbar;

import com.eviware.loadui.fx.ui.toolbar.ToolbarItemNode;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.api.component.ComponentDescriptor;
import javafx.scene.image.Image;

import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;

import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.fx.AppState;

def defaultImage = Image { url: "{__ROOT__}images/png/default-component-icon.png" };

public class ComponentToolbarItem extends ToolbarItemNode {
	public var descriptor:ComponentDescriptor on replace {
		icon = if( descriptor.getIcon() != null )
			Image { url: descriptor.getIcon().toString() }
			else defaultImage;
		
		tooltip = descriptor.getDescription();
		label = descriptor.getLabel();
		category = descriptor.getCategory();
	}
	
	override def onMouseClicked = function (me:MouseEvent) {
				  if( me.button == MouseButton.PRIMARY and me.clickCount == 2) {
				     var canvas:CanvasItem = AppState.byName("MAIN").getActiveCanvas();
				     var name = "{descriptor.getLabel()}";
				     var i=0;
				     while( sizeof canvas.getComponents()[c|c.getLabel() == name] > 0 )
				     		name = "{descriptor.getLabel()} ({++i})";
				     		
				     canvas.createComponent( name, descriptor );
				  }  
			}
}
