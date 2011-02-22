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
package com.eviware.loadui.util.reporting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LReportTemplate
{
	private String path;
	private String reportName;
	private String xml = null;
	private String description;

	private Logger log = LoggerFactory.getLogger( LReportTemplate.class );
	private long lastModified;

	public LReportTemplate( LReportTemplate report )
	{
		this.path = report.getPath();
		this.reportName = report.reportName;
	}

	public LReportTemplate( String name, File templateFile )
	{
		this.reportName = name;
		this.path = templateFile.getAbsolutePath();
		this.lastModified = templateFile.lastModified();
	}

	public String getName()
	{
		return reportName;
	}

	public void setName( String name )
	{
		this.reportName = name;
	}

	public String getPath()
	{
		return this.path;
	}

	public void setPath( String path )
	{
		this.path = path;
	}

	public String getData()
	{
		if( xml == null )
			xml = readTemplateFile();
		return xml;
	}

	private String readTemplateFile()
	{
		StringBuilder result = new StringBuilder();
		File reportFile = new File( path );
		this.lastModified = reportFile.lastModified();
		try
		{
			BufferedReader reader = new BufferedReader( new FileReader( reportFile ) );
			String line = null;
			while( ( line = reader.readLine() ) != null )
			{
				result.append( line );
			}
		}
		catch( FileNotFoundException e )
		{
			log.error( "Report template file not found " + path, e );
		}
		catch( IOException e )
		{
			log.error( "Error reading report template file " + path, e );
		}
		return result.toString();
	}

	public void setData( String xml )
	{
		this.xml = xml;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription( String description )
	{
		this.description = description;
	}

	private boolean isChanged()
	{
		File reportFile = new File( path );
		if( lastModified == reportFile.lastModified() )
		{
			return false;
		}
		return true;
	}

	public void update()
	{
		if( isChanged() )
		{
			log.info( "Updating report template [" + reportName + "]" );
			xml = readTemplateFile();
		}
	}
}
