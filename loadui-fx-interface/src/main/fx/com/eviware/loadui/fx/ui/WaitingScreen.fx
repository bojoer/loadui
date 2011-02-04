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
package com.eviware.loadui.fx.ui;

import javafx.scene.layout.Stack;
import javafx.scene.layout.VBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.Cursor;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;

import com.eviware.loadui.fx.ui.resources.DialogPanel;

public class WaitingScreen extends Stack {
	public var text:String = "Waiting...";
	
	public var onAbort:function():Void;
	
	var title = "Please wait...";
	
	def animateTitle = Timeline {
		repeatCount: Timeline.INDEFINITE
		interpolate: false
		keyFrames: [
			KeyFrame { time: 1s action: function():Void { title = "Please Wait." } },
			KeyFrame { time: 2s action: function():Void { title = "Please Wait.." } },
			KeyFrame { time: 3s action: function():Void { title = "Please Wait..." } }
		]
	}
	
	init {
		animateTitle.playFromStart();
	}
	
	override var content = [
		Rectangle {
			width: bind width
			height: bind height
			opacity: 0.3
			blocksMouse: true
			cursor: Cursor.WAIT
		}, DialogPanel {
			layoutInfo: LayoutInfo { hfill: false, vfill: false }
			body: VBox {
				padding: Insets { left: 10, top: 10, right: 10, bottom: 10 }
				spacing: 10
				content: [
					Label {
						text: bind title
						layoutInfo: LayoutInfo { width: 150 }
					}, Label {
						text: bind text
					}, Button {
						text: "Abort"
						visible: bind onAbort != null
						managed: bind onAbort != null
						action: function() { onAbort(); }
						layoutInfo: LayoutInfo { hpos: HPos.RIGHT }
					}
				]
			}
		}
	]
}