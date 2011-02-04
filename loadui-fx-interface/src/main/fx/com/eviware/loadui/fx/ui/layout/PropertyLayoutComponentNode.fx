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
*PropertyLayoutComponentNode.fx
*
*Created on mar 17, 2010, 13:43:53 em
*/

package com.eviware.loadui.fx.ui.layout;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.layout.Panel;
import javafx.scene.control.Label;
import javafx.geometry.BoundingBox;
import javafx.util.Math;

import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.FxUtils;

import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.layout.PropertyLayoutComponent;
import com.eviware.loadui.api.property.Property;

import java.util.EventObject;

public class PropertyLayoutComponentNode extends LayoutComponentNode, EventHandler {
	public def propertyLayoutComponent = bind layoutComponent as PropertyLayoutComponent;
	public def property:Property = propertyLayoutComponent.getProperty();
	
	
	public-init var widget:Widget = Widget.buildWidgetFor( propertyLayoutComponent );
	
	postinit {
		property.getOwner().addEventListener( PropertyEvent.class, this );
	}
	
	override var layoutBounds = bind lazy (widget as Node).layoutBounds;
	
	def widgetLayoutInfo = bind (widget as Node).layoutInfo on replace {
		layoutInfo = widgetLayoutInfo;
	}
	
	override var layoutInfo on replace {
		(widget as Node).layoutInfo = layoutInfo;
	}
	
	override var width on replace {
		widget.width = width;
	}
	
	override var height on replace {
		widget.height = height;
	}
	
	override function create() {
		widget as Node
	}
	
	override function handleEvent( e:EventObject ) {
		def event = e as PropertyEvent;
		if( event.getEvent() == PropertyEvent.Event.VALUE ) {
			FxUtils.runInFxThread( function():Void {
				widget.value = property.getValue();
			} );
		}
	}
	
	override function getPrefHeight( width:Float ) {
		widget.getPrefHeight( width )
	}
	
	override function getPrefWidth( height:Float ) {
		widget.getPrefWidth( height )
	}
	
	override function release() {
		property.getOwner().removeEventListener( PropertyEvent.class, this );
		super.release();
	}
}
