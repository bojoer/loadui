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
*LoggingEventWrapper.fx
*
*Created on feb 1, 2010, 14:36:41 em
*/

package com.eviware.loadui.fx.log;

import java.util.Date;
import java.lang.RuntimeException;
import org.apache.log4j.spi.LoggingEvent;

/**
 * A Wrapper for a log4j LoggingEvent which provides a nice String representation of it.
 *
 * @author dain.nilsson
 */
public class LoggingEventWrapper {
	public-init var loggingEvent:LoggingEvent;
	var str:String = null;
	var additionalInfo:String = "";
	
	postinit {
		if( loggingEvent == null )
			throw new RuntimeException("loggingEvent was null!");
	}
	
	public function getLevel() {
		loggingEvent.getLevel();
	}
	
	public function addAdditionalInfo( info:String ) {
		additionalInfo = info;
	}
	
	override function toString() {
		if( str == null ) {
			str = "{new Date( loggingEvent.timeStamp )}:{getLevel()}:{loggingEvent.getMessage()}\r\n{additionalInfo}"
		} else {
			str
		}
	}
}
