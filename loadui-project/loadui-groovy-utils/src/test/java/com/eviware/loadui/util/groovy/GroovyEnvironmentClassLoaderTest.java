package com.eviware.loadui.util.groovy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class GroovyEnvironmentClassLoaderTest
{

	private static final String CLASS_NOT_IN_CLASS_PATH = "org.fit.cssbox.css.CSSUnits";
	private static final String[] DEPENDENCY = { "cssbox", "cssbox", "3.4" };

	private static String normalGrapeRoot;
	private static GroovyEnvironmentClassLoader cl;

	@BeforeClass
	public static void setup()
	{
		cl = new GroovyEnvironmentClassLoader( GroovyEnvironmentClassLoaderTest.class.getClassLoader() );

		File f = new File( "target" );
		normalGrapeRoot = System.getProperty( "grape.root" );
		System.setProperty( "grape.root", f.getAbsolutePath() );
	}

	@AfterClass
	public static void cleanup() throws IOException
	{
		System.setProperty( "grape.root", normalGrapeRoot );

		try
		{
			cl.close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		cl = null;
	}

	@Test( expected = ClassNotFoundException.class )
	public void ensureClassUsedForTestsIsNotInClassPath() throws ClassNotFoundException
	{
		getClass().getClassLoader().loadClass( CLASS_NOT_IN_CLASS_PATH );
	}

	@Test
	public void ensureNormalDownloadOfDependencyWorks() throws Exception
	{
		cl.loadDependency( DEPENDENCY[0], DEPENDENCY[1], DEPENDENCY[2] );
		Class<?> cls = cl.loadClass( CLASS_NOT_IN_CLASS_PATH );
		assertEquals( CLASS_NOT_IN_CLASS_PATH, cls.getName() );
	}

	@Test
	public void testLoadJarFile() throws Exception
	{

		// we must assume normal dependency loading worked to test this
		ensureNormalDownloadOfDependencyWorks();

		boolean dependencyAdded = cl.loadJarFile( DEPENDENCY[0], DEPENDENCY[1], DEPENDENCY[2] );

		assertTrue( dependencyAdded );

	}

}
