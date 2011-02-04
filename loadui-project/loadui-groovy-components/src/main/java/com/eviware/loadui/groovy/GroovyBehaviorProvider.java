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

import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.eviware.loadui.groovy.categories.GroovyAnalysis;
import com.eviware.loadui.groovy.categories.GroovyFlow;
import com.eviware.loadui.groovy.categories.GroovyMisc;
import com.eviware.loadui.groovy.categories.GroovyOutput;
import com.eviware.loadui.groovy.categories.GroovyRunner;
import com.eviware.loadui.groovy.categories.GroovyScheduler;
import com.eviware.loadui.groovy.categories.GroovyGenerator;

public class GroovyBehaviorProvider implements BehaviorProvider
{
	public static final String TYPE = "com.eviware.loadui.groovy.GroovyComponent";

	public final static String SCRIPT_PROPERTY = "_script";
	public final static String SCRIPT_FILE_ATTRIBUTE = "_scriptFile";
	public final static String DIGEST_ATTRIBUTE = "_digest";

	private static final int UPDATE_FREQUENCY = 5;

	private final File scriptDir;
	private final ComponentRegistry registry;
	private final Map<File, ScriptDescriptor> scripts = new HashMap<File, ScriptDescriptor>();
	private final ScheduledFuture<?> future;
	private final Set<ComponentContext> activeComponents = new HashSet<ComponentContext>();

	private final ComponentDescriptor emptyDescriptor = new ComponentDescriptor( TYPE, "misc", "EmptyScriptComponent",
			"", null );

