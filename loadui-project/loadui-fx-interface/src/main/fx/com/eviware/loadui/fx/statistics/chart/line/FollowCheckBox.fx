/*
*FollowCheckBox.fx
*
*Created on maj 30, 2011, 14:06:00 em
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