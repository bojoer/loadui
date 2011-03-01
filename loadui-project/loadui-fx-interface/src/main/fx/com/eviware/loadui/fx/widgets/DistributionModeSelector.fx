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
package com.eviware.loadui.fx.widgets;

import javafx.scene.layout.HBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.geometry.Insets;
import javafx.geometry.VPos;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.FxUtils;

import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.events.EventHandler;

def localModeListener = new LocalModeListener();

def workspace = bind MainWindow.instance.workspace on replace oldWorkspace {
	workspace.addEventListener( PropertyEvent.class, localModeListener );
	localMode = workspace.isLocalMode();
	if( oldWorkspace != null )
		oldWorkspace.removeEventListener( PropertyEvent.class, localModeListener );
}

public var localMode = workspace.isLocalMode() on replace {
	workspace.setLocalMode( localMode );
}

public class DistributionModeSelector extends HBox {
	override var padding = Insets { left: 15, right: 15, top: 1, bottom: 1 };
	override var layoutInfo = LayoutInfo { hfill: false };
	override var nodeVPos = VPos.CENTER;
	override var spacing = 4;
	override var styleClass = "distribution-mode-selector";
	
	def toggleGroup = ToggleTabGroup {};
	def localButton = ToggleButton { text: "Local", toggleGroup: toggleGroup };
	def distButton = ToggleButton { text: "Distributed", toggleGroup: toggleGroup };
	
	def myLocalMode = bind localMode on replace {
		toggleGroup.selectedToggle = if( myLocalMode ) localButton else distButton;
	} 
	
	init {
		content = [
			Region { styleClass: "distribution-mode-selector", managed: false, height: bind height, width: bind width },
			localButton,
			distButton
		];
	}
}

class ToggleTabGroup extends ToggleGroup {
	override var selectedToggle on replace oldToggle {
		if( selectedToggle == null ) {
			FX.deferAction( function():Void { oldToggle.selected = true } );
		} else {
			localMode = (selectedToggle == localButton);
		}
	}
}

class LocalModeListener extends EventHandler {
	override function handleEvent( e ) {
		def event = e as PropertyEvent;
		if( WorkspaceItem.LOCAL_MODE_PROPERTY.equals( event.getProperty().getKey() ) ) {
			FxUtils.runInFxThread( function():Void {
				localMode = event.getProperty().getValue() as Boolean;
			} );
		}
	}
}