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

import com.eviware.loadui.api.ui.inspector.Inspector;
import com.eviware.loadui.api.ui.inspector.InspectorPanel;

def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.osgi.InspectorManager" );

/**
 * Manages which Inspectors are available in the InspectorPanel. New Inspectors
 * are added as OSGi services.
 * 
 * @author dain.nilsson
 */
public class InspectorManager {
	public-init var inspectorPanel:InspectorPanel;
	public function setInspectorPanel( inspectorPanel:InspectorPanel ):Void {
		this.inspectorPanel = inspectorPanel;
	}
	
	public-init var activeInspector: String;
	public function setActiveInspector( activeInspector: String ):Void {
		this.activeInspector = activeInspector;
	}
	
	/**
	 * When a new Inspector becomes available, this method is called with a
	 * reference to that Inspector and a Map of its properties.
	 * 
	 * @param inspector
	 * @param properties
	 */
	public function onBind( inspector:Inspector, properties:Map ):Void {
		log.debug( "Adding Inspector '\{\}' to the InspectorPanel.", inspector );
		runInFxThread( function() {
			inspectorPanel.addInspector( inspector );
			if(inspector.getName().equals(activeInspector)){
				inspectorPanel.selectInspector( inspector );
			}
		} );
	}
	
	/**
	 * When a previously bound Inspector is removed, this method is called to
	 * notify the InspectorManager of it so that it may be removed from the
	 * InspectorPanel.
	 * 
	 * @param inspector
	 * @param properties
	 */
	public function onUnbind( inspector:Inspector, properties:Map ):Void {
		log.debug( "Removing Inspector '\{\}' from the InspectorPanel.", inspector );
		runInFxThread( function() {
			inspectorPanel.removeInspector( inspector );
		} );
	}
}
