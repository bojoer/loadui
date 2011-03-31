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
package com.eviware.loadui.fx.dialogs;

import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.LayoutInfo;
import javafx.geometry.Insets;

import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.statistics.StatisticsWindow;
import com.eviware.loadui.fx.statistics.chart.ChartPage;

import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.reporting.ReportingManager;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.charting.LineChartUtils;

import java.util.ArrayList;
import java.util.Map;

public class StatisticsReportPrintDialog {
	var checkBoxes:PageCheckBox[];
	
	def reportingManager:ReportingManager = BeanInjector.getBean( ReportingManager.class );
	
	def statisticPages = StatisticsWindow.getInstance().project.getStatisticPages().getChildren();
	
	def dialog: Dialog = Dialog {
		scene: StatisticsWindow.getInstance().scene
		title: "Create report"
		okText: "Create"
		onOk: function() {
			def pages = new ArrayList();
			for( checkBox in checkBoxes[x|x.selected] ) pages.add( checkBox.page );
			dialog.close();
			var map:Map;
			try {
				AppState.byName("STATISTICS").setBlockedText("Preparing Charts...");
				AppState.byName("STATISTICS").block();
				map = LineChartUtils.createImages( pages, StatisticsWindow.execution, StatisticsWindow.comparedExecution );
			} finally {
				AppState.byName("STATISTICS").unblock();
			}
			
			AppState.byName("STATISTICS").blockingTask( function():Void {
				reportingManager.createReport( StatisticsWindow.getInstance().project.getLabel(), StatisticsWindow.execution, pages, map );
			}, function( task ):Void {
			}, "Generating Printable Report..." );
		}
		onCancel: function() {
			dialog.close();
		}
		content: [
			Label { text: "Name", styleClass: "title" },
			Label { text: "Result report {StatisticsWindow.getInstance().project.getLabel()}: {StatisticsWindow.execution.getLabel()}" }
			Separator { layoutInfo: LayoutInfo { margin: Insets { top: 5, bottom: 5 } } },
			Label { text: "Select content", styleClass: "title" },
			checkBoxes = for( statisticPage in statisticPages ) {
				PageCheckBox { text: statisticPage.getTitle(), selected: true, page: statisticPage }
			}
		]
	}
}

class PageCheckBox extends CheckBox {
	public-init var page:StatisticPage;
}