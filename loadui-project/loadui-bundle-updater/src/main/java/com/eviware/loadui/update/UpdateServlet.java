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
package com.eviware.loadui.update;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.springframework.osgi.context.BundleContextAware;

public class UpdateServlet extends HttpServlet implements BundleContextAware
{
	private static final long serialVersionUID = -206213321500501556L;
	private BundleContext context;

	public UpdateServlet( HttpService http )
	{
		try
		{
			http.registerServlet( "/update", this, null, null );
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
	public void setBundleContext( BundleContext bundleContext )
	{
		context = bundleContext;
	}

	@Override
	public void doGet( HttpServletRequest request, HttpServletResponse response )
	{
		try
		{
			PrintWriter out = response.getWriter();
			StringBuilder html = new StringBuilder() //
					.append( "<html><head><title>loadUI</title></head><body>" ) //
					.append( "<form enctype=\"multipart/form-data\" action=\"/update\" method=\"post\">" ) //
					.append( "<input type=\"file\" name=\"bundle\" /><input type=\"submit\">" ) //
					.append( "</form>" ) //
					.append( "<form enctype=\"multipart/form-data\" action=\"/update\" method=\"post\">" ) //
					.append( "<input type=\"text\" name=\"delete\" /><input type=\"submit\">" ) //
					.append( "</form>" ) //
					.append( "</body></html>" );
			out.println( html );
			out.close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public void doPost( HttpServletRequest request, HttpServletResponse response )
	{
		try
		{
			int status = 200;

			if( !ServletFileUpload.isMultipartContent( request ) )
				response.sendError( 400 );

			ServletFileUpload upload = new ServletFileUpload();

			FileItemIterator iter = upload.getItemIterator( request );
			while( iter.hasNext() )
			{
				FileItemStream item = iter.next();

				if( !item.isFormField() && "bundle".equals( item.getFieldName() ) )
				{
					status = installBundle( item );
				}
				else if( item.isFormField() && "delete".equals( item.getFieldName() ) )
				{
					status = uninstallBundle( Streams.asString( item.openStream() ) );
				}
				else
					status = 400;

				if( status >= 400 )
					break;
			}
			response.setStatus( status );
		}
		catch( FileUploadException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private int uninstallBundle( String filename )
	{
		File parent = new File( "bundle" );
		File target = new File( parent, filename );
		if( target.getParentFile().equals( parent ) && target.exists() )
		{
			System.out.println( "Uninstalling bundle: " + filename + "..." );
			try
			{
				Bundle bundle = context.installBundle( target.toURI().toString() );
				bundle.uninstall();
				File backup = new File( target.getPath() + ".bak" );
				if( backup.exists() )
					backup.delete();
				target.delete();
				System.out.println( "Bundle uninstalled." );
			}
			catch( BundleException e )
			{
				return 500;
			}
		}
		else
			return 400;

		return 200;
	}

	private int installBundle( FileItemStream item )
	{
		try
		{
			InputStream stream = item.openStream();
			File target = new File( "bundle" + File.separator + item.getName() );
			if( !target.getParentFile().isDirectory() )
				return 400;

			File tmp = File.createTempFile( "upload", "tmp" );
			FileOutputStream fos = new FileOutputStream( tmp );
			byte[] buf = new byte[1024];
			int n = -1;
			while( ( n = stream.read( buf ) ) != -1 )
				fos.write( buf, 0, n );
			fos.close();

			File backup = new File( target.getPath() + ".bak" );
			if( target.exists() )
			{
				System.out.println( "Creating backup of old file." );
				if( backup.exists() )
					backup.delete();
				if( !target.renameTo( backup ) )
					System.out.println( "Unable to rename target!" );
			}
			else
			{
				if( backup.exists() )
					backup.delete();
			}
			if( tmp.renameTo( target ) )
			{
				System.out.println( "Installed bundle: " + target.getName() );
				try
				{
					System.out.println( "Starting bundle: " + target.getName() );
					Bundle bundle = context.installBundle( target.toURI().toString() );
					if( backup.exists() )
					{
						bundle.update();
					}
					bundle.start();
				}
				catch( BundleException e )
				{
					e.printStackTrace();
					target.delete();
					if( backup.exists() )
					{
						System.out.println( "Unable to start bundle, restoring backup..." );
						backup.renameTo( target );
					}
					return 500;
				}
			}
			else
			{
				System.out.println( "Target: " + target.getAbsolutePath() );
				if( backup.renameTo( target ) )
					System.out.println( "Unable to restore backup!" );
			}
		}
		catch( FileNotFoundException e )
		{
			return 500;
		}
		catch( IOException e )
		{
			return 500;
		}

		return 200;
	}
}
