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
package com.eviware.loadui.impl.reporting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubReportURLHandler extends URLStreamHandler
{
	public static final Logger log = LoggerFactory.getLogger( SubReportURLHandler.class );
	private static final Map<String, byte[]> reportCache = Collections
			.synchronizedMap( new WeakHashMap<String, byte[]>() );

	private final ReportEngine reportEngine;

	public SubReportURLHandler( ReportEngine reportEngine )
	{
		this.reportEngine = reportEngine;
	}

	@Override
	public URLConnection openConnection( URL url ) throws IOException
	{
		// log.debug( "Getting subreport for url [" + url + "]" );
		String subreportFileName = url.getPath();
		log.debug( "Looking for subreport : " + subreportFileName );
		LReportTemplate subreport = reportEngine.getReport( subreportFileName );

		// get xml compile it and pass connection to it..
		String xml = subreport.getData();

		// cached?
		if( !reportCache.containsKey( xml ) )
		{
			ByteArrayInputStream inputStream = new ByteArrayInputStream( xml.getBytes() );
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			try
			{
				JasperDesign design = JRXmlLoader.load( inputStream );
				JasperCompileManager.compileReportToStream( design, outputStream );
			}
			catch( JRException e )
			{
				e.printStackTrace();
			}

			reportCache.put( xml, outputStream.toByteArray() );
		}

		return new SubreportConnection( url, reportCache.get( xml ) );
	}

	private static class SubreportConnection extends URLConnection
	{
		private final ByteArrayInputStream in;

		protected SubreportConnection( URL url, byte[] compiledReport )
		{
			super( url );
			in = new ByteArrayInputStream( compiledReport );
		}

		@Override
		public InputStream getInputStream() throws IOException
		{
			return in;
		}

		@Override
		public void connect() throws IOException
		{
		}
	}
}
