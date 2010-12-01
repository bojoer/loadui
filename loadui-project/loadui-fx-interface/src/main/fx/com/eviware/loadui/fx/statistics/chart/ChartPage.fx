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
import javafx.scene.layout.Resizable;
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

import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.model.ChartGroup;

/**
 * A Page displaying ChartGroupHolders, which allows adding, removing, and reordering of its children.
 *
 * @author dain.nilsson
 */
public class ChartPage extends BaseNode, Resizable {
	public-init var statisticPage:StatisticPage on replace {
		innerContent = for( chartGroup in statisticPage.getChildren() ) ChartGroupHolder {
			chartGroup: chartGroup as ChartGroup
			layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.SOMETIMES }
		};
	}
	
	var innerContent:ChartGroupHolder[];
	
	override var layoutInfo = LayoutInfo {
		hfill: true
		vfill: true
		hgrow: Priority.ALWAYS
		vgrow: Priority.ALWAYS
	}
	
	def resizable:VBox = VBox {
		padding: Insets { left: 5, top: 5, right: 5, bottom: 5 }
		spacing: 5
		width: bind width
		height: bind height
		content: [
			Region { width: bind width, height: bind height, managed: false, style: "-fx-background-color: white;" },
			VBox { spacing: 5, layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS }, content: bind innerContent },
			Region { layoutInfo: LayoutInfo { height: 50, vfill: true, hfill: true, vgrow: Priority.ALWAYS, hgrow: Priority.ALWAYS }, style: "-fx-background-color: red;" },
		]
	}
	
	override function create():Node {
		resizable
	}
	
	override function getPrefHeight( width:Number ):Number {
		resizable.getPrefHeight( width )
	}
	
	override function getPrefWidth( height:Number ):Number {
		resizable.getPrefWidth( height )
	}
}