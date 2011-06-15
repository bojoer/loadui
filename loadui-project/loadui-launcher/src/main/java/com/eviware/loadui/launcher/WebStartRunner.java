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
		/*
		 * String name = WebStartRunner.class.getName().replace( '.', '/' ); name
		 * = WebStartRunner.class.getResource( "/" + name + ".class" ).toString();
		 * name = name.substring( 0, name.indexOf( ".jar" ) ); name =
		 * name.substring( name.lastIndexOf( ':' ) - 1, name.lastIndexOf( '/' ) +
		 * 1 ).replace( '%', ' ' ); File dir = new File( new File(
		 * name.replaceAll( "%\\d{1,2}", " " ).replace( '/', File.separatorChar )
		 * ).getParentFile(), "jre" + File.separator + "bin" );
		 */

		try
		{
			Runtime.getRuntime().exec( new String[] { "javaws", "loadUI.jnlp" }, null,
					new File( "jre" + File.separator + "bin" ) );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
