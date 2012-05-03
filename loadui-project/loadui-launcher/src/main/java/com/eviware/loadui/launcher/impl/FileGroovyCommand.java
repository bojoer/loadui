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
package com.eviware.loadui.launcher.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import com.google.common.io.Closeables;

public class FileGroovyCommand extends AbstractGroovyCommand
{
	private final File scriptFile;

	public FileGroovyCommand( File scriptFile, Map<String, Object> attributes )
	{
		super( attributes );
		this.scriptFile = scriptFile;
	}

	@Override
	public String getScript()
	{
		BufferedReader br = null;
		try
		{
			br = new BufferedReader( new FileReader( scriptFile ) );
			StringBuilder s = new StringBuilder();
			String line = null;
			while( ( line = br.readLine() ) != null )
				s.append( line ).append( "\n" );

			return s.toString();
		}
		catch( IOException e )
		{
			throw new RuntimeException( e );
		}
		finally
		{
			Closables.closeQuietly( br );
		}
	}
}
