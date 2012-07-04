/*
 * Copyright 2011 eviware software ab
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
package com.eviware.loadui.integration;

import gnu.cajo.invoke.Remote;
import gnu.cajo.utils.ItemServer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.util.soapui.CajoClient;

public class CajoServer implements Runnable
{
	private String server = null;
	private String port = "1199";
	private String itemName = "loaduiIntegration";
	private LoadUIIntegrator loadUIIntegrator;

	private volatile static CajoServer instance;

	public static CajoServer getInstance()
	{
		if( instance == null )
		{
			return instance = new CajoServer();
		}
		return instance;
	}

	private CajoServer()
	{
	}

	Logger logger = LoggerFactory.getLogger( CajoServer.class );

	public CajoServer( LoadUIIntegrator loadUIIntegrator )
	{
		this.loadUIIntegrator = loadUIIntegrator;
	}

	public void start()
	{
		Remote.config(
				server,
				Integer.valueOf( loadUIIntegrator.getWorkspaceProvider().getWorkspace()
						.getProperty( WorkspaceItem.LOADUI_CAJO_PORT_PROPERTY ).getStringValue() ), null, 0 );

		try
		{
			ItemServer.bind( loadUIIntegrator, itemName );
		}
		catch( IOException e )
		{
			logger.error( e.getMessage() );
		}
		logger.debug( "The cajo server is running on localhost:"
				+ loadUIIntegrator.getWorkspaceProvider().getWorkspace()
						.getProperty( WorkspaceItem.LOADUI_CAJO_PORT_PROPERTY ).getStringValue() + "/" + itemName );

		CajoClient.getInstance().testConnection();
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

	@Override
	public void run()
	{
		if( LoadUI.isController() )
		{
			while( loadUIIntegrator.getWorkspaceProvider().getWorkspace() == null )
			{
				try
				{
					Thread.sleep( 500 );
				}
				catch( InterruptedException e )
				{
					// Ignore
				}
			}
			start();
		}
	}

	public void setLoadUILuncher( LoadUIIntegrator loadUILuncher )
	{
		this.loadUIIntegrator = loadUILuncher;
	}
}
