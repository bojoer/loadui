package com.eviware.loadui.test.ui.fx;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.File;
import java.io.IOException;

import org.codehaus.groovy.control.CompilationFailedException;

public class TestScriptRunner
{
	public static void main( String[] args ) throws Exception
	{
		GUI.getStage();

		Thread.sleep( 1000 );

		TestScriptRunner runner = new TestScriptRunner();

		for( String arg : args )
		{
			File file = new File( arg );

			if( file.exists() )
			{
				runner.runScript( file );
			}
			else
			{
				System.err.println( "File: '" + file + "' does not exist! Aborting." );
			}
		}
	}

	private final GroovyShell shell;

	private TestScriptRunner()
	{

		Binding binding = new Binding();
		binding.setVariable( "controller", GUI.getController() );
		binding.setVariable( "stage", GUI.getStage() );

		shell = new GroovyShell( binding );
	}

	private void runScript( File file ) throws CompilationFailedException, IOException
	{
		if( file.isDirectory() )
		{
			for( File subFile : file.listFiles() )
			{
				if( subFile.getName().endsWith( ".groovy" ) )
				{
					runScript( subFile );
				}
			}
		}
		else
		{
			System.out.println( "Running script: " + file );
			Script script = shell.parse( file );
			System.out.println( "Completed with result: " + script.run() );
		}
	}
}
