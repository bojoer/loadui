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
*LogInspector.fx
*
*Created on feb 1, 2010, 14:01:09 em
*/

package com.eviware.loadui.fx.log;

import javafx.scene.control.ListView;

import com.eviware.loadui.api.ui.inspector.Inspector;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.Logger;

/**
 * Static factory method easily invocable from Java Code.
 */
public function createInstance( packages:String, name:String ) {
	def logInspector = LogInspector { packages: packages };
	logInspector.setName( name );
	
	logInspector;
}

/**
 * A Log4J Appender which displays its output as an Inspector.
 *
 * @author dain.nilsson
 */
public class LogInspector extends AppenderSkeleton, Inspector {
	public var maxLines:Integer = 20;
	public-init var packages:String;
	
	def panel = ListView {};
	
	postinit {
		Logger.getLogger( packages ).addAppender( this );
	}

	override function append( event:LoggingEvent ):Void {
		insert LoggingEventWrapper { loggingEvent: event } into panel.items;
		while( sizeof panel.items > maxLines )
			delete panel.items[0];
		panel.selectLastRow();
	}
	
	override function requiresLayout() {
		false
	}
	
	override function close():Void {
	}
	
	override function onShow() {
	}
	
	override function onHide() {
	}
	
	override function getPanel() {
		panel
	}
	
	override function getHelpUrl():String {
		return "http://www.eviware.com";
	}
	
	override function toString() {
		getName()
	}
}
