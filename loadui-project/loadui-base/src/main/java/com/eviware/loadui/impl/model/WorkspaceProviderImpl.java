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
package com.eviware.loadui.impl.model;

import java.io.File;
import java.io.IOException;
import java.util.EventObject;

import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.util.events.EventSupport;

public class WorkspaceProviderImpl implements WorkspaceProvider
{
	public static final Logger log = LoggerFactory.getLogger( WorkspaceProviderImpl.class );

	private final EventSupport eventSupport = new EventSupport();
	private WorkspaceItem workspace;

	@Override
	public WorkspaceItem loadWorkspace( File workspaceFile )
	{
		try
		{
			log.info( "Loading workspace from file: {}", workspaceFile );
			workspace = WorkspaceItemImpl.loadWorkspace( workspaceFile );
			fireEvent( new BaseEvent( this, WORKSPACE_LOADED ) );

			return workspace;
		}
		catch( XmlException e )
		{
			throw new RuntimeException( e );
		}
		catch( IOException e )
		{
			throw new RuntimeException( e );
		}
	}

	@Override
	public WorkspaceItem getWorkspace()
	{
		return workspace;
	}

	@Override
	public boolean isWorkspaceLoaded()
	{
		return workspace != null;
	}

	@Override
	public WorkspaceItem loadDefaultWorkspace()
	{
		return loadWorkspace( new File( System.getProperty( LoadUI.LOADUI_HOME ), "workspace.xml" ) );
	}

	@Override
	public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<T> listener )
	{
		eventSupport.addEventListener( type, listener );
	}

	@Override
	public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<T> listener )
	{
		eventSupport.removeEventListener( type, listener );
	}

	@Override
	public void clearEventListeners()
	{
		eventSupport.clearEventListeners();
	}

	@Override
	public void fireEvent( EventObject event )
	{
		eventSupport.fireEvent( event );
	}
}
