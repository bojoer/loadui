package com.eviware.loadui.groovy.components;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class GroovyComponentsTest
{
	private static final Joiner pathJoiner = Joiner.on( File.separator );
	private static final File scriptDirectory = new File( pathJoiner.join( "src", "main", "groovy" ) );
	private static final File testDirectory = new File( pathJoiner.join( "src", "test", "java", "com", "eviware",
			"loadui", "groovy", "components" ) );

	@Test
	public void shouldHaveUnitTestsForEachComponent()
	{
		Predicate<File> testExists = new Predicate<File>()
		{
			@Override
			public boolean apply( File input )
			{
				String baseName = input.getName().substring( 0, input.getName().length() - 7 );
				return new File( testDirectory, baseName + "Test.java" ).exists();
			}
		};

		Set<File> scriptFiles = ImmutableSet.copyOf( scriptDirectory.listFiles( new FilenameFilter()
		{
			@Override
			public boolean accept( File dir, String name )
			{
				return name.endsWith( ".groovy" );
			}
		} ) );

		Set<File> scriptsWithoutTests = Sets.filter( scriptFiles, Predicates.not( testExists ) );

		assertThat( "Script components are missing tests!", scriptsWithoutTests, is( Collections.<File> emptySet() ) );
	}
}
