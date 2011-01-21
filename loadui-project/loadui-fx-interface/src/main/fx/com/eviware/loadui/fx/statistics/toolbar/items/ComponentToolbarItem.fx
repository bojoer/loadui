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
package com.eviware.loadui.fx.statistics.toolbar.items;

import com.eviware.loadui.fx.statistics.toolbar.StatisticsToolbarItem;
import com.eviware.loadui.fx.FxUtils.*;

import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.FxUtils;

import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.MainWindow;
import javafx.scene.text.Text;

import com.eviware.loadui.api.model.ComponentItem;

import com.eviware.loadui.api.component.ComponentDescriptor;

def defaultImage = Image { url: "{__ROOT__}images/png/default-component-icon.png" };

public class ComponentToolbarItem extends StatisticsToolbarItem {
   
   public var component:ComponentItem on replace {
   	component.addEventListener( BaseEvent.class, new LabelListener() );
      label = component.getLabel();
      tooltip = "Adds {label} to a chart";
   }
   
   public var descriptor: ComponentDescriptor on replace {
		icon = if( descriptor.getIcon() != null ){
				Image { url: descriptor.getIcon().toString() }
			}
			else {
				defaultImage;   
			}
	}
   
   override var category = "COMPONENTS";
    
}

class LabelListener extends EventHandler {
	override function handleEvent(e):Void {
		def event:BaseEvent = e as BaseEvent;
		
		if( event.getKey().equals( ComponentItem.LABEL ) )
		{
			FxUtils.runInFxThread( function():Void {
				label = component.getLabel();
			} );
		}
	} 
}
