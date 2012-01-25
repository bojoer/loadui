/*
*NotificationArea.fx
*
*Created on Jan 23, 2012, 12:15:16 PM
*/

package com.eviware.loadui.fx.ui.notification;

import javafx.scene.layout.Stack;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.animation.transition.SequentialTransition;
import javafx.animation.transition.PauseTransition;
import javafx.animation.transition.TranslateTransition;
import javafx.animation.transition.FadeTransition;

import com.eviware.loadui.fx.AppState;

import java.util.HashMap;

def HIDDEN = 0;
def SLIDE_IN = 1;
def SHOWN = 2;
def SLIDE_OUT = 3;
def FADE_OUT = 4;
def PRE_FADE_OUT = 5;

var state = HIDDEN on replace oldState {
	if( state == HIDDEN ) {
		panel.opacity = 0;
		panel.translateY = 0;
	} else if( state == SLIDE_IN ) {
		panel.opacity = 1;
		slideDownTransition.playFromStart();
	} else if( state == SHOWN ) {
		panel.opacity = 1;
		if( oldState == SLIDE_IN ) {
			mouseMovementTrigger.width = activeWindow.scene.width;
			mouseMovementTrigger.height = activeWindow.scene.height;
			delete mouseMovementTrigger from lastOverlay.overlay.content;
			insert mouseMovementTrigger into (lastOverlay = activeWindow).overlay.content;
		}
	} else if( state == SLIDE_OUT ) {
		slideUpTransition.playFromStart();
	} else if( state == FADE_OUT ) {
		fadeTransition.playFromStart();
	} else if( state == PRE_FADE_OUT ) {
		panel.opacity = 1;
		delayTransition.playFromStart();
	}
}

def areas = new HashMap();

def activeWindow = bind AppState.activeState on replace oldWindow {
	if( state == SHOWN ) {
		delete mouseMovementTrigger from lastOverlay.overlay.content;
		insert mouseMovementTrigger into (lastOverlay = activeWindow).overlay.content;
	}
}

def activeState = bind activeWindow.state on replace {
	activeArea = areas.get( "notification{activeState}" ) as NotificationArea;
}

def panel:NotificationPanel = NotificationPanel {
	layoutInfo: LayoutInfo { width: 220, hfill: false }
	opacity: 0
	action: function() { state = SLIDE_OUT }
	onMouseClicked: function( e ) { if( state == HIDDEN ) show(); }
	onMouseMoved: function( e ) {
		if( state == FADE_OUT ) {
			fadeTransition.stop();
			state = SHOWN;
		} else if( state == PRE_FADE_OUT ) {
			delayTransition.stop();
			state = SHOWN;
		}
	}
	onMouseExited: function( e ) { if( state == SHOWN ) { state = FADE_OUT } }
}

var lastOverlay:AppState;
def mouseMovementTrigger:Rectangle = Rectangle {
	fill: Color.TRANSPARENT
	onMouseMoved: function( e ) {
		delete mouseMovementTrigger from lastOverlay.overlay.content;
		state = PRE_FADE_OUT;
	}
}

var activeArea:NotificationArea on replace oldArea {
	oldArea.content = [];
	activeArea.content = panel;
}

def slideDownTransition = TranslateTransition {
	node: panel
	fromY: -200
	toY: 0
	action: function() { state = SHOWN }
}

def slideUpTransition = TranslateTransition {
	node: panel
	fromY: 0
	toY: -200
	action: function() { state = HIDDEN }
}

def fadeTransition:SequentialTransition = SequentialTransition {
	node: panel
	content: [
		PauseTransition { duration: 500ms },
		FadeTransition {
			fromValue: 1
			toValue: 0
			duration: 1s
		}
	]
	action: function() { state = HIDDEN }
}

def delayTransition:PauseTransition = PauseTransition {
	node: panel
	duration: 1500ms
	action: function() { state = FADE_OUT }
}

public function notify( message:String ):Void {
	panel.text = message;
	show();
}

public function show():Void {
	if( state == HIDDEN ) state = SLIDE_IN;
}

public class NotificationArea extends Stack {
	init {
		if( FX.isInitialized( id ) ) {
			areas.put( id, this );
		}
	}
}