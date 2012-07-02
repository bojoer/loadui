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
package com.eviware.loadui.impl.conversion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.convert.converter.Converter;

import com.eviware.loadui.api.messaging.BroadcastMessageEndpoint;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.impl.property.Reference;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Closeables;

public class FileToReferenceConverter implements Converter<File, Reference>
{
	public final static String CHANNEL = "/" + FileToReferenceConverter.class.getName();

	public final static String START = "start";
	public final static String STOP = "stop";

	// private final File storage = new File( System.getProperty(
	// LoadUI.LOADUI_HOME )
	// + File.separator + "fileStorage" );
	private final ExecutorService executionService;
	private final Map<String, FileStruct> cache = new HashMap<>();
	private final Map<String, File> lookupTable = new HashMap<>();

	public FileToReferenceConverter( BroadcastMessageEndpoint endpoint, ExecutorService executionService )
	{
		this.executionService = executionService;
		endpoint.addMessageListener( CHANNEL, new Listener() );
	}

	@Override
	public Reference convert( File source )
	{
		return new Reference(
				( source.exists() && source.isFile() ) ? getHash( source ) : ":" + source.getAbsolutePath(), null );
	}

	private synchronized String getHash( File file )
	{
		String key = file.getAbsolutePath();
		if( !cache.containsKey( key ) || cache.get( key ).modified != file.lastModified() )
		{
			try
			{
				String hash = DigestUtils.md5Hex( new FileInputStream( file ) );
				cache.put( key, new FileStruct( hash, file.lastModified() ) );
				lookupTable.put( hash, file );
			}
			catch( IOException e )
			{
				throw new RuntimeException( "Error calculating hash of file [" + file.getName() + "].", e );
			}
		}
		return cache.get( key ).hash;
	}

	private static class FileStruct
	{
		public String hash;
		public long modified;

		FileStruct( String hash, long modified )
		{
			this.hash = hash;
			this.modified = modified;
		}
	}

	private class Listener implements MessageListener
	{
		@Override
		public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
		{
			if( lookupTable.containsKey( data ) )
				executionService.execute( new FileSender( ( String )data, lookupTable.get( data ), endpoint ) );
		}
	}

	private static class FileSender implements Runnable
	{
		private final String hash;
		private final File file;
		private final MessageEndpoint endpoint;

		public FileSender( String hash, File file, MessageEndpoint endpoint )
		{
			this.hash = hash;
			this.file = file;
			this.endpoint = endpoint;
		}

		@Override
		public void run()
		{
			FileInputStream fis = null;
			try
			{
				fis = new FileInputStream( file );
				byte[] buf = new byte[1024];
				byte[] res;
				try
				{
					endpoint.sendMessage( ReferenceToFileConverter.CHANNEL, Collections.singletonMap( hash, START ) );
					int len = -1;
					while( ( len = fis.read( buf ) ) >= 0 )
					{
						res = new byte[len];
						System.arraycopy( buf, 0, res, 0, len );
						endpoint.sendMessage( ReferenceToFileConverter.CHANNEL,
								ImmutableMap.<String, String> of( hash, Base64.encodeBase64String( res ) ) );
					}
					endpoint.sendMessage( ReferenceToFileConverter.CHANNEL, Collections.singletonMap( hash, STOP ) );
				}
				catch( IOException e )
				{
					throw new RuntimeException( e );
				}
			}
			catch( FileNotFoundException e )
			{
				throw new RuntimeException( e );
			}
			finally
			{
				Closeables.closeQuietly( fis );
			}
		}
	}
}
