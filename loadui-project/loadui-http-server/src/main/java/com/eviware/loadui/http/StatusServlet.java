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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Used to verify that a loadUI Runner is currently running. Contains a Server
 * header string identifying the HTTP server as a loadUI Runner and the time
 * that it was started.
 * 
 * @author dain.nilsson
 * 
 */
public class StatusServlet extends HttpServlet
{
	private static final long serialVersionUID = 4671397544102142652L;

	private final String startTime;

	public StatusServlet()
	{
		DateFormat rfc1123Format = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss z", Locale.US );
		rfc1123Format.setCalendar( Calendar.getInstance( new SimpleTimeZone( 0, "GMT" ) ) );
		startTime = rfc1123Format.format( new Date() );
	}

	@Override
	protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException
	{
		resp.setHeader( "Server", "LoadUI Agent;Start-Time=" + startTime );
		resp.setDateHeader( "Date", System.currentTimeMillis() );
	}
}
