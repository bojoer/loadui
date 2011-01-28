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
import javafx.scene.layout.Container;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Stack;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Separator;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.geometry.VPos;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.node.BaseNode;

import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.util.ReleasableUtils;

/**
 * Base Chart Node, holds a ChartView.
 *
 * @author dain.nilsson
 */
public class ChartViewHolder extends BaseNode, Resizable, Releasable {
	override var styleClass = "chart-view-holder";
	
	public var label:String = "ChartView label";
	
	public-init var chartModel:com.eviware.loadui.api.statistics.model.Chart;
	
	def chartButtons = HBox { spacing: 5, vpos: VPos.CENTER, styleClass: "chart-group-toolbar", padding: Insets { bottom: 5, top: 5 } };
	protected def panelToggleGroup = new PanelToggleGroup();
	
	def panelHolder:Stack = Stack {
		styleClass: "chart-view-panel"
		padding: Insets { top: 17, right: 17, bottom: 17, left: 17 }
		layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS, margin: Insets { top: -7 } }
		content: Region { managed: false, width: bind panelHolder.width, height: bind panelHolder.height, styleClass: "chart-group-panel" }
		visible: bind ( sizeof panelHolder.content > 1 )
		managed: bind ( sizeof panelHolder.content > 1 )
	}
	
	public var chartView:ChartView on replace oldValue {
		ReleasableUtils.release( oldValue );
		chart = ChartRegistry.createChart( chartView, this );
		chart.update();
		chartButtons.content = rebuildChartButtons();
	}
	
	public-read var chart:BaseChart on replace oldValue {
		ReleasableUtils.release( oldValue );
	}
	
	var vbox:VBox;
	var vbox2:VBox;
	
	def resizable:VBox = VBox {
		width: bind width
		height: bind height
		spacing: 5
		content: [
			vbox = VBox {
				padding: Insets { left: 12, top: 10, right: 12, bottom: 2 }
				content: [
					Region { width: bind vbox.width, height: bind vbox.height, managed: false, styleClass: bind styleClass },
					Label { styleClass: "title", text: bind label.toUpperCase() },
					HBox {
						layoutInfo: LayoutInfo { width: bind width }
						spacing: 5
						content: [
							vbox2 = VBox {
								padding: Insets { top: 8, right: 8, bottom: 8, left: 8 }
								spacing: 4
								content: [
									Region { managed: false, width: bind vbox2.width, height: bind vbox2.height, styleClass: "chart-view-panel" },
									Label { text: "Component" },
									Rectangle { width: 50, height: 30 },
									Button {
										text: "Delete"
										visible: bind chartModel != null
										action: function():Void {
											chartModel.delete();
										}
									}
								]
							}, Stack {
								layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS, vfill: true, vgrow: Priority.ALWAYS }
								content: bind chart as Node
							}
						]
					}, chartButtons
				]
			}
			panelHolder
		]
	}
	
	protected function rebuildChartButtons():Node[] {
		[
			Label { layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS } },
			for( panelFactory in ChartRegistry.getChartPanels( chartView ) ) {
				ToggleButton {
					text: panelFactory.title
					value: panelFactory
					toggleGroup: panelToggleGroup
				}
			},
			Separator { vertical: true, layoutInfo: LayoutInfo { height: 12 }, hpos:HPos.CENTER }
		];
	}
	
	public function update():Void {
		chart.update();
	}
	
	public function reset():Void {
		chart.reset();
	}
	
	override function release():Void {
		chartView = null;
		chart = null;
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

class PanelToggleGroup extends ToggleGroup {
	override var selectedToggle on replace {
		if( selectedToggle == null ) {
			for( child in panelHolder.content ) ReleasableUtils.release( child );
			panelHolder.content = panelHolder.content[0];
		} else {
			def panelFactory = selectedToggle.value as PanelFactory;
			panelHolder.content = [ panelHolder.content[0], panelFactory.build() ];
		}
	}
}