	public GroovyBehaviorProvider( ComponentRegistry registry, ScheduledExecutorService scheduler, File scriptDir )
	{
		this.scriptDir = scriptDir;
		this.registry = registry;

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
			context.setAttribute( SCRIPT_FILE_ATTRIBUTE, scriptDescriptor.getScriptFile().getAbsolutePath() );
			context.setAttribute( DIGEST_ATTRIBUTE, scriptDescriptor.getDigest() );
			return createBehaviorProxy( context, ( ( ScriptDescriptor )descriptor ).getScript() );
		}
		else if( descriptor == emptyDescriptor )
		{
			context.setCategory( descriptor.getCategory() );
			return createBehaviorProxy( context, null );
		}
		return null;
	}

	@Override
	public ComponentBehavior loadBehavior( String componentType, ComponentContext context )
			throws ComponentCreationException
	{
		if( TYPE.equals( componentType ) )
		{
			String scriptPath = context.getAttribute( SCRIPT_FILE_ATTRIBUTE, null );
			String digest = context.getAttribute( DIGEST_ATTRIBUTE, null );
			if( scriptPath != null && digest != null )
			{
				for( Entry<File, ScriptDescriptor> entry : scripts.entrySet() )
				{
					if( entry.getKey().getAbsolutePath().equals( scriptPath ) )
					{
						ScriptDescriptor d = entry.getValue();
						if( !digest.equals( d.getDigest() ) )
						{
							// Script file has changed, update the component.
							context.setAttribute( DIGEST_ATTRIBUTE, d.getDigest() );
							context.getProperty( SCRIPT_PROPERTY ).setValue( d.getScript() );
						}
						break;
					}
				}
			}
			return createBehaviorProxy( context, null );
		}
		return null;
	}

	private ComponentBehavior createBehaviorProxy( ComponentContext context, String script )
			throws ComponentCreationException
	{
		ProxyCreator proxyCreator = new ProxyCreator( context, script );
		try
		{
			if( EventQueue.isDispatchThread() )
				proxyCreator.run();
			else
				EventQueue.invokeAndWait( proxyCreator );
		}
		catch( Exception e )
		{
			throw new ComponentCreationException( TYPE, e );
		}
		return proxyCreator.proxy;
	}

	private Object createBase( GroovyContextProxy handler, Class<? extends ComponentBehavior> category )
	{
		ComponentContext context = ( ComponentContext )handler.getProxy();
		if( GeneratorCategory.class == category )
		{
			return new GroovyGenerator( context, handler );
		}
		else if( RunnerCategory.class == category )
		{
			return new GroovyRunner( context, handler );
		}
		else if( FlowCategory.class == category )
		{
			return new GroovyFlow( context, handler );
		}
		else if( AnalysisCategory.class == category )
		{
			return new GroovyAnalysis( context, handler );
		}
		else if( OutputCategory.class == category )
		{
			return new GroovyOutput( context, handler );
		}
		else if( MiscCategory.class == category )
		{
			return new GroovyMisc( context, handler );
		}
		else if( SchedulerCategory.class == category )
		{
			return new GroovyScheduler( context, handler );
		}
		else
		{
			context.setCategory( MiscCategory.CATEGORY );
			return new GroovyMisc( context, handler );
		}
	}

	private Class<? extends ComponentBehavior> getCategoryType( String category )
	{
		if( GeneratorCategory.CATEGORY.equalsIgnoreCase( category ) || "generator".equalsIgnoreCase( category ) )
		{
			return GeneratorCategory.class;
		}
		else if( RunnerCategory.CATEGORY.equalsIgnoreCase( category ) || "runner".equalsIgnoreCase( category ) )
		{
			return RunnerCategory.class;
		}
		else if( FlowCategory.CATEGORY.equalsIgnoreCase( category ) )
		{
			return FlowCategory.class;
		}
		else if( AnalysisCategory.CATEGORY.equalsIgnoreCase( category ) )
		{
			return AnalysisCategory.class;
		}
		else if( OutputCategory.CATEGORY.equalsIgnoreCase( category ) )
		{
			return OutputCategory.class;
		}
		else if( SchedulerCategory.CATEGORY.equalsIgnoreCase( category ) )
		{
			return SchedulerCategory.class;
		}
		else if( MiscCategory.CATEGORY.equalsIgnoreCase( category ) )
		{
			return MiscCategory.class;
		}
		else
		{
			return MiscCategory.class;
		}
	}

	public void destroy()
	{
		future.cancel( true );
	}

	private static class ScriptDescriptor extends ComponentDescriptor
	{
		private final File script;
		private final long changed;
		private final String digest;
		private final String helpUrl;
		private final static Pattern pattern = Pattern.compile( "(?s)\\s*/\\*+\\s?(.*?)\\*/" );

		public static ScriptDescriptor parseFile( File script )
		{
			Map<String, String> params = new HashMap<String, String>();
			String baseName = script.getName().substring( 0, script.getName().lastIndexOf( ".groovy" ) );
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

			return new ScriptDescriptor( script, params.get( "category" ), params.get( "name" ),
					params.get( "description" ), icon.exists() ? icon : null, params.get( "digest" ), params.get( "help" ) );
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

		private ScriptDescriptor( File script, String category, String label, String description, File icon,
				String digest, String helpUrl )
		{
			super( TYPE, category, label, description, icon == null ? null : icon.toURI(), null );
			this.script = script;
			this.digest = digest;
			this.helpUrl = helpUrl;
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

		@Override
		public String getHelpUrl()
		{
			return helpUrl;
		}
	}

	private class ProxyCreator implements Runnable
	{
		private final ComponentContext context;
		private final String script;

		private ComponentBehavior proxy;

		public ProxyCreator( ComponentContext context, String script )
		{
			this.context = context;
			this.script = script;
		}

		@Override
		public void run()
		{
			Class<? extends ComponentBehavior> categoryType = getCategoryType( context.getCategory() );
			GroovyContextProxy handler = new GroovyContextProxy( context, script, categoryType, activeComponents );
			Object base = createBase( handler, categoryType );
			handler.setDelegates( new Object[] { base, context, new GroovyContextSupport( context ) } );
			handler.init();
			proxy = ( ComponentBehavior )handler.getProxy();
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
						// Check for existing components using an older version of
						// this
						// script.
						for( ComponentContext c : activeComponents )
						{
							if( file.getAbsolutePath().equals( c.getAttribute( SCRIPT_FILE_ATTRIBUTE, null ) )
									&& !descriptor.getDigest().equals( c.getAttribute( DIGEST_ATTRIBUTE, null ) ) )
							{
								System.out.println( "Script changed, updating component: " + c.getLabel() );
								c.setAttribute( DIGEST_ATTRIBUTE, descriptor.getDigest() );
								c.getProperty( SCRIPT_PROPERTY ).setValue( descriptor.getScript() );
							}
						}
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
