/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.util.soapui;

import gnu.cajo.invoke.Remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;

public class CajoClient
{
	private static final Logger log = LoggerFactory.getLogger( CajoClient.class );

	private String server = "localhost";
	private String port = "1198";
	private String itemName = "soapuiIntegration";
	private WorkspaceProvider workspaceProviderRegistry;

	private static CajoClient instance;

	public synchronized static CajoClient getInstance()
	{
		if( instance == null )
		{
			instance = new CajoClient();
		}
		return instance;
	}

	private CajoClient()
	{
	}

	public Object getItem() throws Exception
	{
		return Remote.getItem( getConnectionString() );
	}

	public Object invoke( String method, Object object )
	{
		try
		{
			return Remote.invoke( getItem(), method, object );
		}
		catch( Exception e )
		{
			log.warn( "Could not connect to SoapUI cajo server on " + getConnectionString() + " , method name:" + method );
			return null;
		}
	}

	public boolean testConnection()
	{
		try
		{
			Remote.invoke( getItem(), "test", null );
			setSoapUIPath();
			return true;
		}
		catch( Exception e )
		{
			return false;
		}
	}

	/**
	 * If soapUI bat folder is not specified in loadUI and there is an running
	 * instance of soapUI, takes the path of that instance and sets it to loadUI.
	 */
	public void setSoapUIPath()
	{
		String soapUIPath = workspaceProviderRegistry.getWorkspace().getProperty( WorkspaceItem.SOAPUI_PATH_PROPERTY )
				.getStringValue();
		if( soapUIPath == null || soapUIPath.trim().length() == 0 )
		{
			soapUIPath = ( String )invoke( "getSoapUIPath", null );
			if( soapUIPath != null )
			{
				workspaceProviderRegistry.getWorkspace().getProperty( WorkspaceItem.SOAPUI_PATH_PROPERTY )
						.setValue( soapUIPath );
			}
		}
	}

	public String getConnectionString()
	{
		return "//"
				+ server
				+ ":"
				+ workspaceProviderRegistry.getWorkspace().getProperty( WorkspaceItem.SOAPUI_CAJO_PORT_PROPERTY )
						.getStringValue() + "/" + itemName;
	}

	public String getServer()
	{
		return server;
	}

	public String getPort()
	{
		return port;
	}

	public String getItemName()
	{
		return itemName;
	}

	public void setServer( String server )
	{
		this.server = server;
	}

	public void setPort( String port )
	{
		this.port = port;
	}

	public void setItemName( String itemName )
	{
		this.itemName = itemName;
	}

	public String getPathToSoapUIBat()
	{
		return workspaceProviderRegistry.getWorkspace().getProperty( WorkspaceItem.SOAPUI_PATH_PROPERTY )
				.getStringValue();
	}

	public void setPathToSoapUIBat( String pathToSoapUIBat )
	{
		workspaceProviderRegistry.getWorkspace().getProperty( WorkspaceItem.SOAPUI_PATH_PROPERTY )
				.setValue( pathToSoapUIBat );
	}

	public void setWorkspaceProviderRegistry( WorkspaceProvider workspaceProviderRegistry )
	{
		this.workspaceProviderRegistry = workspaceProviderRegistry;
	}

	public void startSoapUI()
	{
		String soapUIPath = workspaceProviderRegistry.getWorkspace().getProperty( WorkspaceItem.SOAPUI_PATH_PROPERTY )
				.getStringValue();
		SoapUIStarter.start( soapUIPath );
	}
}
