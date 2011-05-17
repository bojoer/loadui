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
*Movable.fx
*
*Created on mar 10, 2010, 11:21:51 fm
*/

package com.eviware.loadui.fx.ui.dnd;

import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.BaseNode.*;

import javafx.scene.input.MouseEvent;

import com.eviware.loadui.fx.FxUtils.*;

public mixin class Movable extends Draggable {
	/**
	 * Action to be performed whenever the Movable is moved.
	 */
	public var onMove: function():Void;
	
	init {
		revert = false;
		useOverlay = false;
	}
	
	override function onReleased( e:MouseEvent ) {
		if( not dragging )
			return;
		
		(this as BaseNode).layoutX += (this as BaseNode).translateX;
		(this as BaseNode).layoutY += (this as BaseNode).translateY;
			
		Draggable.onReleased( e );
		
		mouseEvent = e;
		onMove();
	}
}
