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

package com.eviware.loadui.fx.util;

import com.eviware.loadui.api.model.Labeled;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.events.BaseEvent;

import com.eviware.loadui.fx.FxUtils;

public class ModelUtils {
}

public function getLabelHolder( labeled:Labeled ):LabelHolder {
	return LabelHolder { labeled: labeled };
}

public class LabelHolder extends WeakEventHandler {
	public-read var label:String = "null";
	
	public var labeled:Labeled on replace oldLabeled {
		if( oldLabeled != null and oldLabeled instanceof EventFirer )
			(oldLabeled as EventFirer).removeEventListener( BaseEvent.class, this );
			
		if( labeled != null ) {
			if( labeled instanceof EventFirer )
				(labeled as EventFirer).addEventListener( BaseEvent.class, this );
			label = labeled.getLabel();
		} else {
			label = "null";
		}
	}
	
	override function handleEvent( e ):Void {
		def baseEvent = e as BaseEvent;
		if( (e as BaseEvent).getKey() == Labeled.LABEL ) {
			FxUtils.runInFxThread( function():Void {
				label = labeled.getLabel();
			} );
		}
	}
}