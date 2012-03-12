package com.eviware.loadui.impl.eventlog;

import java.nio.ByteBuffer;

import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.testevents.AbstractTestEvent;

public class DemoEvent extends AbstractTestEvent
{
	private final byte[] data;

	public DemoEvent( long timestamp, byte[] entryData )
	{
		super( timestamp );

		data = entryData;
	}

	@Override
	public String toString()
	{
		return "This is demo event nr: " + ByteBuffer.wrap( data ).getLong();
	}

	byte[] getData()
	{
		return data;
	}

	public static class Factory extends AbstractTestEvent.Factory<DemoEvent> implements TestEvent.Source<DemoEvent>
	{
		public Factory()
		{
			super( DemoEvent.class );

			new Thread( new Runnable()
			{
				@Override
				public void run()
				{
					TestEventManager manager = BeanInjector.getBean( TestEventManager.class );
					long count = 1;

					while( true )
					{
						try
						{
							Thread.sleep( ( long )( Math.random() * 1000 + 1000 ) );
						}
						catch( InterruptedException e )
						{
							e.printStackTrace();
						}
						byte[] data = new byte[8];
						ByteBuffer.wrap( data ).putLong( count++ );
						manager.logTestEvent( Factory.this, new DemoEvent( System.currentTimeMillis(), data ) );
					}
				}
			} ).start();
		}

		@Override
		public DemoEvent createTestEvent( long timestamp, byte[] sourceData, byte[] entryData )
		{
			return new DemoEvent( timestamp, entryData );
		}

		@Override
		public byte[] getDataForTestEvent( DemoEvent testEvent )
		{
			return testEvent.getData();
		}

		@Override
		public byte[] getData()
		{
			return new byte[0];
		}

		@Override
		public String getHash()
		{
			return "demoEventFactory";
		}
	}
}