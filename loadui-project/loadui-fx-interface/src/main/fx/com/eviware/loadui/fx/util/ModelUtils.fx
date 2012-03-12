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

package com.eviware.loadui.fx.util;

import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.api.traits.Describable;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;

import com.eviware.loadui.fx.FxUtils;
import java.lang.IllegalArgumentException;
import java.util.Collection;

public class ModelUtils {
}

public function getLabelHolder( labeled:Labeled ):LabelHolder {
	LabelHolder { labeled: labeled }
}

public function getCollectionHolder( eventFirer:EventFirer, key:String ):CollectionHolder {
	CollectionHolder { owner: eventFirer, key: key }
}

public class LabelHolder extends WeakEventHandler {
	public-read var label:String = "null";
	public-read var description:String = "null";
	
	public var labeled:Labeled on replace oldLabeled {
		if( oldLabeled != null and oldLabeled instanceof EventFirer )
			(oldLabeled as EventFirer).removeEventListener( BaseEvent.class, this );
			
		if( labeled != null ) {
			if( labeled instanceof EventFirer )
				(labeled as EventFirer).addEventListener( BaseEvent.class, this );
			label = labeled.getLabel();
			if( labeled instanceof Describable ) description = (labeled as Describable).getDescription();
		} else {
			label = "null";
			description = "null";
		}
	}
	
	override function handleEvent( e ):Void {
		def baseEvent = e as BaseEvent;
		if( baseEvent.getKey().equals( Labeled.LABEL ) ) {
			FxUtils.runInFxThread( function():Void {
				label = labeled.getLabel();
			} );
		} else if( baseEvent.getKey().equals( Describable.DESCRIPTION ) ) {
			FxUtils.runInFxThread( function():Void {
				description = (labeled as Describable).getDescription();
			} );
		}
	}
}

public class CollectionHolder extends WeakEventHandler {
	public-init var items:Object[];
	
	public-init var key:String;
	
	public-init var owner:EventFirer on replace oldOwner {
		if( oldOwner != null ) {
			oldOwner.removeEventListener( BaseEvent.class, this );
		}
		if( owner != null ) {
			owner.addEventListener( BaseEvent.class, this );
		}
	}
	
	public var onAdd: function( element:Object ):Void;
	public var onRemove: function( element:Object ):Void;
	
	postinit {
		if( not FX.isInitialized( owner ) ) throw new IllegalArgumentException("No Owner set for ModelUtils.CollectionHolder!");
		
		if( sizeof items == 1 and items[0] instanceof Collection ) {
			items = for( elem in items[0] as Collection ) elem;
		}
	}
	
	override function handleEvent( e ):Void {
		if( Releasable.RELEASED.equals( (e as BaseEvent).getKey() ) ) {
			owner = null;
			for( item in items ) onRemove( item );
			items = [];
			return;
		}
		
		if( e instanceof CollectionEvent ) {
			def event = e as CollectionEvent;
			if( event.getKey().equals( key ) ) {
				if( event.getEvent() == CollectionEvent.Event.ADDED ) {
					FxUtils.runInFxThread( function():Void {
						insert event.getElement() into items;
						onAdd( event.getElement() );
					} );
				} else if( event.getEvent() == CollectionEvent.Event.REMOVED ) {
					FxUtils.runInFxThread( function():Void {
						delete event.getElement() from items;
						onRemove( event.getElement() );
					} );
				}
			}
		}
	}
}