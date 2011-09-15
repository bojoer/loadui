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
package com.eviware.loadui.fx.statistics.chart.line;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.HPos;

import com.javafx.preview.layout.Grid;
import com.javafx.preview.layout.GridRow;
import com.javafx.preview.layout.GridLayoutInfo;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.control.ColorPicker;
import com.eviware.loadui.fx.ui.form.fields.SelectField;

import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.charting.line.LineSegmentModel;
import com.eviware.loadui.api.charting.line.StrokeStyle;
import com.eviware.loadui.api.charting.ChartNamePrettifier;

/**
 * Panel for changing the style of LineSegments.
 *
 * @author dain.nilsson
 */
public class StylePanel extends Grid {
	public-init var lineSegmentModels:LineSegmentModel[];
	
	override var styleClass = "style-panel";
	override var padding = Insets { top: 10, right: 10, bottom: 10, left: 10 };
	override var hgap = 10;
	override var vgap = 10;
	
	init {
		rows = [
			GridRow { cells: [
				Region { managed: false, width: bind width, height: bind height, styleClass: "style-panel" },
				Label { styleClass: "header-row", text: "Color" },
				Label { styleClass: "header-row", text: "Statistic", layoutInfo: GridLayoutInfo { hspan: 3 } },
				Label { styleClass: "header-row", text: "Width", layoutInfo: GridLayoutInfo { hspan: 2 } },
				Label { styleClass: "header-row", text: "Stroke" }
			] }, for( model in lineSegmentModels ) {
				def lineSegment = model.getLineSegment();
				def slider = Slider {
					min: 1
					max: 9
					value: model.getStrokeWidth()
					minorTickCount: 0
					majorTickUnit: 1
					clickToPosition: true
				}
				def selector = SelectField {
					options: [ StrokeStyle.SOLID, StrokeStyle.DASHED, StrokeStyle.DOTTED ]
					labelProvider: function(o):String { null }
					graphicProvider: function(o):Node {
						if( o == StrokeStyle.DASHED ) SVGPath { content: "M 0 0 L 15 0 15 2 0 2 0 0 M 25 0 L 40 0 40 2 25 2 25 0 M 50 0 L 65 0 65 2 50 2 50 0" }
						else if( o == StrokeStyle.DOTTED ) SVGPath { content: "M 1 0 L 3 0 3 2 1 2 1 0 M 5 0 L 7 0 7 2 5 2 5 0 M 9 0 L 11 0 11 2 9 2 9 0 M 13 0 L 15 0 15 2 13 2 13 0 M 17 0 L 19 0 19 2 17 2 17 0 M 21 0 L 23 0 23 2 21 2 21 0 M 25 0 L 27 0 27 2 25 2 25 0 M 29 0 L 31 0 31 2 29 2 29 0 M 33 0 L 35 0 35 2 33 2 33 0 M 37 0 L 39 0 39 2 37 2 37 0 M 41 0 L 43 0 43 2 41 2 41 0 M 45 0 L 47 0 47 2 45 2 45 0 M 49 0 L 51 0 51 2 49 2 49 0 M 53 0 L 55 0 55 2 53 2 53 0 M 57 0 L 59 0 59 2 57 2 57 0 M 61 0 L 63 0 63 2 61 2 61 0" }
						else Rectangle { width: 65, height: 2 }
					}
					value: model.getStrokeStyle()
					layoutInfo: LayoutInfo { width: 90 }
					onValueChanged: function( value ):Void {
						model.setStrokeStyle( value as StrokeStyle );
					}
				}
				def width:Number = bind slider.value on replace {
					model.setStrokeWidth( width );
					slider.value = width as Integer;
				}
				var lineColor:Color = FxUtils.awtColorToFx( model.getColor() );
				GridRow { cells: [
					ColorPicker {
						color: FxUtils.awtColorToFx( model.getColor() );
						onReplace: function( color ):Void {
							lineColor = color;
							model.setColor( FxUtils.getAwtColor( color ) );
						}
					}, Label {
						text: ChartNamePrettifier.compactDataAndMetricName( lineSegment.getVariableName(), lineSegment.getStatisticName() )
						layoutInfo: LayoutInfo { minWidth: 70 }
					}, Label {
						text: ChartNamePrettifier.nameForSource( lineSegment.getSource() )
						layoutInfo: LayoutInfo { minWidth: 60 }
					}, Label {
						text: lineSegment.getStatisticHolder().getLabel()
						layoutInfo: LayoutInfo { width: 100, hshrink: Priority.ALWAYS }
					},
					Label {
						text: bind "{width as Integer}px"
						graphic: Rectangle { height: bind width as Integer, width: 15, fill: bind lineColor }
						graphicHPos: HPos.RIGHT
						layoutInfo: LayoutInfo { hshrink: Priority.NEVER, width: 45 }
					},
					slider,
					selector
				] }
			}
		];
	}
}