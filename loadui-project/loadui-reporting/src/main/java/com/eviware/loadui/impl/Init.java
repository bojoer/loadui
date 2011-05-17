package com.eviware.loadui.impl;

import java.io.IOException;
import java.io.InputStream;
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
		InputStream licenseStream = null;
		try
		{
			Properties jidedata = new Properties();
			licenseStream = Init.class.getResourceAsStream( "/properties/jide.properties" );
			jidedata.load( licenseStream );
			String company = jidedata.getProperty( "company" );
			log.debug( "Initializing JIDE for {}", company );
			Lm.verifyLicense( company, jidedata.getProperty( "product" ), jidedata.getProperty( "license" ) );
		}
		catch( Throwable e )
		{
			log.error( "Failed to initialize JIDE:", e );
		}
		finally
		{
			if( licenseStream != null )
			{
				try
				{
					licenseStream.close();
				}
				catch( IOException e )
				{
					log.error( "Failed to initialize JIDE:", e );
				}
			}
		}
	}
}
