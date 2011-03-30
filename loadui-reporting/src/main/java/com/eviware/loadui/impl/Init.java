package com.eviware.loadui.impl;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jidesoft.utils.Lm;

/**
 * Initialized the JIDE license.
 * 
 * @author dain.nilsson
 */
public class Init
{
	public static final Logger log = LoggerFactory.getLogger( Init.class );

	public void initJIDE()
	{
		try
		{
			Properties jidedata = new Properties();
			jidedata.load( Init.class.getResourceAsStream( "/properties/jide.properties" ) );
			String company = jidedata.getProperty( "company" );
			String product = jidedata.getProperty( "product" );
			log.debug( "Initializing JIDE {} for {}", product, company );
			Lm.verifyLicense( company, product, jidedata.getProperty( "license" ) );
		}
		catch( Throwable e )
		{
			log.error( "Failed to initialize JIDE:", e );
		}
	}
}
