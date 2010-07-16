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
*ProjectNode.fx
*
*Created on feb 10, 2010, 11:47:11 fm
*/

package com.eviware.loadui.fx.widgets;

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.Font;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.resources.TitlebarPanel;

import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;

import java.util.EventObject;
import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;

import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.control.Label;
import javafx.scene.text.Font;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.ProjectNode" );

/**
 * Node to display in the AgentList representing a AgentItem.
 */
public class AgentNode extends BaseNode, Draggable, ModelItemHolder, EventHandler {
	/**
	 * The AgentItem to represent.
	 */
	public-init var agent: AgentItem;
	
	override var modelItem = bind lazy agent;
	
	var enabled: Boolean = agent.isEnabled();
	var ready: Boolean = agent.isReady();
	
	var label:String;
	
	postinit {
		if( not FX.isInitialized( agent ) )
			throw new RuntimeException( "agent must not be null!" );
		
		agent.addEventListener( BaseEvent.class, this );
		label = agent.getLabel();
	}
	
	override function create() {
		TitlebarPanel {
			content: [
				Rectangle { fill: Color.web("#c9c9c9"), width: 115, height: 163 },
			]
			titlebarContent: [
				ImageView {
					layoutX: 15
					layoutY: 14
					image: Image {
		            	url: "{__ROOT__}images/png/led-active.png"
		        	}
		        	visible: bind enabled and ready 
				}
				ImageView {
					layoutX: 15
					layoutY: 14
					image: Image {
		            	url: "{__ROOT__}images/png/led-inactive.png"
		        	}
		        	visible: bind enabled and not ready
				}
				ImageView {
					layoutX: 15
					layoutY: 14
					image: Image {
		            	url: "{__ROOT__}images/png/led-disabled.png"
		        	}
		        	visible: bind not enabled
				}
				Label {
					text: bind label.toUpperCase()
					layoutX: 27
					layoutY: 3
					//width: bind width - 30
					height: bind 30
					textFill: Color.web("#606060")
					font: Font.font("Arial", 9)
				}
			]
			opacity: bind if( dragging ) 0.8 else 1
		}
	}
	
	override function handleEvent( e:EventObject ) {
		def event = e as BaseEvent;
		if( event.getKey().equals( ModelItem.LABEL ) ) {
			runInFxThread( function():Void { label = agent.getLabel() } );
		}
		else if(event.getKey().equals(AgentItem.ENABLED)) {
			runInFxThread( function():Void { 
				ready = agent.isReady();
				enabled = agent.isEnabled();
			});
		}
		else if(event.getKey().equals(AgentItem.READY)) {
			runInFxThread( function():Void { 
				ready = agent.isReady();
				enabled = agent.isEnabled();
			});
		}
	}
	
	override function toString() { label }
}
