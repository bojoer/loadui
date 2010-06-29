/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.controller;

import groovy.ui.Console;

import java.io.File;
import java.io.IOException;

import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.model.WorkspaceProvider;

public class ControllerTest
{
	public final static Logger log = LoggerFactory.getLogger( ControllerTest.class );

	private final WorkspaceProvider workspaceProvider;
	private final ComponentRegistry componentRegistry;

	public ControllerTest( WorkspaceProvider workspaceProvider, ComponentRegistry componentRegistry )
	{
		log.debug( "ControllerTest started!" );
		this.workspaceProvider = workspaceProvider;
		this.componentRegistry = componentRegistry;
	}

	public void init()
	{
		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				File home = new File( System.getProperty( "loadui.home" ) );
				Console console = new Console();
				console.setVariable( "workspaceProvider", workspaceProvider );
				console.setVariable( "componentRegistry", componentRegistry );
				console.setVariable( "home", home );

				File initScript = new File( home, "init.groovy" );
				if( initScript.exists() )
				{
					try
					{
						console.getShell().evaluate( initScript );
					}
					catch( CompilationFailedException e )
					{
						e.printStackTrace();
					}
					catch( IOException e )
					{
						e.printStackTrace();
					}
				}
				if( !System.getProperty( "console", "false" ).equals( "false" ) )
					console.run();
			}
		} ).start();
	}
}
