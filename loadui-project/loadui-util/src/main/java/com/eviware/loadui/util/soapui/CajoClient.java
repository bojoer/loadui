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
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.util.StringUtils;

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

	Logger logger = LoggerFactory.getLogger( CajoClient.class );

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
			logger.warn( "Could not connect to SoapUI cajo server on " + getConnectionString() + " , method name:"
					+ method );
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
			try
			{
				soapUIPath = ( String )invoke( "getSoapUIPath", null );
				if( soapUIPath != null )
				{
					workspaceProviderRegistry.getWorkspace().getProperty( WorkspaceItem.SOAPUI_PATH_PROPERTY )
							.setValue( soapUIPath );
				}
			}
			catch( Exception e )
			{
				// do nothing
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
		return pathToSoapUIBat;
	}

	public void setPathToSoapUIBat( String pathToSoapUIBat )
	{
		this.pathToSoapUIBat = pathToSoapUIBat;
		Property<?> pathProperty = workspaceProviderRegistry.getWorkspace().getProperty(
				WorkspaceItem.SOAPUI_PATH_PROPERTY );
		if( pathProperty.getValue() == null )
			pathProperty.setValue( pathToSoapUIBat );
	}

	public void setWorkspaceProviderRegistry( WorkspaceProvider workspaceProviderRegistry )
	{
		this.workspaceProviderRegistry = workspaceProviderRegistry;
	}

	public void startSoapUI()
	{
		String path = StringUtils.isNullOrEmpty( pathToSoapUIBat ) ? workspaceProviderRegistry.getWorkspace()
				.getProperty( WorkspaceItem.SOAPUI_PATH_PROPERTY ).getStringValue() : pathToSoapUIBat;
		SoapUIStarter.start( path );
	}
}
