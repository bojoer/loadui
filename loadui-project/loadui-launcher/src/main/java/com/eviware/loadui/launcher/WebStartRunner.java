package com.eviware.loadui.launcher;

import java.io.File;
import java.io.IOException;

public class WebStartRunner
{
	/**
	 * Java method to launch WebStart as a separate process
	 */
	public static void main( String[] args )
	{
		try
		{
			Runtime.getRuntime().exec(
					new String[] { new File( "jre/bin/javaws" ).getAbsolutePath(),
							new File( "loadUI.jnlp" ).getAbsolutePath() }, null );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
