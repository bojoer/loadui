/* 
 * Copyright 2011 SmartBear Software
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
package com.eviware.loadui.launcher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

public class WebStartRunner
{
	/**
	 * Java method to launch WebStart as a separate process
	 */
	public static void main( String[] args )
	{
		try
		{
			FileWriter f = new FileWriter( "c:/users/henrik/desktop/debug.txt" );
			PrintWriter out = new PrintWriter( f );
			out.println( "TEsTing" );
			out.println( System.getProperty( "java.home" ) );
			out.println( "END" );
		}
		catch( IOException e1 )
		{
			try
			{
				FileWriter f = new FileWriter( "c:/users/henrik/desktop/debug2.txt" );
			}
			catch( IOException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			e1.printStackTrace();
		}

		File javaws = new File( System.getProperty( "java.home" ) + "/bin/", System.getProperty( "os.name" ).contains(
				"Windows" ) ? "javaws.exe" : "javaws" );
		String command = javaws.exists() ? javaws.getAbsolutePath() : "javaws";

		try
		{
			Runtime.getRuntime().exec( new String[] { command, new File( "loadUI.jnlp" ).getAbsolutePath() }, null );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
