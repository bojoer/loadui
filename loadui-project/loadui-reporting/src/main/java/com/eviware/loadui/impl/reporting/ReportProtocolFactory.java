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

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class ReportProtocolFactory implements URLStreamHandlerFactory
{
	private final ReportEngine reportEngine;

	public ReportProtocolFactory( ReportEngine reportEngine )
	{
		this.reportEngine = reportEngine;
	}

	@Override
	public URLStreamHandler createURLStreamHandler( String protocol )
	{
		if( "subreport".equals( protocol ) )
		{
			// log.debug( "Creating handler for protocol [" + protocol + "]" );
			return new SubReportURLHandler( reportEngine );
		}
		//		else if( "logo".equals( protocol ) )
		//		{
		// log.debug( "Creating handler for protocol [" + protocol + "]" );
		// return new LogoProtocolHandler();
		//		}
		// else log.debug( "Failed to create logo handler for protocol [" +
		// protocol + "]" );

		return null;
	}
}