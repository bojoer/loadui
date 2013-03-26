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
package com.eviware.loadui.integration;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.soapui.SoapUIExtensionClassLoader;
import com.eviware.soapui.SoapUIExtensionClassLoader.SoapUIClassLoaderState;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.project.ProjectFactoryRegistry;

/**
 * Keeps track of loaded soapUI projects shared between soapUI Runners.
 * 
 * @author Ole
 * 
 */

public class SoapUIProjectLoader
{
	private static volatile SoapUIProjectLoader instance;

	private final HashMap<WsdlProject, String> projectFiles = new HashMap<>();
	private final HashMap<String, WsdlProject> loadedProjects = new HashMap<>();;
	private final HashMap<ProjectSettings, Long> timestamps = new HashMap<>();
	private final HashMap<WsdlProject, Integer> counters = new HashMap<>();
	private final Set<ProjectUpdateListener> listeners = new HashSet<>();
	private static final Logger log = LoggerFactory.getLogger( SoapUIProjectLoader.class );

	public static SoapUIProjectLoader getInstance()
	{
		if( instance == null )
		{
			return instance = new SoapUIProjectLoader();
		}
		return instance;
	}

	private SoapUIProjectLoader()
	{
		TimerTask task = new FileWatcher();
		Timer timer = new Timer();
		timer.schedule( task, new Date(), 5000 );
	}

	public void addProjectUpdateListener( ProjectUpdateListener listener )
	{
		listeners.add( listener );
	}

	public void removeProjectUpdateListener( ProjectUpdateListener listener )
	{
		listeners.remove( listener );
	}

	public WsdlProject getProject( String projectFile, String projectPassword ) throws Exception
	{
		if( loadedProjects.containsKey( projectFile ) )
		{
			WsdlProject wsdlProject = loadedProjects.get( projectFile );
			counters.put( wsdlProject, counters.get( wsdlProject ) + 1 );
			return wsdlProject;
		}
		return loadProject( projectFile, projectPassword );
	}

	public boolean isProjectLoaded( String projectFile )
	{
		return loadedProjects.containsKey( projectFile );
	}

	public WsdlProject getProject( String projectFile ) throws Exception
	{
		return getProject( projectFile, null );
	}

	private WsdlProject loadProject( String filename, String projectPassword )
	{
		File projectFile = new File( filename );
		String absolutePath = projectFile.getAbsolutePath();
		log.debug( "Caching soapUI project at [" + absolutePath + "]" );
		ProjectSettings projectSettings = new ProjectSettings( absolutePath, projectPassword );
		WsdlProject newProject = null;
		try
		{
			newProject = tryReloadingProject( projectPassword, projectFile, absolutePath, projectSettings );
		}
		catch( Exception e )
		{
			if( timestamps.containsKey( projectSettings ) )
			{
				unsetProjectPassword( projectSettings );
				try
				{
					newProject = tryReloadingProject( null, projectFile, absolutePath, projectSettings );
				}
				catch( Exception e2 )
				{
					log.debug( "error while loading project", e );
				}
			}
		}
		return newProject;
	}

	private WsdlProject tryReloadingProject( String projectPassword, File projectFile, String absolutePath,
			ProjectSettings projectSettings )
	{
		WsdlProject newProject;
		newProject = ( WsdlProject )ProjectFactoryRegistry.getProjectFactory( "wsdl" ).createNew( absolutePath,
				projectPassword );
		if( newProject.isWrongPasswordSupplied() && timestamps.containsKey( projectSettings ) )
		{
			unsetProjectPassword( projectSettings );
		}
		loadedProjects.put( absolutePath, newProject );
		projectFiles.put( newProject, absolutePath );
		counters.put( newProject, 1 );
		if( projectPassword == null )
		{
			projectPassword = newProject.getShadowPassword();
			projectSettings.setProjectPassword( projectPassword );
		}
		timestamps.put( projectSettings, projectFile.lastModified() );
		return newProject;
	}

	private void unsetProjectPassword( ProjectSettings projectSettings )
	{
		long lastModified = timestamps.remove( projectSettings );
		projectSettings.setProjectPassword( null );
		timestamps.put( projectSettings, lastModified );
	}

	public void releaseProject( WsdlProject project )
	{
		if( !counters.containsKey( project ) )
			return;

		if( counters.get( project ) == 1 )
		{
			log.debug( "Releasing soapUI project [" + project.getName() + "]" );

			counters.remove( project );

			// if this happens during a reload we shouldn't remove timestamp and
			// path
			if( loadedProjects.get( projectFiles.get( project ) ) == project )
			{
				// TODO check this remove
				timestamps.remove( new ProjectSettings( project.getPath(), project.getShadowPassword() ) );
				loadedProjects.remove( projectFiles.get( project ) );
			}

			projectFiles.remove( project );

			for( ProjectUpdateListener listener : listeners )
				listener.onProjectRelease( project );

			project.release();
		}
		else
		{
			counters.put( project, counters.get( project ) - 1 );
		}
	}

	public class FileWatcher extends TimerTask
	{
		public FileWatcher()
		{
		}

		@Override
		public final void run()
		{
			for( ProjectSettings projectSettings : timestamps.keySet() )
			{
				String filename = projectSettings.getProjectPath();
				String pass = projectSettings.getProjectPassword();
				if( new File( filename ).lastModified() > timestamps.get( projectSettings ) )
				{
					reloadProject( filename, pass );
				}
			}
		}
	}

	public static class ProjectSettings
	{
		String projectPath;
		String projectPassword;

		public ProjectSettings( String path, String pass )
		{
			this.projectPath = path;
			this.projectPassword = pass;
		}

		public String getProjectPath()
		{
			return projectPath;
		}

		public void setProjectPath( String path )
		{
			this.projectPath = path;
		}

		public String getProjectPassword()
		{
			return projectPassword;
		}

		public void setProjectPassword( String projectPassword )
		{
			this.projectPassword = projectPassword;
		}

		@Override
		public boolean equals( Object ps )
		{
			if( ps == null || !( ps instanceof ProjectSettings ) )
				return false;
			ProjectSettings comparingOne = ( ProjectSettings )ps;
			return comparingOne.getProjectPath().equals( getProjectPath() );
		}

		@Override
		public int hashCode()
		{
			return projectPath.length();
		}

		@Override
		public String toString()
		{
			return "path = \'" + projectPath + "\',pass = \'" + projectPassword + "\'";
		}
	}

	public interface ProjectUpdateListener
	{
		public void projectUpdated( String filename, WsdlProject oldProject, WsdlProject newProject );

		public void onProjectRelease( WsdlProject project );
	}

	public void reloadProject( String filename, String projectPassword )
	{
		try
		{
			WsdlProject oldProject = loadedProjects.get( filename );

			SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();
			try
			{
				WsdlProject newProject = loadProject( filename, projectPassword );

				for( ProjectUpdateListener listener : listeners )
				{
					listener.projectUpdated( filename, oldProject, newProject );
				}
			}
			finally
			{
				state.restore();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
