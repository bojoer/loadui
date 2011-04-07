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
package com.eviware.loadui.fx;

import javafx.stage.Stage;
import javafx.scene.Scene;

import com.eviware.loadui.api.ui.WindowController;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ContainerEvent;
import javafx.lang.FX;
import javax.swing.JFrame;

public class WindowControllerImpl extends WindowController {
	public var stage:Stage;
	var frame:JFrame;
	public-read var doClose = false;
	public-init var windowTitleFilter:String;
	var isAlwaysOnTop:Boolean = false;
	
	init {
		Toolkit.getDefaultToolkit().addAWTEventListener( new Listener(), AWTEvent.COMPONENT_EVENT_MASK );
	}
	
	override function isFullscreen():Boolean {
		stage.fullScreen
	}
	
	override function setFullscreen( fullscreen:Boolean ):Void {
		stage.fullScreen = fullscreen
	}
	
	override function close():Void {
		stage.close();
	}
	
	override function forceClose():Void {
		doClose = true;
		stage.close();
	}
	
	override function toggleAlwaysOnTop()
	{
		isAlwaysOnTop = not isAlwaysOnTop;
		frame.setAlwaysOnTop( isAlwaysOnTop );
	}
	
	public function setAlwaysOnTop( enabled:Boolean )
	{
		frame.setAlwaysOnTop( enabled );
	}
	
	override function bringToFront():Void {
		stage.iconified = false;
		stage.toFront(); 
//		frame.setVisible( true );
//		frame.setAlwaysOnTop( true );
//		frame.setAlwaysOnTop( false );
	}
}

class Listener extends AWTEventListener {
	override function eventDispatched( event:AWTEvent ) {
		if(event.getID() == ContainerEvent.COMPONENT_RESIZED) {
			if(event.getSource() instanceof JFrame) {
				if ( ((event.getSource() as JFrame).getTitle().contains( windowTitleFilter ) ) ) {
					Toolkit.getDefaultToolkit().removeAWTEventListener(this);
					frame = event.getSource() as JFrame;
				}
			}
		}
	}
}
