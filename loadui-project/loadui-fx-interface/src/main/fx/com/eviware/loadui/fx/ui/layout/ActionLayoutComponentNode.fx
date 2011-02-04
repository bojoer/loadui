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
/*
*ActionLayoutComponentNode.fx
*
*Created on apr 14, 2010, 18:08:56 em
*/

package com.eviware.loadui.fx.ui.layout;

import javafx.scene.control.Button;
import com.eviware.loadui.fx.FxUtils;

import com.eviware.loadui.api.layout.ActionLayoutComponent;

public class ActionLayoutComponentNode extends LayoutComponentNode {
	//public def actionLayoutComponent = bind layoutComponent as ActionLayoutComponent;

	override var layoutBounds = bind lazy button.layoutBounds;

	var button:Button;
	def listener = new EnabledListener();
	
	override function create() {
		(layoutComponent as ActionLayoutComponent).registerListener( listener );
	
		button = Button {
			width: bind width
			height: bind height
			action: function() {
				(layoutComponent as ActionLayoutComponent).getAction().run();
			}
			text: (layoutComponent as ActionLayoutComponent).getLabel();
			disable: not (layoutComponent as ActionLayoutComponent).isEnabled()
		}
	}
	
	override function getPrefHeight( width:Float ) {
		button.getPrefHeight( width )
	}
	
	override function getPrefWidth( height:Float ) {
		button.getPrefWidth( height )
	}
	
	override function release() {
		super.release();
		(layoutComponent as ActionLayoutComponent).unregisterListener( listener );
		button.action = null;
	}
}

class EnabledListener extends ActionLayoutComponent.ActionEnabledListener {
	override function stateChanged( source: ActionLayoutComponent ):Void {
		FxUtils.runInFxThread( function():Void { button.disable = not source.isEnabled() } );
	}
} 
