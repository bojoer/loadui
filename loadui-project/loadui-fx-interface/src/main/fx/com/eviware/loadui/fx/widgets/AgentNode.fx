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
*ProjectNode.fx
*
*Created on feb 10, 2010, 11:47:11 fm
*/

package com.eviware.loadui.fx.widgets;

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.Font;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.resources.DialogPanel;

import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;

import java.util.EventObject;
import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;

import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Container;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Stack;
import javafx.scene.layout.LayoutInfo;
import javafx.geometry.Insets;

import com.javafx.preview.control.MenuItem;
import com.javafx.preview.control.MenuButton;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.AgentNode" );

/**
 * Node to display in the AgentList representing a AgentItem.
 */
public class AgentNode extends AgentNodeBase, Draggable {
	override var opacity = bind if( dragging ) 0.8 else 1;
	
	override function create() {
		def base = super.create() as DialogPanel;
		activityNode.layoutInfo = LayoutInfo { margin: Insets { top: 70 } };
		
		insert Stack {
			content: [
				ImageView {
					image: Image { url: "{__ROOT__}images/png/agent_node_background.png" }
				}, activityNode
			]
		} into (base.body as Container).content;
		
		base;
	}
}
