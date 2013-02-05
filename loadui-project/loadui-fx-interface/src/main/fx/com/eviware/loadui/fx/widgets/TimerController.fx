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
*TimerController.fx
*
*Created on May 11, 2010, 10:31:30 AM
*/

package com.eviware.loadui.fx.widgets;

import javafx.scene.control.ToggleButton;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;

import java.util.EventObject;
import java.util.HashSet;

public def RUNNING = 0;
public def PAUSED = 1;
public def STOPPED = 2;

mixin public class TimerController {

	public-read def canvasListener = new CanvasListener();
	
	public var playButton:ToggleButton;
	
    public var timeLimit:Integer on replace {
		canvas.setLimit( CanvasItem.TIMER_COUNTER, timeLimit );
	}
	
	public var failureLimit:Integer on replace {
		canvas.setLimit( CanvasItem.FAILURE_COUNTER, failureLimit );
	}
	
	public var requestLimit:Integer on replace {
		canvas.setLimit( CanvasItem.REQUEST_COUNTER, requestLimit );
	}
	
	public var canvas:CanvasItem on replace oldCanvas = newCanvas {
		timeLimit = canvas.getLimit( CanvasItem.TIMER_COUNTER );
		requestLimit = canvas.getLimit( CanvasItem.REQUEST_COUNTER );
		failureLimit = canvas.getLimit( CanvasItem.FAILURE_COUNTER );
		
		playButton.selected = canvas.isRunning();
		
		if( oldCanvas != null )
			oldCanvas.removeEventListener( ActionEvent.class, canvasListener );
		
		if( newCanvas != null ) {
			newCanvas.addEventListener( ActionEvent.class, canvasListener );
			state = if( newCanvas.isRunning() ) RUNNING else STOPPED;
		} else {
			state = STOPPED;
		}
	}
	
	public-read var state = STOPPED;
};

class CanvasListener extends EventHandler {
	override function handleEvent( e:EventObject ) {
		def event = e as ActionEvent;
		if( event.getKey() == CanvasItem.STOP_ACTION ) {
			FxUtils.runInFxThread( function():Void { playButton.selected = false; state = PAUSED; } )
		} else if( event.getKey() == CanvasItem.START_ACTION ) {
			FxUtils.runInFxThread( function():Void { 
				playButton.selected = true; 
				state = RUNNING; 
			} )
		} else if( event.getKey() == CanvasItem.COMPLETE_ACTION ) {
			FxUtils.runInFxThread( function():Void {
				playButton.selected = false;
				state = STOPPED;
			} )
		}
	}
}
