package com.eviware.loadui.test.ui.fx.states;

import java.io.File;

import org.junit.Test;

import com.eviware.loadui.test.ui.fx.TestScriptRunner;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;

public class TestScriptRunnerTest
{
	@Test
	public void shouldRunSingleFile() throws Exception
	{
		File tmpFile = File.createTempFile( "script", ".groovy" );
		Files.write( "assert 1+2 == 3\r\nreturn 'OK'", tmpFile, Charsets.UTF_8 );

		TestScriptRunner.main( new String[] { tmpFile.getAbsolutePath() } );
	}

	@Test
	public void shouldRunDirectory() throws Exception
	{
		File tmpDir = Files.createTempDir();

		File tmpFile1 = File.createTempFile( "script", ".groovy", tmpDir );
		Files.write( "assert 1+2 == 3\r\nreturn 'OK 1'", tmpFile1, Charsets.UTF_8 );

		File tmpFile2 = File.createTempFile( "script", ".groovy", tmpDir );
		Files.write( "assert 1+2 == 3\r\nreturn 'OK 2'", tmpFile2, Charsets.UTF_8 );

		TestScriptRunner.main( new String[] { tmpDir.getAbsolutePath() } );
	}

	@Test
	public void shouldRunMultiple() throws Exception
	{
		File tmpFile = File.createTempFile( "script", ".groovy" );
		Files.write( "assert 1+2 == 3\r\nreturn 'OK A'", tmpFile, Charsets.UTF_8 );

		File tmpDir = Files.createTempDir();

		File tmpFile1 = File.createTempFile( "script", ".groovy", tmpDir );
		Files.write( "assert 1+2 == 3\r\nreturn 'OK B'", tmpFile1, Charsets.UTF_8 );

		File tmpFile2 = File.createTempFile( "script", ".groovy", tmpDir );
		Files.write( "assert 1+2 == 3\r\nreturn 'OK C'", tmpFile2, Charsets.UTF_8 );

		TestScriptRunner.main( new String[] { tmpFile.getAbsolutePath(), tmpDir.getAbsolutePath() } );
	}

	@Test
	public void runScriptFiles() throws Exception
	{
		String scripts = System.getProperty( "scripts" );
		if( !Strings.isNullOrEmpty( scripts ) )
		{
			TestScriptRunner.main( new String[] { scripts } );
		}
	}
}
