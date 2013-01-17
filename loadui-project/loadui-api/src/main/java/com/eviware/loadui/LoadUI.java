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
package com.eviware.loadui;

import java.io.File;
import java.io.IOException;

public class LoadUI
{
	/**
	 * The main version number of loadUI.
	 */
	public static final String VERSION = "2.5.0-SNAPSHOT";

	/**
	 * Internal version number used to determine controller/agent compatibility.
	 * Compatibility is only ensured when this version string is the same for
	 * both agent and controller.
	 */
	public static final String AGENT_VERSION = "13";

	public static final String INSTANCE = "loadui.instance";
	public static final String CONTROLLER = "controller";
	public static final String AGENT = "agent";

	public static final String HEADLESS = "loadui.headless";

	public static final String NAME = "loadui.name";
	public static final String BUILD_NUMBER = "loadui.build.number";
	public static final String BUILD_DATE = "loadui.build.date";

	public static final String LOADUI_HOME = "loadui.home";
	public static final String WORKING_DIR = "loadui.working";

	public static final String HTTPS_PORT = "loadui.https.port";

	public static final String DISABLE_STATISTICS = "loadui.statistics.disable";
	public static final String DISABLE_DISCOVERY = "loadui.discovery.disable";

	public static final String KEY_STORE = "loadui.ssl.keyStore";
	public static final String TRUST_STORE = "loadui.ssl.trustStore";
	public static final String KEY_STORE_PASSWORD = "loadui.ssl.keyStorePassword";
	public static final String TRUST_STORE_PASSWORD = "loadui.ssl.trustStorePassword";

	public static boolean isController()
	{
		return CONTROLLER.equals( System.getProperty( INSTANCE ) );
	}

	public static boolean isHeadless()
	{
		return "true".equals( System.getProperty( HEADLESS ) );
	}

	public static boolean isPro()
	{
		return Boolean.parseBoolean( System.getProperty( "loadui.pro" ) );

	}

	/**
	 * Gets the directory from where all relative paths should be resolved.
	 * 
	 * @return
	 */
	public static File getWorkingDir()
	{
		return new File( System.getProperty( WORKING_DIR, "." ) ).getAbsoluteFile();
	}

	public static File relativeFile( String path )
	{
		return new File( getWorkingDir(), path );
	}

	public static void restart()
	{
		try
		{
			Runtime.getRuntime().exec( "loadui.bat" );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		System.exit( 0 );
	}
}
