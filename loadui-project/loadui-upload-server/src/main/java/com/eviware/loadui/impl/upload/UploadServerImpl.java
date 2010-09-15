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
package com.eviware.loadui.impl.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import com.eviware.loadui.api.upload.FileFormField;
import com.eviware.loadui.api.upload.FormField;
import com.eviware.loadui.api.upload.UploadHandler;
import com.eviware.loadui.api.upload.UploadServer;

public class UploadServerImpl extends HttpServlet implements UploadServer
{
	private static final long serialVersionUID = 8950749934408724005L;

	private final HttpService httpService;
	private final Map<String, UploadHandler> handlers = new HashMap<String, UploadHandler>();

	public UploadServerImpl( HttpService httpService )
	{
		this.httpService = httpService;
	}

	@Override
	public void registerUploadEndpoint( String alias, UploadHandler handler )
	{
		if( handlers.containsKey( alias ) )
			throw new RuntimeException( "Cannot bind to alias '" + alias + "', already bound!" );

		try
		{
			handlers.put( alias, handler );
			httpService.registerServlet( alias, this, null, null );
		}
		catch( ServletException e )
		{
			e.printStackTrace();
		}
		catch( NamespaceException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public void unregisterUploadEndpoint( String alias )
	{
		if( handlers.remove( alias ) != null )
			httpService.unregister( alias );
	}

	@Override
	protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
			IOException
	{
		if( !ServletFileUpload.isMultipartContent( request ) )
			response.sendError( 400 );

		ServletFileUpload upload = new ServletFileUpload();

		Collection<FormField> fields = new ArrayList<FormField>();
		try
		{
			FileItemIterator iter = upload.getItemIterator( request );
			while( iter.hasNext() )
			{
				FileItemStream item = iter.next();

				if( !item.isFormField() )
				{
					InputStream stream = item.openStream();
					File tmp = File.createTempFile( "upload", "tmp" );
					FileOutputStream fos = new FileOutputStream( tmp );
					byte[] buf = new byte[1024];
					int n = -1;
					while( ( n = stream.read( buf ) ) != -1 )
						fos.write( buf, 0, n );
					fos.close();
					fields.add( new FileFormField( item.getFieldName(), tmp, item.getName() ) );
				}
				else if( item.isFormField() )
				{
					fields.add( new FormField( item.getFieldName(), Streams.asString( item.openStream() ) ) );
				}
			}

			System.out.println( request.getRequestURI() );
			System.out.println( request.getRequestURL() );
			handlers.get( request.getRequestURI() ).handleUpload( fields );
		}
		catch( FileUploadException e )
		{
			e.printStackTrace();
			response.sendError( 500 );
		}
	}
}
