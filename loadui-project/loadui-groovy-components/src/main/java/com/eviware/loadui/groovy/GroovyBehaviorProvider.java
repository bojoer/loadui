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
package com.eviware.loadui.groovy;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;

import com.eviware.loadui.api.component.BehaviorProvider;
import com.eviware.loadui.api.component.ComponentBehavior;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.component.categories.AnalysisCategory;
import com.eviware.loadui.api.component.categories.FlowCategory;
import com.eviware.loadui.api.component.categories.MiscCategory;
import com.eviware.loadui.api.component.categories.OutputCategory;
import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.api.component.categories.SchedulerCategory;
import com.eviware.loadui.api.component.categories.GeneratorCategory;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.groovy.categories.GroovyAnalysis;
import com.eviware.loadui.groovy.categories.GroovyFlow;
import com.eviware.loadui.groovy.categories.GroovyMisc;
import com.eviware.loadui.groovy.categories.GroovyOutput;
import com.eviware.loadui.groovy.categories.GroovyRunner;
import com.eviware.loadui.groovy.categories.GroovyScheduler;
import com.eviware.loadui.groovy.categories.GroovyGenerator;
import com.eviware.loadui.util.MapUtils;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.events.EventSupport;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

public class GroovyBehaviorProvider implements BehaviorProvider, EventFirer
{
	public static final String TYPE = "com.eviware.loadui.groovy.GroovyComponent";

	private static final int UPDATE_FREQUENCY = 5;

	private final File scriptDir;
	private final ComponentRegistry registry;
	private final Map<File, ScriptDescriptor> scripts = new HashMap<File, ScriptDescriptor>();
	private final ScheduledFuture<?> future;
	private final EventSupport eventSupport = new EventSupport();
	private final ClassLoaderRegistry clr = new ClassLoaderRegistry();

	private final ComponentDescriptor emptyDescriptor = new ComponentDescriptor( TYPE, "misc", "EmptyScriptComponent",
			"", null );

