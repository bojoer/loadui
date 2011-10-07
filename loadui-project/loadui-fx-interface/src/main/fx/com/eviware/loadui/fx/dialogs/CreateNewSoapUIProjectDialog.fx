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

import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.form.fields.*;

import com.eviware.loadui.api.model.CanvasItem;

import java.util.HashMap;

import java.io.File;
import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.util.TestExecutionUtils;

import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.categories.GeneratorCategory;
import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.api.component.categories.AnalysisCategory;

import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;

//import com.eviware.loadui.integration.SoapUIProjectLoader;
//import com.eviware.soapui.impl.wsdl.WsdlProject;
//import java.lang.ClassLoader;


public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.dialogs.CreateNewSoapuiProjectDialog" );

/**
 * Asks the user for the details to create a new soapUI project
 *
 * @author nenad.ristic */
public class CreateNewSoapUIProjectDialog {
	
	public-init var layoutX:Number;
	public-init var layoutY:Number;
	
	
	var form:Form;
			
			var numRequests:LongInputField;
			var addStatisticsDiagram:CheckBoxField;
			var autoStart:CheckBoxField;

			
		//	var soapuiProject:WsdlProject;
			
			function ok():Void  {
				var project:ProjectItem = AppState.byName("MAIN").getActiveCanvas() as ProjectItem;	
				var manager:ComponentRegistry = BeanInjector.getBean(ComponentRegistry.class);
								 
				var soapuiItem:ComponentItem = project.createComponent( "soapui Runner", manager.findDescriptor("soapUI Runner") );
				soapuiItem.setAttribute( "gui.layoutX", "0" );
				soapuiItem.setAttribute( "gui.layoutY", "200" );
				
								 var fixedRateItem:ComponentItem = project.createComponent( "Fixed Rate", manager.findDescriptor("Fixed Rate") ); 
								 fixedRateItem.setAttribute( "gui.layoutX", "50" );
								 fixedRateItem.setAttribute( "gui.layoutY", "0" );
								var fixedRateContext:ComponentContext = fixedRateItem.getContext();
								fixedRateContext.getProperty("rate").setValue(numRequests.text);
								 
								 var triggerTerminal:OutputTerminal;
								 for (terminal in fixedRateItem.getTerminals()) {
								     if (terminal.getName().equals( GeneratorCategory.TRIGGER_TERMINAL )) {
								     	triggerTerminal = terminal as OutputTerminal;
								     	break;
								     }
								     
								 }
								 
								 var controllerTerminal:InputTerminal;
								 for (terminal in soapuiItem.getTerminals()) {
								 	if (terminal.getName().equals(RunnerCategory.TRIGGER_TERMINAL)) {
								 		controllerTerminal = terminal as InputTerminal;
								 		break;
								 	}
								 }
								 project.connect(triggerTerminal, controllerTerminal);
								 
								 if (addStatisticsDiagram.selected) {
								 	var statisticsItem:ComponentItem = project.createComponent( "Statistics", manager.findDescriptor("Statistics") );
								 	statisticsItem.setAttribute( "gui.layoutX", "50" );
								 	statisticsItem.setAttribute( "gui.layoutY", "500" );
								 	var outputTerminal:OutputTerminal;
								 	for (terminal in soapuiItem.getTerminals()) {
								 		if (terminal.getName().equals(RunnerCategory.RESULT_TERMINAL)) {
								 			outputTerminal = terminal as OutputTerminal;
								 			break;
								 		}
								 	}
								 	
								 	var statisticsTerminal:InputTerminal;
								 	for (terminal in statisticsItem.getTerminals()) {
								 		if (terminal.getName().equals(AnalysisCategory.INPUT_TERMINAL)) {
								 			statisticsTerminal = terminal as InputTerminal;
								 			break;
								 		}
								 	}
								 	project.connect(outputTerminal, statisticsTerminal);
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
				
				//testsuite = SelectField{ label: "TestSuite", options: testsuites, value: "--Select Project--" },
				//testcase = SelectField{ label: "TestCase", options: testcases, value: "--Select Project--" },
				numRequests = LongInputField { label: "Number of Requests per second", action: ok },
				addStatisticsDiagram = CheckBoxField { label: "Add Statistics Component", value: false },
				autoStart = CheckBoxField { label: "Start when created?", value: false }
			]
		};
		
		dialog = if( FX.isInitialized( layoutX ) and FX.isInitialized( layoutY ) )
			Dialog {
				title: "New SoapUI Project"
				x:layoutX
				y:layoutY
				content: form
				okText: "Create"
				onOk: ok
			} 
		else
			Dialog {
				title: "New SoapUI Project"
				content: form
				okText: "Create"
				onOk: ok
			};
		
	}	
	

}
