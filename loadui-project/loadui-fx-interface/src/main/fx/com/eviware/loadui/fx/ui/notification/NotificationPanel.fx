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
package com.eviware.loadui.fx.ui.notification;

import javafx.scene.layout.Stack;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.geometry.Insets;
import javafx.geometry.HPos;

import com.sun.javafx.scene.layout.Region;

public class NotificationPanel extends VBox {
	override var styleClass = "notification-panel";
	override var nodeHPos = HPos.CENTER;
	
	public var dateText = "Tue Nov 01 13:46:00";
	public var text = "Monitor failed aspectum videm treitor gorealte doire lolcat";
	public var messageCount = 0;
	
	public var action:function():Void;
	
	init {
		content = [
			Stack {
				content: [
					Region { styleClass: "base" },
					VBox {
						padding: Insets { top: 10, right: 10, bottom: 10, left: 10 }
						content: [
							HBox {
								content: [
									Label { text: bind dateText, layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS } },
									Label { text: bind "{messageCount - 1} more", visible: bind messageCount > 1 },
								]
							}
							Label { text: bind text, textWrap: true }
						]
					}
				]
			}, Stack {
				content: [
					Region { styleClass: "bottom" },
					HBox {
						padding: Insets { top: 6, right: 10, bottom: 6, left: 10 }
						content: [
							Button { text: "Event Log »", blocksMouse: false },
							Label { layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS } },
							Button { graphic: Region { styleClass: "up-arrow" }, blocksMouse: false, action: bind action }
						]
					}
				]
			}
		];
	}
}