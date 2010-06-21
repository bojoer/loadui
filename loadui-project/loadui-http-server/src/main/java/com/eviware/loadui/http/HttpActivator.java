/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.http;

import org.apache.felix.http.proxy.ProxyServlet;
import org.cometd.server.continuation.ContinuationCometdServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.eviware.loadui.api.messaging.MessageEndpointProvider;
import com.eviware.loadui.impl.messaging.BayeuxServiceMessagingProvider;

public class HttpActivator implements BundleActivator
{
	private Server sslServer;
	private Server server;

	@Override
	public void start( BundleContext bc ) throws Exception
	{
		server = new Server();

		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort( Integer.parseInt( System.getProperty( "loadui.http.port", "8080" ) ) );
		server.addConnector( connector );

		ServletContextHandler context = new ServletContextHandler( server, "/" );
		context.addServlet( new ServletHolder( new StatusServlet() ), "/*" );

		sslServer = new Server();

		System.setProperty( "javax.net.ssl.keyStore", System.getProperty( "user.home" ) + "/.loadui/keystore.jks" );
		System.setProperty( "javax.net.ssl.trustStore", System.getProperty( "user.home" ) + "/.loadui/keystore.jks" );
		System.setProperty( "javax.net.ssl.keyStorePassword", "password" );
		System.setProperty( "javax.net.ssl.trustStorePassword", "password" );

		SslSelectChannelConnector sslConnector = new SslSelectChannelConnector();
		sslConnector.setKeystore( System.getProperty( "javax.net.ssl.keyStore" ) );
		sslConnector.setKeyPassword( System.getProperty( "javax.net.ssl.keyStorePassword" ) );
		sslConnector.setTruststore( System.getProperty( "javax.net.ssl.trustStore" ) );
		sslConnector.setTrustPassword( System.getProperty( "javax.net.ssl.trustStorePassword" ) );
		sslConnector.setPort( Integer.parseInt( System.getProperty( "loadui.https.port", "8443" ) ) );

		sslConnector.setNeedClientAuth( true );
		sslServer.addConnector( sslConnector );

		ServletContextHandler sslContext = new ServletContextHandler( sslServer, "/" );
		sslContext.setAttribute( BundleContext.class.getName(), bc );
		sslContext.addServlet( new ServletHolder( new ProxyServlet() ), "/*" );

		ContinuationCometdServlet cometd = new ContinuationCometdServlet();

		ServletHolder cometd_holder = new ServletHolder( cometd );
		cometd_holder.setInitParameter( "timeout", "10000" );
		cometd_holder.setInitParameter( "interval", "100" );
		cometd_holder.setInitParameter( "maxInterval", "10000" );
		cometd_holder.setInitParameter( "multiFrameInterval", "2000" );
		cometd_holder.setInitParameter( "logLevel", "0" );

		sslContext.addServlet( cometd_holder, "/cometd/*" );
		sslContext.addServlet( new ServletHolder( new StatusServlet() ), "/status" );

		server.start();
		sslServer.start();

		bc.registerService( MessageEndpointProvider.class.getName(), new BayeuxServiceMessagingProvider( cometd
				.getBayeux() ), null );
	}

	@Override
	public void stop( BundleContext context ) throws Exception
	{
		server.stop();
		sslServer.stop();
	}
}
