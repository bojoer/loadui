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
*WidgetRegistry.fx
*
*Created on mar 29, 2010, 16:56:07 em
*/

package com.eviware.loadui.fx.osgi;

import javafx.scene.Node;
import javafx.ext.swing.SwingComponent;

import com.eviware.loadui.fx.FxUtils.*;

import com.eviware.loadui.api.layout.WidgetFactory;
import com.eviware.loadui.api.layout.WidgetCreationException;
import com.eviware.loadui.api.layout.LayoutComponent;

import java.util.Map;
import java.util.HashMap;
import javax.swing.JComponent;
import org.slf4j.LoggerFactory;

def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.osgi.WidgetRegistry" );

/**
 * A reference to the WidgetRegistry, once it has been instantiated.
 */
public-read var instance:WidgetRegistry;

/**
 * Keeps track of available WidgetFactories and allows the creation of Widgets by using them.
 *
 * @author dain.nilsson
 */
public class WidgetRegistry {
	def factories = new HashMap();
	
	init {
		instance = this;
	}
	
	/**
	 * When a new WidgetFactory becomes available, this method is called with a
	 * reference to that WidgetFactory.
	 * 
	 * @param factory
	 */
	public function registerFactory( factory:WidgetFactory, properties:Map ):Void {
		log.debug( "Registering WidgetFactory with id = '\{\}'.", factory.getId() );
		runInFxThread( function():Void {
			factories.put( factory.getId(), factory );
		} );
	}
	
	/**
	 * When a previously registered WidgetFactory is removed, this method is called to
	 * notify the WidgetRegistry of it so that it may be removed from the
	 * InspectorPanel.
	 * 
	 * @param factory
	 */
	public function unregisterFactory( factory:WidgetFactory, properties:Map ):Void {
		log.debug( "Unregistering WidgetFactory with id = '\{\}'.", factory.getId() );
		runInFxThread( function():Void {
			factories.remove( factory.getId() );
		} );
	}
	
	public function buildWidget( lc: LayoutComponent ):Node {
		def factoryId = lc.get("widget");
		
		if( factories.containsKey( factoryId ) ) {
			def widget = (factories.get( factoryId ) as WidgetFactory).buildWidget( lc );
			if( widget instanceof Node ) {
				widget as Node
			} else if( widget instanceof JComponent ) {
				def node = SwingComponent.wrap( widget as JComponent );
				node.blocksMouse = true;
				
				node
			} else {
				throw new WidgetCreationException( "WidgetFactory with id = {factoryId} produced an invalid Widget" );
			}
		} else {
			throw new WidgetCreationException( "No WidgetFactory exists for the id = {factoryId}" );
		}
	}
}
