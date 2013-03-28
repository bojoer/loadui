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
package com.eviware.loadui.test.ui.fx;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javafx.stage.Stage;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.test.IntegrationTestUtils;
import com.eviware.loadui.util.test.TestUtils;

/**
 * An loadUI Controller which can be used for testing.
 * 
 * @author dain.nilsson
 */
public class ControllerFXWrapper
{
	private final File baseDir = new File( "target/controllerTest" );
	private final File homeDir = new File( baseDir, ".loadui" );
	private final OSGiFXLauncher launcher;
	private final BundleContext context;

	public ControllerFXWrapper() throws Exception
	{
		if( baseDir.exists() && !IntegrationTestUtils.deleteRecursive( baseDir ) )
			throw new RuntimeException( "Test directory already exists and cannot be deleted! "+ baseDir.getAbsolutePath() );

		if( !baseDir.mkdir() )
			throw new RuntimeException( "Could not create test directory!" );

		if( !homeDir.mkdir() )
			throw new RuntimeException( "Could not create home directory!" );

		System.setProperty( LoadUI.WORKING_DIR, baseDir.getAbsolutePath() );
		System.setProperty( LoadUI.LOADUI_HOME, homeDir.getAbsolutePath() );

		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				OSGiFXLauncher.main( baseDir, new String[] { "-nolock", "--nofx=false" } );
			}
		} ).start();

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return OSGiFXLauncher.getInstance() != null;
			}
		}, 30 );

		launcher = OSGiFXLauncher.getInstance();

		context = launcher.getBundleContext();
	}

	public Future<Stage> getStageFuture()
	{
		return OSGiFXLauncher.getStageFuture();
	}

	public void stop() throws BundleException
	{
		try
		{
			launcher.stop();
		}
		finally
		{
			IntegrationTestUtils.deleteRecursive( baseDir );
		}
	}

	public BundleContext getBundleContext()
	{
		return context;
	}
}
