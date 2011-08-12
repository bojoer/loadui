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
package com.eviware.loadui.fx.osgi;

import javafx.scene.image.Image;

import com.eviware.loadui.api.ui.ToolbarItem;
import com.eviware.loadui.fx.ui.toolbar.Toolbar;
import com.eviware.loadui.fx.ui.toolbar.ToolbarItemNode;

import com.eviware.loadui.fx.FxUtils.*;

import java.util.Map;
import com.google.common.base.Strings;
import com.google.common.collect.MapMaker;
import com.google.common.collect.HashMultimap;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.service.importer.ServiceReferenceProxy;
import org.springframework.osgi.context.BundleContextAware;

def toolbarMap = new MapMaker().weakValues().makeMap();
def toolbarItemMap = HashMultimap.create();
def toolbarItemNodes = new MapMaker().weakValues().makeMap();

public def TOOLBAR_ID = "toolbarId";

public function registerToolbar( toolbar:Toolbar ):Void {
	def toolbarId = toolbar.id;
	if( not Strings.isNullOrEmpty( toolbarId ) ) {
		toolbarMap.put( toolbarId, toolbar );
		
		for( toolbarItem in toolbarItemMap.get( toolbarId ) ) {
			addToolbarItem( toolbarItem as ToolbarItem, toolbar );
		}
	}
}

function addToolbarItem( toolbarItem:ToolbarItem, toolbar:Toolbar ) {
	runInFxThread( function():Void {
		def toolbarItemNode = if( toolbarItem instanceof ToolbarItemNode ) {
			toolbarItem as ToolbarItemNode
		} else {
			def node = ToolbarItemNode {
				label: toolbarItem.getLabel()
				category: toolbarItem.getCategory()
				icon: Image { url: toolbarItem.getIconUri().toString() }
			}
			toolbarItemNodes.put( toolbarItem, node );
			node
		};
		
		toolbar.addItem( toolbarItemNode );
	} );
}

public class ToolbarItemManager extends BundleContextAware {
	var bundleContext:BundleContext;
	override function setBundleContext( context ):Void {
		bundleContext = context;
	}
	
	/**
	 * When a new ToolbarItem becomes available, this method is called with a
	 * reference to that ServiceReference. This is used to get the actual object implementing the service and not a proxy provided by Spring.
	 * 
	 * @param serviceReference
	 */
	public function toolbarItemServiceAdded( serviceReference:ServiceReference ):Void {
		def nativeReference = (serviceReference as ServiceReferenceProxy).getTargetServiceReference();
		def toolbarId = nativeReference.getProperty( TOOLBAR_ID );
		if( toolbarId != null ) {
			def toolbarItem = bundleContext.getService( nativeReference ) as ToolbarItem;
			toolbarItemMap.put( toolbarId, toolbarItem );
		
			def toolbar = toolbarMap.get( toolbarId ) as Toolbar;
			if( toolbar != null ) addToolbarItem( toolbarItem, toolbar );
		}
	}
	
	/**
	 * When a new ToolbarItem becomes unavailable, this method is called with a
	 * reference to that ServiceReference. This is used to get the actual object implementing the service and not a proxy provided by Spring.
	 * 
	 * @param serviceReference
	 */
	public function toolbarItemServiceRemoved( serviceReference:ServiceReference ):Void {
		def nativeReference = (serviceReference as ServiceReferenceProxy).getTargetServiceReference();
		def toolbarId = nativeReference.getProperty( TOOLBAR_ID );
		if( toolbarId != null ) {
			def toolbarItem = bundleContext.getService( nativeReference ) as ToolbarItem;			
			toolbarItemMap.remove( toolbarId, toolbarItem );
			def toolbarItemNode = ( if( toolbarItem instanceof ToolbarItemNode ) toolbarItem else toolbarItemNodes.remove( toolbarItem ) ) as ToolbarItemNode;
			def toolbar = toolbarMap.get( toolbarId ) as Toolbar;
			
			println("From {toolbar} remove {toolbarItemNode}");
			
			if( toolbar != null and toolbarItemNode != null ) {
				runInFxThread( function():Void {
					toolbar.removeItem( toolbarItemNode );
				} );
			}
		}
	}
	
	/**
	 * When a new ToolbarItem becomes available, this method is called with a
	 * reference to that ToolbarItem and a Map of its properties.
	 * 
	 * @param toolbarItem
	 * @param properties
	 */
	public function toolbarItemAdded( toolbarItem:ToolbarItem, properties:Map ):Void {
		toolbarItemMap.put( properties.get( TOOLBAR_ID ), toolbarItem );
		
		def toolbar = toolbarMap.get( properties.get( TOOLBAR_ID ) ) as Toolbar;
		if( toolbar != null ) addToolbarItem( toolbarItem, toolbar );
	}
	
	/**
	 * When a previously bound ToolbarItem is removed, this method is called to
	 * notify the ToolbarItemManager of it so that it may be removed from the
	 * Toolbar.
	 * 
	 * @param toolbarItem
	 * @param properties
	 */
	public function toolbarItemRemoved( toolbarItem:ToolbarItem, properties:Map ):Void {
		println("!!!ITEM_REMOVED: {toolbarItem}, {properties}");
		toolbarItemMap.remove( properties.get( TOOLBAR_ID ), toolbarItem );
		def toolbarItemNode = ( if( toolbarItem instanceof ToolbarItemNode ) toolbarItem else toolbarItemNodes.remove( toolbarItem ) ) as ToolbarItemNode;
		def toolbar = toolbarMap.get( properties.get( TOOLBAR_ID ) ) as Toolbar;
		
		println("From {toolbar} remove {toolbarItemNode}");
		
		if( toolbar != null and toolbarItemNode != null ) {
			runInFxThread( function():Void {
				toolbar.removeItem( toolbarItemNode );
			} );
		}
	}
}