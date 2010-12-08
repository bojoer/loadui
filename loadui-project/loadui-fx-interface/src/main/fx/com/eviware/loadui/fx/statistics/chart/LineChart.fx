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
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.node.BaseNode;

import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.ConfigurableLineChartView;

/**
 * Base LineChart Node, visualizes a LineChartView.
 *
 * @author dain.nilsson
 */
public class LineChart extends BaseNode, Resizable {
	public-init var chartView:LineChartView on replace {
		//TODO: Remove this when LineSegments are configurable within the gui.
		if( chartView instanceof ConfigurableLineChartView and sizeof chartView.getSegments() == 0) {
			def clcv = chartView as ConfigurableLineChartView;
			clcv.addSegment( "TimeTaken", "AVERAGE", "main" );
		}
	}
	
	def resizable:VBox = VBox {
		width: bind width
		height: bind height
		content: Region {
			layoutInfo: LayoutInfo { width: 400, height: 100 }
			style: "-fx-background-color: green;"
		}
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