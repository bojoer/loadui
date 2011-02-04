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
*InspectorManager.fx
*
*Created on feb 3, 2010, 15:57:22 em
*/

package com.eviware.loadui.fx.osgi;

import com.eviware.loadui.fx.FxUtils.*;

import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.toolbar.Toolbar;
import com.eviware.loadui.fx.ui.toolbar.ToolbarItem;
import com.eviware.loadui.api.component.ComponentRegistry;

import com.eviware.loadui.fx.widgets.toolbar.ComponentToolbarItem;

import com.eviware.loadui.api.component.ComponentDescriptor;

import javafx.util.Sequences;

def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.osgi.ComponentManager" );

/**
 * Manages the Components, obtained from OSGi bundles.
 * 
 * @author nenad.ristic
 */
public class ComponentManager extends ComponentRegistry.DescriptorListener {
	public-init var projectToolbar:Toolbar;
	public-init var testcaseToolbar:Toolbar;
	
	var componentRegistry:ComponentRegistry;
	def items = new HashMap();
	def testcaseItems = new HashMap();

	
	public function setProjectToolbar( toolbar:Toolbar ):Void {
		projectToolbar = toolbar
	}
	
	public function setTestcaseToolbar( toolbar:Toolbar ):Void {
		testcaseToolbar = toolbar
	}
	
	/**
	 * Used the Register the instance of the component registry
	 * 
	 * @param inspector
	 * @param properties
	 */
	public function onBind( componentRegistry:ComponentRegistry, properties:Map ):Void {
		log.debug( "Adding the ComponentRegistry '\{\}' to the Application.", componentRegistry );
		this.componentRegistry = componentRegistry;
		componentRegistry.addDescriptorListener( this );
		
		for ( descriptor in componentRegistry.getDescriptors() )
			descriptorAdded( descriptor );
	}
	
	/**
	 * If the componentRegistry ever does Unbind, either the application is shutting down,
	 * or something has gone very, very wrong.
	 * 
	 * @param inspector
	 * @param properties
	 */
	public function onUnbind( componentRegistry:ComponentRegistry, properties:Map ):Void {
		componentRegistry.removeDescriptorListener( this );
	}
	
	override function descriptorAdded( descriptor:ComponentDescriptor ):Void {
		log.debug( "Added ComponentDescriptor: \{\}", descriptor.getLabel() );
		FxUtils.runInFxThread( function():Void {
			def toolbarItem = ComponentToolbarItem { descriptor: descriptor };
			def tctoolbarItem = ComponentToolbarItem { descriptor: descriptor };
			items.put( descriptor, toolbarItem );
			testcaseItems.put( descriptor, tctoolbarItem);
			projectToolbar.addItem( toolbarItem );
			testcaseToolbar.addItem( tctoolbarItem );
		} );
	}
	
	override function descriptorRemoved( descriptor:ComponentDescriptor ):Void {
		log.debug( "Removed ComponentDescriptor: \{\}", descriptor );
		FxUtils.runInFxThread( function():Void {
			def toolbarItem = items.remove( descriptor ) as ToolbarItem;
			def tcToolbarItem = testcaseItems.remove ( descriptor ) as ToolbarItem;
			projectToolbar.removeItem( toolbarItem );
			testcaseToolbar.removeItem( tcToolbarItem );
		} );
	}
	 
	function populateProjectToolbar() {
		FxUtils.runInFxThread( function():Void {
			var ds:String[];
			for (descriptor in componentRegistry.getDescriptors()) {
				insert descriptor.getLabel() into ds;
			}
			for (descriptor in componentRegistry.getDescriptors()) {
				projectToolbar.addItem( ComponentToolbarItem { descriptor: descriptor } );
			}
		} );
	}
	
	public function findDescriptor(label:String) {
	    componentRegistry.findDescriptor(label);
	}
}
