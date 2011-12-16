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
package com.eviware.loadui.fx.assertions;

import com.eviware.loadui.api.ui.inspector.Inspector;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Stack;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Resizable;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.geometry.HPos;

import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.dnd.Droppable;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.toolbar.Toolbar;
import com.eviware.loadui.fx.FxUtils.*;

import com.eviware.loadui.api.statistics.StatisticsManager;

import com.sun.javafx.scene.layout.Region;

public function createInstance( statisticsManager:StatisticsManager ):AssertionInspector {
	AssertionInspector { statisticsManager: statisticsManager }
}

public class AssertionInspector extends Inspector {
	
	public-init var statisticsManager:StatisticsManager;
	
	var panel:AssertionInspectorNode;
	
	postinit {
		panel = AssertionInspectorNode {}
	}

	override function onShow(): Void {
	}
	
	override function onHide(): Void {
	}
	
	override function getPanel(): Object {
		panel
	}

	override function getName(): String {
		"Assertions"
	}
	
	override function getHelpUrl(): String {
		"http://www.loadui.org/";
	}
}

class AssertionInspectorNode extends Stack {
	override var layoutInfo = LayoutInfo { vfill: true, hfill: true, hgrow: Priority.ALWAYS, vgrow: Priority.ALWAYS, minHeight: 265, maxHeight: 500 }
	override var padding = Insets { right: 5, bottom: 5 }
	
	def toolbar = AssertionToolbar {
		id: "AssertionToolbar",
		statisticsManager: statisticsManager,
		layoutInfo: LayoutInfo { vfill: true, hfill: false, hshrink: Priority.NEVER, margin: Insets { left: -45 }, hpos: HPos.LEFT, vpos: VPos.TOP }
	};
	
	def assertions = AssertionList {
		layoutInfo: LayoutInfo { vfill: true, hfill: true, hgrow: Priority.ALWAYS, vgrow: Priority.ALWAYS, margin: bind if( toolbar.hidden ) Insets { left: 40 } else Insets { left: toolbar.width + 15 } }
	}
	
	init {
		content = [ assertions, toolbar ]
	}
}