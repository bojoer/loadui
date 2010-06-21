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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import gnu.cajo.invoke.Remote;

public class CajoClient
{
	private String server = "localhost";
	private String port = "1198";
	private String itemName = "soapuiIntegration";
	private String pathToSoapUIBat;
	private WorkspaceProvider workspaceProviderRegistry;

	private static CajoClient instance;

	public static CajoClient getInstance()
	{
		if( instance == null )
		{
			return instance = new CajoClient();
		}
		else
			return instance;
	}

	Logger logger = LoggerFactory.getLogger( "com.eviware.loadui.integration.CajoClient" );

	private CajoClient()
	{
	}

	public Object getItem() throws Exception
	{
		return Remote.getItem( getConnectionString() );
	}

	public Object invoke( String method, Object object ) throws Exception
	{
		try
		{
			return Remote.invoke( getItem(), method, object );
		}
		catch( Exception e )
		{
			logger.warn( "Could not connect to SoapUI cajo server on " + getConnectionString()+ " , method name:"+method );
			return null;
		}
	}

	public boolean testConnection()
	{
		try
		{
			Remote.invoke( getItem(), "test", null );
			return true;
		}
		catch( Exception e )
		{
			return false;
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
		return pathToSoapUIBat;
	}

	public void setPathToSoapUIBat( String pathToSoapUIBat )
	{
		this.pathToSoapUIBat = pathToSoapUIBat;
	}

	public void setWorkspaceProviderRegistry( WorkspaceProvider workspaceProviderRegistry )
	{
		this.workspaceProviderRegistry = workspaceProviderRegistry;
	}

	public  void startSoapUI(){
		SoapUIStarter.start( workspaceProviderRegistry.getWorkspace().getProperty( WorkspaceItem.SOAPUI_PATH_PROPERTY )
				.getStringValue()   );
	}
}
