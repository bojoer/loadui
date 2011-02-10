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

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.geometry.Insets;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.treeselector.CascadingTreeSelector;
import com.eviware.loadui.fx.statistics.chart.SegmentTreeModel;

import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.statistics.model.chart.ConfigurableLineChartView;

import java.lang.Runnable;
import java.util.Arrays;

def buttonInfo = LayoutInfo { hfill: true, hgrow: Priority.ALWAYS };

/**
 * Panel for adding a LineSegment.
 *
 * @author dain.nilsson
 */
public class AddSegmentPanel extends VBox {
	public-init var chartViews:ConfigurableLineChartView[];
	
	override var styleClass = "add-segment-panel";
	override var hpos = HPos.CENTER;
	override var vpos = VPos.CENTER;
	override var nodeVPos = VPos.BOTTOM;
	override var spacing = 4;
	
	var selected:Runnable;
	
	init {
		content = [
			CascadingTreeSelector {
				columnCount: 4
				treeModel: if( sizeof chartViews == 1 ) new SegmentTreeModel( chartViews[0] ) else new SegmentTreeModel( Arrays.asList( chartViews ) )
				allowMultiple: false
				onSelect: function(obj):Void { selected = obj as Runnable; }
				onDeselect: function(obj):Void { selected = null; }
			}, Separator {
			}, HBox {
				hpos: HPos.RIGHT
				content: Button {
					text: "Add"
					disable: bind selected == null
					action: function():Void { selected.run() }
				}
			}
		];
	}
}