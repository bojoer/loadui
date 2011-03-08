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
import javafx.scene.shape.SVGPath;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.util.Math;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.Deletable;

import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.DeletableChartView;
import com.eviware.loadui.util.ReleasableUtils;

import javafx.fxd.FXDNode;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import com.eviware.loadui.fx.FxUtils.*;

import java.lang.Exception;

public def HEIGHT_ATTRIBUTE = "height";

/**
 * Base Chart Node, holds a ChartView.
 *
 * @author dain.nilsson
 */
public class ChartViewHolder extends BaseNode, Resizable, Releasable, Deletable {
   
	override var styleClass = "chart-view-holder";
	
	public var label:String = "ChartView label";
	
	public var graphic:Node = Rectangle { width: 50, height: 30 };
	
	public var typeLabel:String = "Component";
	
	public-init var chartModel:com.eviware.loadui.api.statistics.model.Chart;
	
	def chartButtons = HBox { spacing: 5, vpos: VPos.CENTER, styleClass: "chart-group-toolbar", padding: Insets { bottom: 5, top: 5, right: 3 }, layoutInfo: LayoutInfo { vfill: false, vgrow: Priority.NEVER } };
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
		if( chartView != null ) {
			chart = ChartRegistry.createChart( chartView, this );
			chart.update();
			chartButtons.content = rebuildChartButtons();
		}
	}
	
	public-read var chart:BaseChart on replace oldValue {
		ReleasableUtils.release( oldValue );
	}
	
	protected var vbox:VBox;
	var vbox2:VBox;
	var compact = true;
	
	var resizeYStart: Number = 0;
	var resizeImg: String = "{__ROOT__}images/execution-selector-resize.fxz";
		
	def resizeAction: Group = Group {
	   blocksMouse: true
      cursor: Cursor.V_RESIZE
	   content: [
	     	Rectangle {
			    width: 12  
			    height: 12
			    fill: Color.TRANSPARENT
			}
	   	FXDNode {
	   	   layoutX: -3
      		layoutY: -3 
				url: bind resizeImg
				visible: true
			}
		]
		onMousePressed: function( e: MouseEvent ) {
	   	if( e.primaryButtonDown ) {
	   	    resizeYStart = e.screenY;
	   	} 
	   }
	   onMouseDragged: function( e: MouseEvent ) {
	   	if( e.primaryButtonDown ) {
	   	    def delta = e.screenY - resizeYStart;
	   	    if((hbox.layoutInfo as LayoutInfo).height + delta >= Math.max((chart as Resizable).getMinHeight(), 105 )){
		   	   (hbox.layoutInfo as LayoutInfo).height += delta;
		   	 	setAttribute( HEIGHT_ATTRIBUTE, String.valueOf((hbox.layoutInfo as LayoutInfo).height) );
		   	 	resizeYStart = e.screenY;
	   	    }
	   	} 
	   }
	}
	
	var hbox: HBox;
	
	public-read var chartHeight = bind hbox.height on replace oldValue {
	    if(oldValue == 0 and hbox.height > 0){
	        def savedHeight = getAttribute( HEIGHT_ATTRIBUTE, "none" );
	        if(not savedHeight.equals("none")){
	            try{
	                (hbox.layoutInfo as LayoutInfo).height = Float.parseFloat(savedHeight);
	            }
	            catch(e: Exception){
	                (hbox.layoutInfo as LayoutInfo).height = hbox.height;
	            }
	        }
	        else{
	            (hbox.layoutInfo as LayoutInfo).height = hbox.height;
	        }
	    }
	}
	
	def resizable:VBox = VBox {
		width: bind width
		height: bind height
		spacing: 5
		content: [
			Stack {
				nodeHPos: HPos.RIGHT
				nodeVPos: VPos.BOTTOM
				layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS, vfill: true, vgrow: Priority.ALWAYS }
				content: [
					vbox = VBox {
						padding: Insets { left: 12, top: 10, right: 12, bottom: 2 }
						content: [
							Region { width: bind vbox.width, height: bind vbox.height, managed: false, styleClass: bind styleClass },
							hbox = HBox {
								layoutInfo: LayoutInfo{ width: bind width }
								spacing: 5
								content: [
									vbox2 = VBox {
										layoutInfo: bind if(compact) LayoutInfo { width: 100 } else null
										padding: Insets { top: 8, right: 8, bottom: 8, left: 8 }
										spacing: 4
										content: [
											Region { managed: false, width: bind vbox2.width, height: bind vbox2.height, styleClass: "chart-view-panel" },
											HBox {
												layoutInfo: LayoutInfo { vfill: false, vgrow: Priority.NEVER }
												content: [
													Label { text: bind typeLabel, layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS } },
													Button {
														styleClass: "compact-panel-button"
														graphic: SVGPath {
															fill: Color.rgb( 0xb2, 0xb2, 0xb2 )
															content: bind if(compact) "M 0 0 L 3.5 3.5 0 7 0 0 M 3.5 0 L 7 3.5 3.5 7 3.5 0" else "M 0 0 L -3.5 3.5 0 7 0 0 M -3.5 0 L -7 3.5 -3.5 7 -3.5 0"
														}
														action: function():Void { compact = not compact }
													}
												]
											}
											graphic,
											Label { text: bind label }
										]
									}, Stack {
										layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS, vfill: true, vgrow: Priority.ALWAYS }
										content: bind chart as Node
									}
								]
							}, chartButtons
						]
					},
					resizeAction
				]
			}
			panelHolder
		]
	}

	protected function rebuildChartButtons():Node[] {
		[
			Label { layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS } },
			for( panelFactory in ChartRegistry.getChartPanels( chartView ) ) {
				[
					ToggleButton {
						text: panelFactory.title
						value: panelFactory
						toggleGroup: panelToggleGroup
					}
					if(panelFactory.separator) Separator { vertical: true, layoutInfo: LayoutInfo { height: 12 }, hpos:HPos.CENTER } else null
				]
			},
			Separator { vertical: true, layoutInfo: LayoutInfo { height: 12 }, hpos:HPos.CENTER }
		];
	}
	
	protected function setAttribute(name: String, value: String): Void {
	    chartModel.setAttribute( name, value );
	}
	
	protected function getAttribute(name: String, default: String): String {
	    chartModel.getAttribute( name, default );
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
	
	override function doDelete():Void {
		if( chartView instanceof DeletableChartView )
			(chartView as DeletableChartView).delete();
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