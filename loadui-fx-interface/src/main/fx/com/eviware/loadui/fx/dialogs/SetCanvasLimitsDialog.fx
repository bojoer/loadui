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
/*
*SetCanvasLimitsDialog.fx
*
*Created on apr 26, 2010, 11:29:14 fm
*/

package com.eviware.loadui.fx.dialogs;

import javafx.scene.text.Text;

import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.form.fields.*;

import com.eviware.loadui.fx.widgets.TimerController;

import java.lang.RuntimeException;

public class SetCanvasLimitsDialog {
	public-init var runController:TimerController;
	
	postinit {
		if( not FX.isInitialized( runController ) )
			throw new RuntimeException( "RunController needs to be set" );
		var form:Form;
		def dialog:Dialog = Dialog {
			title: "Set run limits"
			content: [
				form = Form {
					width: bind 200
					formContent: [
						LongInputField { id: "timeLimit", label: "Time limit (sec):", value: valueOf(runController.timeLimit) },
						LongInputField { id: "sampleLimit", label: "Sample limit:", value: valueOf(runController.sampleLimit) },
						LongInputField { id: "failureLimit", label: "Failure limit:", value: valueOf(runController.failureLimit) },
						CheckBoxField { id: "reset", label: "Reset counters?", value: false, translateY: 20 } as FormField
					]
				}
			]
			okText: "Set"
			onOk: function() {
				def tl = form.getValue( "timeLimit" );
				runController.timeLimit = if(tl != null) tl as Long else -1;
				def sl = form.getValue( "sampleLimit" );
				runController.sampleLimit = if(sl != null) sl as Long else -1;
				def fl = form.getValue( "failureLimit" );
				runController.failureLimit = if(fl != null) fl as Long else -1;
				
				//runController.canvas.triggerAction( "STOP" );
				if( form.getValue("reset") as Boolean )
					runController.canvas.triggerAction( "RESET" );
					
				dialog.close();
			}
			onCancel: function() {
				dialog.close();
			}
			
			width : 235
			height : 180
		}
	}
}

function valueOf( val:Object ) {
	if( val == null ) return null;
	
	def value = val as Integer;
	return if( value <= 0 ) null else value.longValue()
}
