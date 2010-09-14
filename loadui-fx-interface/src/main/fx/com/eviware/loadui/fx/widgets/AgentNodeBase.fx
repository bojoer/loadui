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
import javafx.scene.Group;
import javafx.scene.CustomNode;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
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
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.LayoutInfo;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;

import com.javafx.preview.control.MenuItem;
import com.javafx.preview.control.MenuButton;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.AgentNodeBase" );

def ledActive = Image { url: "{__ROOT__}images/png/led-active.png" };
def ledInactive = Image { url: "{__ROOT__}images/png/led-inactive.png" };
def ledDisabled = Image { url: "{__ROOT__}images/png/led-disabled.png" };

/**
 * Node to display in the AgentList representing a AgentItem.
 */
public class AgentNodeBase extends BaseNode, ModelItemHolder, EventHandler {
	public var enabled: Boolean;
	public-read var ready: Boolean;
	public-read var label:String;
	public-read var url:String;
	public-read var utilization:Integer;
	
	/**
	 * The AgentItem to represent.
	 */
	public-init var agent: AgentItem on replace oldAgent {
		modelItem = agent;
		oldAgent.removeEventListener( BaseEvent.class, this );
		agent.addEventListener( BaseEvent.class, this );
		label = agent.getLabel();
		url = agent.getUrl();
		enabled = agent.isEnabled();
		ready = agent.isReady();
		utilization = agent.getUtilization();
	}
	
	protected def activityNode = Group {
		content: [
			Rectangle {
				width: 44
				height: 11
			}, ImageView {
				layoutX: 2
				layoutY: 2
				image: Image { url: "{__ROOT__}images/png/agent-cpu-inactive.png" }
			}, ImageView {
				layoutX: 2
				layoutY: 2
				image: Image { url: "{__ROOT__}images/png/agent-cpu-active.png" }
				viewport: bind Rectangle2D {
					width: 1 + 4*utilization/10 as Integer
					height: 8
				}
				visible: bind utilization > 0
			}
		]
	};
	
	override var modelItem on replace {
		agent = modelItem as AgentItem;
	}
	
	postinit {
		if( not FX.isInitialized( agent ) )
			throw new RuntimeException( "agent must not be null!" );
	}
	
	override var styleClass = "model-item-node";
	
	override function create() {
		var labelTooltip:Tooltip;
		DialogPanel {
			layoutInfo: LayoutInfo { width: 93, height: 146 }
			body: VBox {
				padding: Insets { left: 8, right: 8, top: 8 }
				spacing: 8
				content: [
					Label {
						text: bind label.toUpperCase()
						tooltip: labelTooltip = Tooltip { text: bind "{label} ({url})" }
						graphic: ImageView {
							image: bind if( enabled and ready ) ledActive else if( enabled and not ready ) ledInactive else ledDisabled
						}
						onMouseEntered: function(e) { labelTooltip.activate() }
						onMouseExited: function(e) { labelTooltip.deactivate() }
					}
				]
			}
		}
	}
	
	override function handleEvent( e:EventObject ) {
		def event = e as BaseEvent;
		if( event.getKey().equals( ModelItem.LABEL ) ) {
			runInFxThread( function():Void { label = agent.getLabel() } );
		} else if( event.getKey().equals( AgentItem.URL ) ) {
			runInFxThread( function():Void { url = agent.getUrl() } );
		} else if(event.getKey().equals(AgentItem.ENABLED)) {
			runInFxThread( function():Void {
				ready = agent.isReady();
				enabled = agent.isEnabled();
			});
		} else if(event.getKey().equals(AgentItem.READY)) {
			runInFxThread( function():Void { 
				ready = agent.isReady();
				enabled = agent.isEnabled();
			});
		} else if(event.getKey().equals(AgentItem.UTILIZATION)) {
			runInFxThread( function():Void { 
				utilization = agent.getUtilization();
			});
		}
	}
	
	override function toString() { label }
}
