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
 
/**
 * This is the big loadUI logo in the upper left corner.
 *
 * Contains Window specific actions.
 */

package com.eviware.loadui.fx.ui.menu;

import javafx.scene.Group;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import javafx.scene.shape.Ellipse;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

import com.eviware.loadui.api.ui.WindowController;

import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.popup.*;
import com.eviware.loadui.fx.ui.dialogs.*;
import com.eviware.loadui.fx.ui.menu.button.*;

import com.javafx.preview.control.MenuItem;
import com.javafx.preview.control.MenuButton;

public class MainWindowButton extends Group {
	public-init var wc:WindowController;
	
	var menuButton:MenuButton;
	
	init {
		content = [
			Ellipse {
				radiusX: 29
				radiusY: 6
				centerX: 29
				centerY: 57
				fill: RadialGradient {
					centerX: 0.5
					centerY: 0.5
					focusX: 0.5
					focusY: 0.5
					stops: [
						Stop { offset: 0, color: Color.rgb( 0, 0, 0, 0.7 ) },
						Stop { offset: 0.45, color: Color.TRANSPARENT }
					]
				}
			}, menuButton = MenuButton {
				styleClass: "loadui-menu-button"
				graphic: ImageView {
						image: Image { url: "{__ROOT__}images/png/main-button-no-shadow.png" }
					}
				items: [
					CheckBox {
						styleClass: "context-menu-check-box"
						text: "Always on top"
						onMouseClicked:function(e:MouseEvent):Void {
							MainWindow.instance.wc.toggleAlwaysOnTop();
						}
					},
					Separator{},
					MenuItem {
						text: ##[ABOUT]"About"
						action: function() {
							new AboutDialog();
						}
					},
					MenuItem {
						text: ##[SYSTEM_PROPERTIES]"System Properties"
						action: function() {
							new SystemPropertiesDialog();
						}
					},
					MenuItem {
						text: ##[FORUM]"Give Feedback"
						action: function() {
							openURL("http://www.eviware.com/forum/viewforum.php?f=9")
						}
					},
					Separator{},
					MenuItem {
						text: ##[EXIT]"Exit"
						action: function() {
							if( AppState.byName("MAIN").state == MainWindow.TESTCASE_FRONT 
									or AppState.byName("MAIN").state == MainWindow.PROJECT_FRONT ) {
								ExitConfirmDialog{wc:wc};
							} else {
								ExitConfirmDialogWorkspace{wc:wc};
							}
						}
					}
				]
			}
		]
	}
	
	postinit {
		FX.deferAction( function() { menuButton.show(); FX.deferAction( function() { menuButton.hide() } ) } );
	}
}