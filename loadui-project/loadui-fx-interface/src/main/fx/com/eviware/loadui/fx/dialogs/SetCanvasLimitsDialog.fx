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
import com.eviware.loadui.api.model.CanvasItem;

import java.lang.RuntimeException;

public class SetCanvasLimitsDialog {
	public-init var runController:TimerController;
	
	var form:Form;
	
	var warnDialog: Dialog;
	var warnMsg: String;
	var warnMsgPlural: String;
	
	postinit {
		if( not FX.isInitialized( runController ) )
			throw new RuntimeException( "RunController needs to be set" );
		
		warnDialog = Dialog {
			title: "Limit already reached"
			showPostInit: false
			content: [
				Text { content: bind "You've entered a {warnMsg} limit{warnMsgPlural} below what you've been running.\n\rWould you like to reset the counters?" },
			]
			okText: "Yes"
			cancelText: "No"
			onOk: function() {
				reset();
				warnDialog.close();
			}
			onCancel: function() {
				warnDialog.close();
			}
		
			width : 450
			height : 150
		}
  
		
		def dialog:Dialog = Dialog {
			title: "Set run limits"
			content: [
				form = Form {
					width: bind 200
					formContent: [
						//LongInputField { id: "timeLimit", label: "Time limit (sec):", value: valueOf(runController.timeLimit) },
						TimeField { id: "timeLimit", label: "Time limit:", value: valueOf(runController.timeLimit) },
						LongInputField { id: "sampleLimit", label: "Sample limit:", value: valueOf(runController.sampleLimit) },
						LongInputField { id: "failureLimit", label: "Failure limit:", value: valueOf(runController.failureLimit) },
						CheckBoxField { id: "reset", label: "Reset counters?", value: false, translateY: 20 } as FormField
					]
				}
			]
			okText: "Set"
			onOk: function() {
				setLimits();
				if( form.getValue("reset") as Boolean ){
					reset();
					dialog.close();
				}
				else{
					dialog.close();
					validateLimits();
				}
			}
			onCancel: function() {
				dialog.close();
			}
			
			width : 235
			height : 180
		}
	}

	function setLimits(): Void {
		def tl = form.getValue( "timeLimit" );
		runController.timeLimit = if(tl != null) tl as Long else -1;
		def sl = form.getValue( "sampleLimit" );
		runController.sampleLimit = if(sl != null) sl as Long else -1;
		def fl = form.getValue( "failureLimit" );
		runController.failureLimit = if(fl != null) fl as Long else -1;
	}
	
	function reset(): Void {
		runController.canvas.triggerAction( "RESET" );
	}

	function validateLimits(): Void {
		var result: String[] = [];

		var time: Integer = runController.canvas.getCounter( CanvasItem.TIMER_COUNTER ).get();
		def tl = form.getValue("timeLimit");
		if(tl < time){
			insert "time" into result;
		}	
		
		var sampleCount: Integer = runController.canvas.getCounter( CanvasItem.SAMPLE_COUNTER ).get();
		def sl = form.getValue( "sampleLimit" );
		if(sl < sampleCount){
			insert "sample" into result;
		}
		
		var failureCount: Integer = runController.canvas.getCounter( CanvasItem.FAILURE_COUNTER ).get();
		def fl = form.getValue( "failureLimit" );
		if(fl < failureCount){
			insert "failure" into result;
		}
		
		warnMsgPlural = if(result.size() > 1) "s" else "";

		warnMsg = "";
		for(i in [0..result.size()-1]){
			if(i > 0 and i < result.size()-1){
				warnMsg = "{warnMsg}, ";
			}
			else if(i > 0 and i == result.size()-1){
				warnMsg = "{warnMsg} and ";
			}
			warnMsg = "{warnMsg}{result[i]}";
		}
		
		if(warnMsg.length()>0){
			warnDialog.show();
		}
	}
	
}


function valueOf( val:Object ) {
	if( val == null ) return null;
	
	def value = val as Integer;
	return if( value <= 0 ) null else value.longValue()
}
