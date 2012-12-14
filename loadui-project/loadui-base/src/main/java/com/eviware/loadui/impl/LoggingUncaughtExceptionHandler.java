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
package com.eviware.loadui.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler
{
	@Override
	public void uncaughtException( Thread t, Throwable e )
	{
		Logger log = LoggerFactory.getLogger( e.getStackTrace()[0].getClassName() );
		log.error( "Uncaught exception in Thread \"" + t.getName() + "\":", e );
	}

	public static Object setForAllThreads()
	{
		Thread.setDefaultUncaughtExceptionHandler( new LoggingUncaughtExceptionHandler() );

		return null;
	}
}
