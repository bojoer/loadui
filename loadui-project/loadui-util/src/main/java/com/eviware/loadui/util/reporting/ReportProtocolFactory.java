package com.eviware.loadui.util.reporting;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class ReportProtocolFactory implements URLStreamHandlerFactory
{

	public ReportProtocolFactory(  )
	{
	}

	public URLStreamHandler createURLStreamHandler( String protocol )
	{
		if( protocol.equals( "subreport" ) )
		{
//			log.debug( "Creating handler for protocol [" + protocol + "]" );
			return new SubReportURLHandler( );
		}
		else if( protocol.equals( "logo" ) )
		{
//			log.debug( "Creating handler for protocol [" + protocol + "]" );
//			return new LogoProtocolHandler();
		}
	//	else log.debug( "Failed to create logo handler for protocol [" + protocol + "]" );

		return null;
	}
}