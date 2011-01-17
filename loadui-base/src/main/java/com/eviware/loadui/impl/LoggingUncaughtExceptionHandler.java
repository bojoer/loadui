package com.eviware.loadui.impl;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingUncaughtExceptionHandler implements UncaughtExceptionHandler
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
