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

package com.eviware.loadui.fx;

import javafx.stage.Stage;
import javafx.scene.Scene;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ContainerEvent;
import javafx.lang.FX;
import javax.swing.JFrame;

public var instance:SplashWindowController;
public function getInstance():SplashWindowController { instance }

public class SplashWindowController extends AWTEventListener {

	public var stage:Stage;
	//var frame:JFrame;
	public var splash:Boolean;

	init {
		Toolkit.getDefaultToolkit().addAWTEventListener( this, AWTEvent.COMPONENT_EVENT_MASK );
		instance = this;
	}
	
	override function eventDispatched( event:AWTEvent ) {
		if(event.getID() == ContainerEvent.COMPONENT_RESIZED) {
			if(event.getSource() instanceof JFrame) {
				var frame:JFrame = event.getSource() as JFrame;
				if ( frame.getTitle() == stage.title ) {
					Toolkit.getDefaultToolkit().removeAWTEventListener(this);
					if( splash ) {
						frame.setVisible( true );
						frame.setAlwaysOnTop( true );
						splash = false;
					}
				}
			}
		}
	}
	
	function isFullscreen():Boolean {
		stage.fullScreen
	}
	
	function setFullscreen( fullscreen:Boolean ):Void {
		stage.fullScreen = fullscreen
	}
	
	public function close():Void {
		println("closing splash");
		stage.close()
	}
	
};
