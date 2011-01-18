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
import org.cometd.server.CometdServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.messaging.ServerEndpoint;
import com.eviware.loadui.impl.messaging.BayeuxServiceServerEndpoint;

public class HttpServerActivator implements BundleActivator
{
	private Server sslServer;
	private Server server;

	@Override
	public void start( BundleContext bc ) throws Exception
	{
		server = new Server();

		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort( Integer.parseInt( System.getProperty( LoadUI.HTTP_PORT, "8080" ) ) );
		server.addConnector( connector );

		ServletContextHandler context = new ServletContextHandler( server, "/" );
		context.addServlet( new ServletHolder( new StatusServlet() ), "/*" );

		sslServer = new Server();

		SslSelectChannelConnector sslConnector = new SslSelectChannelConnector();
		sslConnector.setKeystore( System.getProperty( LoadUI.KEY_STORE ) );
		sslConnector.setKeyPassword( System.getProperty( LoadUI.KEY_STORE_PASSWORD ) );
		sslConnector.setTruststore( System.getProperty( LoadUI.TRUST_STORE ) );
		sslConnector.setTrustPassword( System.getProperty( LoadUI.TRUST_STORE_PASSWORD ) );
		sslConnector.setPort( Integer.parseInt( System.getProperty( LoadUI.HTTPS_PORT, "8443" ) ) );

		sslConnector.setNeedClientAuth( true );
		sslServer.addConnector( sslConnector );

		ServletContextHandler sslContext = new ServletContextHandler( sslServer, "/" );
		sslContext.setAttribute( BundleContext.class.getName(), bc );
		sslContext.addServlet( new ServletHolder( new ProxyServlet() ), "/*" );

		CometdServlet cometd = new CometdServlet();

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

		bc.registerService( ServerEndpoint.class.getName(), new BayeuxServiceServerEndpoint( cometd.getBayeux() ), null );
	}

	@Override
	public void stop( BundleContext context ) throws Exception
	{
		server.stop();
		sslServer.stop();
	}
}