	public GroovyBehaviorProvider( ComponentRegistry registry, ScheduledExecutorService scheduler, File scriptDir )
	{
		this.scriptDir = scriptDir;
		this.registry = registry;

		File groovyRoot = new File( System.getProperty( "groovy.root" ) );
		if( !groovyRoot.isDirectory() )
			groovyRoot.mkdirs();

		File groovyConfig = new File( groovyRoot, "grapeConfig.xml" );
		if( !groovyConfig.exists() )
		{
			try
			{
				Files.copy( new InputSupplier<InputStream>()
				{
					@Override
					public InputStream getInput() throws IOException
					{
						return getClass().getResourceAsStream( "/grapeConfig.xml" );
					}
				}, groovyConfig );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}

		// registry.registerDescriptor( emptyDescriptor, this );
		registry.registerType( TYPE, this );

		future = scheduler.scheduleWithFixedDelay( new DirWatcher(), 0, UPDATE_FREQUENCY, TimeUnit.SECONDS );
	}

	@Override
	public ComponentBehavior createBehavior( ComponentDescriptor descriptor, ComponentContext context )
			throws ComponentCreationException
	{
		if( descriptor instanceof ScriptDescriptor )
		{
			ScriptDescriptor scriptDescriptor = ( ScriptDescriptor )descriptor;
			context.setCategory( descriptor.getCategory() );
			context.setAttribute( GroovyComponent.DIGEST_ATTRIBUTE, scriptDescriptor.getDigest() );
			context.setAttribute( GroovyComponent.ID_ATTRIBUTE, scriptDescriptor.getId() );
			context.setAttribute( GroovyComponent.CLASS_LOADER_ATTRIBUTE, scriptDescriptor.getClassLoaderId() );
			context.createProperty( GroovyComponent.SCRIPT_PROPERTY, String.class,
					( ( ScriptDescriptor )descriptor ).getScript() );
			return instantiateBehavior( context, ( ( ScriptDescriptor )descriptor ).getCategory() );
		}
		else if( descriptor == emptyDescriptor )
		{
			context.setCategory( descriptor.getCategory() );
			return instantiateBehavior( context, null );
		}
		return null;
	}

	@Override
	public ComponentBehavior loadBehavior( String componentType, ComponentContext context )
			throws ComponentCreationException
	{
		if( TYPE.equals( componentType ) )
		{
			String id = context.getAttribute( GroovyComponent.ID_ATTRIBUTE, "" );
			String scriptPath = context.getAttribute( GroovyComponent.SCRIPT_FILE_ATTRIBUTE, null );
			String digest = context.getAttribute( GroovyComponent.DIGEST_ATTRIBUTE, null );
			if( digest != null )
			{
				for( Entry<File, ScriptDescriptor> entry : scripts.entrySet() )
				{
					ScriptDescriptor d = entry.getValue();
					if( id.equals( d.getId() ) )
					{
						if( !digest.equals( d.getDigest() ) )
						{
							// ID matches, update the component.
							context.setAttribute( GroovyComponent.DIGEST_ATTRIBUTE, d.getDigest() );
							context.setAttribute( GroovyComponent.CLASS_LOADER_ATTRIBUTE, d.getClassLoaderId() );
							context.createProperty( GroovyComponent.SCRIPT_PROPERTY, String.class ).setValue( d.getScript() );
						}
						break;
					}
					if( entry.getKey().getAbsolutePath().equals( scriptPath ) )
					{
						if( !digest.equals( d.getDigest() ) )
						{
							// Script file has changed, update the component.
							context.setAttribute( GroovyComponent.DIGEST_ATTRIBUTE, d.getDigest() );
							context.setAttribute( GroovyComponent.ID_ATTRIBUTE, d.getId() );
							context.setAttribute( GroovyComponent.CLASS_LOADER_ATTRIBUTE, d.getClassLoaderId() );
							context.createProperty( GroovyComponent.SCRIPT_PROPERTY, String.class ).setValue( d.getScript() );
						}
						break;
					}
				}
			}
			return instantiateBehavior( context, context.getCategory() );
		}
		return null;
	}

	private ComponentBehavior instantiateBehavior( ComponentContext context, String category )
	{
		if( GeneratorCategory.CATEGORY.equalsIgnoreCase( category ) || "generator".equalsIgnoreCase( category ) )
		{
			return new GroovyGenerator( this, context );
		}
		else if( RunnerCategory.CATEGORY.equalsIgnoreCase( category ) || "runner".equalsIgnoreCase( category ) )
		{
			return new GroovyRunner( this, context );
		}
		else if( FlowCategory.CATEGORY.equalsIgnoreCase( category ) )
		{
			return new GroovyFlow( this, context );
		}
		else if( AnalysisCategory.CATEGORY.equalsIgnoreCase( category ) )
		{
			return new GroovyAnalysis( this, context );
		}
		else if( OutputCategory.CATEGORY.equalsIgnoreCase( category ) )
		{
			return new GroovyOutput( this, context );
		}
		else if( SchedulerCategory.CATEGORY.equalsIgnoreCase( category ) || "scheduler".equalsIgnoreCase( category ) )
		{
			return new GroovyScheduler( this, context );
		}
		else if( MiscCategory.CATEGORY.equalsIgnoreCase( category ) )
		{
			return new GroovyMisc( this, context );
		}
		else
		{
			return new GroovyMisc( this, context );
		}
	}

	public void destroy()
	{
		future.cancel( true );
		ReleasableUtils.releaseAll( eventSupport, clr );
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

	public ClassLoaderRegistry getClassLoaderRegistry()
	{
		return clr;
	}

	public static class ScriptDescriptor extends ComponentDescriptor
	{
		private final String id;
		private final String classLoaderId;
		private final File script;
		private final long changed;
		private final String digest;
		private final static Pattern pattern = Pattern.compile( "(?s)\\s*/\\*+\\s?(.*?)\\*/" );

		public static ScriptDescriptor parseFile( File script )
		{
			Map<String, String> params = Maps.newHashMap();
			String baseName = script.getName().substring( 0, script.getName().lastIndexOf( ".groovy" ) );
			params.put( "id", baseName );
			params.put( "name", baseName );
			params.put( "category", MiscCategory.CATEGORY );
			params.put( "description", "" );
			params.put( "icon", baseName + ".png" );

			Matcher m = pattern.matcher( getFileContent( script ) );
			if( m.find() )
			{
				StringBuilder description = new StringBuilder();
				for( String line : m.group( 1 ).split( "\r\n|\r|\n" ) )
				{
					if( line.startsWith( " *" ) )
						line = line.substring( 2 );
					line = line.trim();
					if( line.startsWith( "@" ) )
					{
						String[] parts = line.split( "\\s", 2 );
						params.put( parts[0].substring( 1 ), parts[1].trim() );
					}
					else
						description.append( line ).append( '\n' );
				}
				params.put( "description", description.toString().trim() );
			}

			File icon = new File( script.getParentFile(), params.get( "icon" ) );
			FileInputStream fis = null;
			try
			{
				fis = new FileInputStream( script );
				params.put( "digest", DigestUtils.md5Hex( fis ) );
			}
			catch( FileNotFoundException e )
			{
				e.printStackTrace();
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					if( fis != null )
						fis.close();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}

			return new ScriptDescriptor( params.get( "id" ), MapUtils.getOr( params, "classloader", params.get( "id" ) ),
					script, params.get( "category" ), params.get( "name" ), params.get( "description" ),
					icon.exists() ? icon : null, params.get( "digest" ), params.get( "help" ) );
		}

		private static String getFileContent( File file )
		{
			try
			{
				Reader in = new FileReader( file );
				StringBuilder sb = new StringBuilder();
				char[] chars = new char[1 << 16];
				int length;

				while( ( length = in.read( chars ) ) > 0 )
				{
					sb.append( chars, 0, length );
				}
				in.close();
				return sb.toString();
			}
			catch( FileNotFoundException e )
			{
				e.printStackTrace();
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
			return "";
		}

		private ScriptDescriptor( String id, String classLoaderId, File script, String category, String label,
				String description, File icon, String digest, String helpUrl )
		{
			super( TYPE, category, label, description, icon == null ? null : icon.toURI(), helpUrl );
			this.classLoaderId = classLoaderId;
			this.id = id;
			this.script = script;
			this.digest = digest;
			changed = script.lastModified();
		}

		public File getScriptFile()
		{
			return script;
		}

		public String getScript()
		{
			return getFileContent( script );
		}

		public boolean isModified()
		{
			return script.lastModified() != changed;
		}

		public String getDigest()
		{
			return digest;
		}

		public String getId()
		{
			return id;
		}

		public String getClassLoaderId()
		{
			return classLoaderId;
		}
	}

	private class DirWatcher implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if( !scriptDir.isDirectory() )
					return;

				List<File> files = Arrays.asList( scriptDir.listFiles() );
				for( Iterator<Entry<File, ScriptDescriptor>> it = scripts.entrySet().iterator(); it.hasNext(); )
				{
					Entry<File, ScriptDescriptor> entry = it.next();
					if( !entry.getKey().exists() || entry.getValue().isModified() )
					{
						registry.unregisterDescriptor( entry.getValue() );
						it.remove();
					}
				}

				for( File file : files )
				{
					if( file.getName().endsWith( ".groovy" ) && !scripts.containsKey( file ) )
					{
						ScriptDescriptor descriptor = ScriptDescriptor.parseFile( file );
						scripts.put( file, descriptor );
						registry.registerDescriptor( descriptor, GroovyBehaviorProvider.this );
						fireEvent( new PropertyChangeEvent( descriptor, file.getCanonicalPath(), null, descriptor.getScript() ) );
						fireEvent( new PropertyChangeEvent( descriptor, descriptor.getId(), null, descriptor.getScript() ) );
					}
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}
}
