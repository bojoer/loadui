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

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Closeables;
import com.jidesoft.utils.Lm;

/**
 * Initialized the JIDE license.
 * 
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
		catch( Exception e )
		{
			log.error( "Failed to initialize JIDE:", e );
		}
		finally
		{
			Closeables.closeQuietly( licenseStream );
		}
	}
}
