/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
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

	public static CajoClient getInstance()
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
