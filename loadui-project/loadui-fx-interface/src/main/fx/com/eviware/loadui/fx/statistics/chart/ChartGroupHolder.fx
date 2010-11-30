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
package com.eviware.loadui.fx.statistics.chart;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.node.BaseNode;

import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import java.util.EventObject;

/**
 * Base Chart Node, visualizes a ChartGroup.
 *
 * @author dain.nilsson
 */
public class ChartGroupHolder extends BaseNode {
	
	var title:String = "ChartGroupHolder";
	public-read var expandGroups = false;
	public-read var expandAgents = false;
	var expandedNode:Node;
	
	public-init var chartGroup:ChartGroup on replace {
		title = chartGroup.getTitle();
		chartGroup.addEventListener( BaseEvent.class, new ChartGroupListener() );
	}
	
	def buttonBar:HBox = HBox {
		spacing: 5
		content: [
			Button { text: "Expand group", action: toggleGroupExpand },
			Button { text: "Show agents", action: toggleAgentExpand },
			Button { text: "Configure", action: toggleConfiguration }
		]
	}
	
	def configurationHolder = VBox {
	}
	
	def vbox:VBox = VBox {
		layoutX: 150
		layoutY: 150
		padding: Insets { left: 5, top: 5, right: 5, bottom: 5 }
		spacing: 5
		content: [
			Region { width: bind vbox.width, height: bind vbox.height, managed: false, style: "-fx-background-color: gray;" },
			Label { text: bind title },
			HBox {
				spacing: 5
				content: [
					Rectangle { width: 100, height: 100 },
					Rectangle { width: 400, height: 100 }
				]
			},
			buttonBar,
			configurationHolder
		]
	}
	
	public function toggleGroupExpand():Void {
		expandGroups = not expandGroups;
		if( expandGroups ) {
			if( expandAgents ) toggleAgentExpand();
			expandedNode = Rectangle { width: 500, height: 200 }
			insert expandedNode into vbox.content;
		} else {
			delete expandedNode from vbox.content;
		}
	}
	
	public function toggleAgentExpand():Void {
		expandAgents = not expandAgents;
		if( expandAgents ) {
			if( expandGroups ) toggleGroupExpand();
			expandedNode = Rectangle { width: 500, height: 300 }
			insert expandedNode into vbox.content;
		} else {
			delete expandedNode from vbox.content;
		}
	}
	
	function toggleConfiguration():Void {
		if( sizeof configurationHolder.content == 0 ) {
			insert Rectangle { height: 50, width: 500 } into configurationHolder.content;
		} else {
			configurationHolder.content = [];
		}
	}
	
	override function create():Node {
		vbox
	}
}

class ChartGroupListener extends EventHandler {
	override function handleEvent( e:EventObject ):Void {
		def event = e as BaseEvent;
		if( ChartGroup.TITLE == event.getKey() ) {
			FxUtils.runInFxThread( function():Void {
				title = chartGroup.getTitle();
			} );
		} else if( ChartGroup.TYPE == event.getKey() ) {
			FxUtils.runInFxThread( function():Void {
			} );
		}
	}
}