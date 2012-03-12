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
/*
*CreateNewWebProjectDialog.fx
*
*Created on feb 10, 2010, 13:06:43 em
*/

package com.eviware.loadui.fx.dialogs;

import javafx.scene.layout.LayoutInfo;

import com.eviware.loadui.fx.statistics.chart.ChartDefaults;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.form.fields.*;
import com.eviware.loadui.fx.util.TestExecutionUtils;

import com.eviware.loadui.api.model.CanvasItem;

import java.util.HashMap;

import java.io.File;
import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.fx.AppState;

import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.categories.GeneratorCategory;
import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.api.component.categories.AnalysisCategory;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.dialogs.CreateNewWebProjectDialog" );

/**
 * Asks the user for the details to create a new web project
 *
 * @author nenad.ristic */
public class CreateNewWebProjectDialog {
	
	public-init var layoutX:Number;
	public-init var layoutY:Number;
	
	
	var form:Form;
			var url:TextField;
			var numRequests:LongInputField;
			var addStatisticsDiagram:CheckBoxField;
			var autoStart:CheckBoxField;
			
			function ok():Void  {
				 var project:ProjectItem = AppState.byName("MAIN").getActiveCanvas() as ProjectItem;	
				 var manager:ComponentRegistry = BeanInjector.getBean(ComponentRegistry.class);
				 
				 var webItem:ComponentItem = project.createComponent( "Web Page Runner", manager.findDescriptor("Web Page Runner") );
				 webItem.setAttribute( "gui.layoutX", "0" );
				 webItem.setAttribute( "gui.layoutY", "200" );
				 var webContext:ComponentContext = webItem.getContext();
				 webContext.getProperty("url").setValue(url.text);
			
				 var fixedRateItem:ComponentItem = project.createComponent( "Fixed Rate", manager.findDescriptor("Fixed Rate") ); 
				 fixedRateItem.setAttribute( "gui.layoutX", "50" );
				 fixedRateItem.setAttribute( "gui.layoutY", "0" );
				 var fixedRateContext:ComponentContext = fixedRateItem.getContext();
				 fixedRateContext.getProperty("rate").setValue(numRequests.text);
				 
				 var triggerTerminal:OutputTerminal;
				 for (terminal in fixedRateItem.getTerminals()) {
				     if(terminal.getName().equals(GeneratorCategory.TRIGGER_TERMINAL)) {
				     	triggerTerminal = terminal as OutputTerminal;
				     	break;
				     }
				     
				 }
				 
				 var controllerTerminal:InputTerminal;
				 for (terminal in webItem.getTerminals()) {
				 	if (terminal.getName().equals(RunnerCategory.TRIGGER_TERMINAL)) {
				 		controllerTerminal = terminal as InputTerminal;
				 		break;
				 	}
				 }
				 project.connect(triggerTerminal, controllerTerminal);
				 
				 if ( addStatisticsDiagram.selected ) {
				 	def page = project.getStatisticPages().getChildAt( 0 );
					def chartGroup = ChartDefaults.createChartGroup( page, null, null ); 
				 	ChartDefaults.createSubChart( chartGroup, webItem );
				 }
				 dialog.close();
				if (autoStart.selected) {
					TestExecutionUtils.startCanvas( project );
				}
			}
						
	var dialog:Dialog;
	
	postinit {
		form = Form {
			layoutInfo: LayoutInfo { width: 250 }
			formContent: [
				url = TextField { label: "Url", action: ok },
				numRequests = LongInputField { label: "Number of Requests per second", action: ok },
				addStatisticsDiagram = CheckBoxField { label: "Create chart in Statistic Workbench", value: true },
				autoStart = CheckBoxField { label: "Start when created?", value: false }
			]
		};
		
		dialog = if( FX.isInitialized( layoutX ) and FX.isInitialized( layoutY ) )
			Dialog {
				title: "New Web Project"
				x:layoutX
				y:layoutY
				content: form
				okText: "Create"
				onOk: ok
			} 
		else
			Dialog {
				title: "New Web Project"
				content: form
				okText: "Create"
				onOk: ok
			};
		
	}	
	

}
