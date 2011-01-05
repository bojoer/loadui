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
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.node.BaseNode;

import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.api.statistics.model.chart.ChartView;

/**
 * Base Chart Node, holds a ChartView.
 *
 * @author dain.nilsson
 */
public class ChartViewHolder extends BaseNode, Resizable, Releasable {
	override var styleClass = "chart-view-holder";
	
	public var label:String = "ChartView label";
	
	public-init var chartModel:com.eviware.loadui.api.statistics.model.Chart;
	
	public-init var chartView:ChartView on replace {
		chart = ChartRegistry.createChart( chartView, this );
		chart.update();
	}
	
	public-read var chart:BaseChart;
	var configurationPanel:HBox;
	
	def resizable:VBox = VBox {
		width: bind width
		height: bind height
		spacing: 5
		content: [
			Region { width: bind width, height: bind height, managed: false, styleClass: "chart-view-holder" },
			Label { text: bind label },
			HBox {
				layoutInfo: LayoutInfo { width: bind width }
				spacing: 5
				content: [
					VBox {
						content: [
							Rectangle { width: 100, height: 100 },
							Button {
								text: "Configure"
								action: function():Void {
									if( sizeof configurationPanel.content == 0 ) {
										showConfig( Rectangle { width: 400, height: 50 } )
									} else {
										hideConfig();
									}
								}
							}, Button {
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
			}, configurationPanel = HBox {
				visible: false
				managed: false
			}
		]
	}
	
	public function showConfig( content:Node[] ):Void {
		configurationPanel.content = content;
		configurationPanel.managed = true;
		configurationPanel.visible = true;
	}
	
	public function hideConfig():Void {
		configurationPanel.content = [];
		configurationPanel.visible = false;
		configurationPanel.managed = false;
	}
	
	public function update():Void {
		chart.update();
	}
	
	public function reset():Void {
		chart.reset();
	}
	
	override function release():Void {
		if( chart instanceof Releasable )
			(chart as Releasable).release();
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