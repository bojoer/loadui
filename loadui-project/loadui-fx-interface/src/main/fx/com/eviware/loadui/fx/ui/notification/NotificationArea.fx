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
import com.eviware.loadui.fx.FxUtils;

import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.api.testevents.MessageLevel;
import com.eviware.loadui.util.testevents.MessageTestEvent;

import java.util.HashMap;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

import org.slf4j.LoggerFactory;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.ui.NotificationArea" );
def dateFormat = new SimpleDateFormat( "EEE MMM dd HH:mm:ss", Locale.ENGLISH );

def HIDDEN = 0;
def SLIDE_IN = 1;
def SHOWN = 2;
def SLIDE_OUT = 3;
def FADE_OUT = 4;
def PRE_FADE_OUT = 5;

class MessageListener extends TestEventManager.TestEventObserver {
	override function onTestEvent( entry ) {
		def testEvent = entry.getTestEvent();
		if( testEvent instanceof MessageTestEvent ) {
			def messageEvent = testEvent as MessageTestEvent;
			def level = messageEvent.getLevel();
			if( level == MessageLevel.WARNING or level == MessageLevel.ERROR ) {
				FxUtils.runInFxThread( function() {
					NotificationArea.notify( messageEvent.getMessage(), new Date() );
				} );
			}
			
			if( level == MessageLevel.NOTIFICATION )
				log.info( messageEvent.getMessage() )
			else if( level == MessageLevel.WARNING )
				log.warn( messageEvent.getMessage() )
			else if( level == MessageLevel.ERROR )
				log.error( messageEvent.getMessage() );
		}
	}
}

def messageListener = new MessageListener();
def messageManager = BeanInjector.getBean( TestEventManager.class ) on replace {
	messageManager.registerObserver( messageListener );
}

var state = HIDDEN on replace oldState {
	if( state == HIDDEN ) {
		panel.messageCount = 0;
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
	action: function() { state = HIDDEN; slideUpTransition.playFromStart() }
}

def delayTransition:PauseTransition = PauseTransition {
	node: panel
	duration: 1500ms
	action: function() { state = FADE_OUT }
}

public function notify( message:String ):Void {
	notify( message, new Date() );
}

public function notify( message:String, time:Date ):Void {
	panel.dateText = dateFormat.format( time );
	panel.text = message;
	panel.messageCount++;

	if( state == HIDDEN ) {	
		state = SLIDE_IN;
	} else if( state == SLIDE_OUT ) {
		slideUpTransition.stop();
		state = SLIDE_IN;
	} else if( state == FADE_OUT ) {
		fadeTransition.stop();
		state = SLIDE_IN;
	} else if( state == PRE_FADE_OUT ) {
		delayTransition.playFromStart();
	}
}

public class NotificationArea extends Stack {
	init {
		if( FX.isInitialized( id ) ) {
			areas.put( id, this );
		}
	}
}