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
package com.eviware.loadui.fx.statistics.chart.line;

import javafx.scene.control.CheckBox;
import com.eviware.loadui.fx.FxUtils;

import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.api.charting.line.LineChart;

import java.beans.PropertyChangeEvent;

public class FollowCheckBox extends CheckBox {
	override var text = "Follow";
	
	def followListener = new FollowListener();
	
	public-init var chartView:LineChartView on replace {
		chartView.addEventListener( PropertyChangeEvent.class, followListener );
		selected = Boolean.parseBoolean( chartView.getAttribute( LineChart.FOLLOW_ATTRIBUTE, "true" ) );
	}
	
	var eventTriggered = false;
	override var selected on replace {
		if( not eventTriggered ) {
			chartView.fireEvent( new PropertyChangeEvent( chartView, LineChart.FOLLOW, not selected, selected ) );
		}
	}
}

class FollowListener extends WeakEventHandler {
	override function handleEvent( e ):Void {
		def event = e as PropertyChangeEvent;
		if( LineChart.FOLLOW.equals( event.getPropertyName() ) ) {
			FxUtils.runInFxThread(function():Void {
				eventTriggered = true;
				selected = event.getNewValue() as Boolean;
				eventTriggered = false;
			});
		}
	}
}