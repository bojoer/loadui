package com.eviware.loadui.impl.conversion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.model.PropertyHolder;
import com.eviware.loadui.impl.property.Reference;

public class ReferenceToFileConverter implements Converter<Reference, File>, EventHandler<CollectionEvent>
{
	public final static Logger log = LoggerFactory.getLogger( ReferenceToFileConverter.class );

	public final static String CHANNEL = "/" + ReferenceToFileConverter.class.getName();

	private final File storage = new File( System.getProperty( "loadui.home" ) + File.separator + "fileStorage" );
	private final FilePropertyListener filePropertyListener = new FilePropertyListener();
	private final Set<File> filesInUse = new HashSet<File>();

	private final Map<String, File> files = new HashMap<String, File>();
	private final Map<String, OutputStream> writers = Collections.synchronizedMap( new HashMap<String, OutputStream>() );
	private final FileReceiver listener = new FileReceiver();

	public ReferenceToFileConverter( AddressableRegistry addressableRegistry, ScheduledExecutorService executorService )
	{
		addressableRegistry.addEventListener( CollectionEvent.class, this );
		executorService.scheduleAtFixedRate( new RemoveOldFilesTask(), 5, 5, TimeUnit.MINUTES );

		if( !storage.isDirectory() )
			storage.mkdirs();
	}

	@Override
	public File convert( Reference source )
	{
		if( source.getId().startsWith( ":" ) )
			return new File( source.getId().substring( 1 ) );

		File target = getOrCreate( source );
		synchronized( target )
		{
			while( !target.exists() )
			{
				try
				{
					target.wait();
				}
				catch( InterruptedException e )
				{
				}
			}
		}

		return target;
	}

	private File getOrCreate( Reference source )
	{
		String hash = source.getId();
		synchronized( files )
		{
			if( !files.containsKey( hash ) )
			{
				files.put( hash, new File( storage, hash ) );
				source.getEndpoint().addMessageListener( CHANNEL, listener );
				source.getEndpoint().sendMessage( FileToReferenceConverter.CHANNEL, hash );
			}
			return files.get( hash );
		}
	}

	@Override
	public void handleEvent( CollectionEvent event )
	{
		Object element = event.getElement();
		if( element instanceof PropertyHolder )
		{
			PropertyHolder propertyHolder = ( PropertyHolder )element;
			if( event.getEvent() == CollectionEvent.Event.ADDED )
				propertyHolder.addEventListener( PropertyEvent.class, filePropertyListener );
			else
				propertyHolder.removeEventListener( PropertyEvent.class, filePropertyListener );
		}
	}

	private class FileReceiver implements MessageListener
	{
		@Override
		@SuppressWarnings( "unchecked" )
		public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
		{
			Map<String, Object> map = ( Map<String, Object> )data;
			for( Entry<String, Object> entry : map.entrySet() )
			{
				String hash = entry.getKey();
				synchronized( files )
				{
					if( files.containsKey( hash ) )
					{
						File file = files.get( hash );
						if( FileToReferenceConverter.START.equals( entry.getValue() ) )
						{
							try
							{
								file.createNewFile();
								writers.put( hash, new FileOutputStream( file ) );
							}
							catch( FileNotFoundException e )
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							//
							catch( IOException e )
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else if( FileToReferenceConverter.STOP.equals( entry.getValue() ) )
						{
							try
							{
								writers.remove( hash ).close();
							}
							catch( IOException e )
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							synchronized( file )
							{
								try
								{
									FileInputStream fis = new FileInputStream( file );
									String md5Hex = DigestUtils.md5Hex( fis );
									fis.close();
									if( hash.equals( md5Hex ) )
									{
										file.notifyAll();
									}
									else
									{
										log.error( "File transfered with MD5 hash: {}, should be {}. Retrying...", md5Hex, hash );
										file.delete();
										endpoint.sendMessage( FileToReferenceConverter.CHANNEL, hash );
									}
								}
								catch( FileNotFoundException e )
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
						}
						else if( writers.containsKey( hash ) )
						{
							try
							{
								writers.get( hash ).write( Base64.decodeBase64( ( String )entry.getValue() ) );
							}
							catch( IOException e )
							{
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	private class FilePropertyListener implements EventHandler<PropertyEvent>
	{
		@Override
		public void handleEvent( PropertyEvent event )
		{
			if( File.class.isAssignableFrom( event.getProperty().getType() ) )
			{
				if( event.getEvent() == PropertyEvent.Event.CREATED )
					filesInUse.add( ( File )event.getProperty().getValue() );
				else if( event.getEvent() == PropertyEvent.Event.VALUE )
				{
					filesInUse.remove( event.getPreviousValue() );
					filesInUse.add( ( File )event.getProperty().getValue() );
				}
				else if( event.getEvent() == PropertyEvent.Event.DELETED )
					filesInUse.remove( event.getPreviousValue() );
			}
		}
	}

	private class RemoveOldFilesTask implements Runnable
	{
		// max storage size in MB
		public static final int MAX_STORAGE_SIZE = 100;

		private Comparator<File> compareByLastUsed = new Comparator<File>()
		{
			@Override
			public int compare( File o1, File o2 )
			{
				return ( int )( o1.lastModified() - o2.lastModified() );
			}
		};

		@Override
		public void run()
		{
			List<File> unused = new ArrayList<File>();
			for( File file : storage.listFiles() )
				if( !filesInUse.contains( file ) )
					unused.add( file );

			Collections.sort( unused, compareByLastUsed );

			long limit = MAX_STORAGE_SIZE * 1024 * 1024;
			long size = 0;
			for( int i = unused.size() - 1; i >= 0; i-- )
			{
				size += unused.get( i ).length();
				if( size > limit )
				{
					int newSize = unused.size() - i - 1;
					while( unused.size() > newSize )
					{
						unused.get( 0 ).delete();
						unused.remove( 0 );
					}
					break;
				}
			}
		}
	}
}
