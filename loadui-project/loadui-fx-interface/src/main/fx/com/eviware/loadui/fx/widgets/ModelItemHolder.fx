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
/*
*ModelItemHolder.fx
*
*Created on feb 10, 2010, 11:02:03 fm
*/

package com.eviware.loadui.fx.widgets;

import com.eviware.loadui.fx.FxUtils;

import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;

import java.util.EventObject;

/**
 * Mixin class for any object that wraps a ModelItem.
 */
public mixin class ModelItemHolder {

	def listener = new ReleaseListener();
	
	/**
	 * The ModelItem that this ModelItemHolder represents.
	 */
	public-read protected var modelItem: ModelItem on replace oldVal = newVal {
		if( oldVal != null )
			oldVal.removeEventListener( BaseEvent.class, listener );
		if( newVal != null )
			newVal.addEventListener( BaseEvent.class, listener );		
	}
	
	/**
	 * The user presentable name of the type of the ModelItem.
	 */
	public function getTypeName():String { modelItem.getClass().getSimpleName() }
	
	/**
	 * This function is called when the ModelItem specified by the 'modelItem' variable is released. 
	 */
	protected function release():Void {}
}

public class ReleaseListener extends EventHandler {
	override function handleEvent( e:EventObject ) {
		def event = e as BaseEvent;
		if( event.getKey() == ModelItem.RELEASED ) {
			FxUtils.runInFxThread( release );
		}
	}
}
