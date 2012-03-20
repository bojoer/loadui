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
package com.eviware.loadui.fx.widgets;

import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.Deletable;
import com.eviware.loadui.fx.ui.dnd.Droppable;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.widgets.ModelItemHolder;
import com.eviware.loadui.fx.dialogs.DeleteProjectDialog;
import com.eviware.loadui.fx.dialogs.DeleteModelItemDialog;
import com.eviware.loadui.fx.dialogs.DeleteDeletablesDialog;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.Stack;
import javafx.scene.layout.LayoutInfo;
import javafx.animation.transition.FadeTransition;
import javafx.animation.transition.PauseTransition;
import javafx.animation.transition.SequentialTransition;

import com.sun.javafx.scene.layout.Region;

def closed = Image { url: "{__ROOT__}images/png/trashcan-closed.png" };
def opened = Image { url: "{__ROOT__}images/png/trashcan-opened.png" };

public class Trashcan extends Stack {
	def accepter = Accepter {};
	
	def fadeIn = FadeTransition {
		node: this
		fromValue: 0
		toValue: 1
		duration: 200ms
	}
	
	def slowIn = SequentialTransition {
		node: this
		content: [
			PauseTransition { duration: 200ms },
			fadeIn
		]
		action: function() { opacity = 1 }
	}
	
	def fadeOut = FadeTransition {
		node: this
		fromValue: 1
		toValue: 0
		duration: 200ms
	}
	
	def image = ImageView { image: closed };
	
	override var layoutInfo = LayoutInfo { width: 100, height: 120 }
	override var opacity = 0;
	
	var oldOpacity:Number = 1.0;
	var oldNode:Node = null;
	def acceptHover = bind accepter.hover on replace {
		if( acceptHover ) {
			image.image = opened;
			def newNode = Draggable.currentDraggable as Node;
			if( not isReadOnly( newNode.opacity ) ) {
				oldOpacity = newNode.opacity;
				newNode.opacity = 0.4;
				oldNode = newNode;
			}
		} else {
			image.image = closed;
			if( oldNode != null ) {
				oldNode.opacity = oldOpacity;
				oldNode = null;
			}
		}
	} 

	init {
		content = Region {
			styleClass: "trashcan"
			content: image
		}
	}
	
	var shown = false;
	
	def acceptable = bind accepter.accept( Draggable.currentDraggable ) on replace {
		if( acceptable and Draggable.currentDraggable.node.scene == scene ) {
			shown = true;
			if( fadeOut.running ) {
				fadeOut.stop();
				fadeIn.time = fadeOut.duration - fadeOut.time;
				fadeIn.play();
			} else {
				slowIn.playFromStart();
			}
			
			def sceneBounds = localToScene( layoutBounds );
			accepter.layoutX = sceneBounds.minX;
			accepter.layoutY = sceneBounds.minY;
			insert accepter into AppState.byScene( scene ).overlay.content;
		} else if( not acceptable and shown ) {
			shown = false;
			if( slowIn.running ) {
				slowIn.stop();
				fadeIn.stop();
				def time = fadeIn.duration - fadeIn.time;
				if( fadeOut.duration == time ) {
					fadeOut.playFromStart();
				} else {
					fadeOut.time = time;
					fadeOut.play();	
				}
			} else if( fadeIn.running ) {
				fadeIn.stop();
				fadeOut.time = fadeIn.duration - fadeIn.time;
				fadeOut.play();
			} else {
				fadeOut.playFromStart();
			}
			
			delete accepter from AppState.byScene( scene ).overlay.content;
		}
	}
}

class Accepter extends BaseNode, Droppable {
	
	def box = Region {
		width: bind width
		height: bind height
		style: "-fx-background-color: transparent;"
	}
	
	override function create() {
		box
	}
	
	override var accept = function( d ) {
		d instanceof Deletable or d instanceof ProjectNode;
	}
	
	override var onDrop = function( d ) {
		if( d.revert ) {
			d.revert = false;
			FX.deferAction( function():Void {
				d.revert = true;
			} );
		}
		
		if( d instanceof ProjectNode ) {
			//We need to treat ProjectNode a bit differenty.
			DeleteProjectDialog { projectRef: ( d as ProjectNode ).projectRef }
		} else if( d instanceof ModelItemHolder ) {
			DeleteModelItemDialog { modelItemHolder: d as ModelItemHolder }
		} else {
			(d as Deletable).deleteObject();
		}
	}
}