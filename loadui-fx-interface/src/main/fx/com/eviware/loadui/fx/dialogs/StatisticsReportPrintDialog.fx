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
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.statistics.StatisticsWindow;
import com.eviware.loadui.fx.statistics.chart.ChartPage;

import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.reporting.ReportingManager;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.charting.LineChartUtils;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.events.EventHandler;

import com.eviware.loadui.fx.FxUtils;

import java.util.EventObject;
import java.util.ArrayList;
import java.util.Map;

public class StatisticsReportPrintDialog {

	var projectEventHandler = ProjectEventHandler{}
	
	var project: ProjectItem = bind MainWindow.instance.projectCanvas.canvasItem as ProjectItem on replace old {
		old.removeEventListener(BaseEvent.class, projectEventHandler);
		project.addEventListener(BaseEvent.class, projectEventHandler);
		prependSummaryCb.disable = not StatisticsWindow.execution.getSummaryReport().exists();
	}
	
	var checkBoxes:PageCheckBox[];
	
	var prependSummaryCb: CheckBox;
	
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
			
			AppState.byName("STATISTICS").blockingTask( function():Void {
				map = LineChartUtils.createImages( pages, StatisticsWindow.execution, StatisticsWindow.comparedExecution );
			}, function( task ):Void {
				AppState.byName("STATISTICS").blockingTask( function():Void {
					if( not prependSummaryCb.disabled and prependSummaryCb.selected ){
						reportingManager.createReport( StatisticsWindow.getInstance().project.getLabel(), StatisticsWindow.execution, pages, map, StatisticsWindow.execution.getSummaryReport() );
					} else {
						reportingManager.createReport( StatisticsWindow.getInstance().project.getLabel(), StatisticsWindow.execution, pages, map );
					}
				}, function( task ):Void {
				}, "Generating Printable Report..." );
			}, "Preparing Charts..." );
		}
		onCancel: function() {
			dialog.close();
		}
		content: [
			Label { text: "Name", styleClass: "title" },
			Label { text: "Result report {StatisticsWindow.getInstance().project.getLabel()}: {StatisticsWindow.execution.getLabel()}" }
			Separator { layoutInfo: LayoutInfo { margin: Insets { top: 5, bottom: 5 } } },
			Label { text: "Summary report", styleClass: "title" },
			prependSummaryCb = CheckBox { text: "Prepend summary report", selected: false },
			Separator { layoutInfo: LayoutInfo { margin: Insets { top: 5, bottom: 5 } } },
			Label { text: "Select content", styleClass: "title" },
			checkBoxes = for( statisticPage in statisticPages ) {
				PageCheckBox { text: statisticPage.getTitle(), selected: true, page: statisticPage }
			}
		]
	}
	
	postinit{
		prependSummaryCb.disable = not StatisticsWindow.execution.getSummaryReport().exists();
	}
}

class PageCheckBox extends CheckBox {
	public-init var page:StatisticPage;
}

class ProjectEventHandler extends EventHandler {
	override function handleEvent( e: EventObject ) {
		if(e instanceof BaseEvent){
			def event = e as BaseEvent;
			if( ProjectItem.SUMMARY_EXPORTED.equals( event.getKey() ) ) {
				FxUtils.runInFxThread( function():Void {
					prependSummaryCb.disable = not StatisticsWindow.execution.getSummaryReport().exists();
				});
			}
			else if( ProjectItem.START_ACTION.equals( event.getKey() ) ) {
				FxUtils.runInFxThread( function():Void {
					prependSummaryCb.disable = true;
				});
			} 
		}
	}
}	




