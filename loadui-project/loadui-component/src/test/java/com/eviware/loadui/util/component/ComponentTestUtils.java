package com.eviware.loadui.util.component;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.component.ComponentBehavior;
import com.eviware.loadui.api.counter.Counter;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.TerminalConnectionEvent;
import com.eviware.loadui.api.events.TerminalMessageEvent;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.config.ComponentItemConfig;
import com.eviware.loadui.impl.addressable.AddressableRegistryImpl;
import com.eviware.loadui.impl.counter.CounterSupport;
import com.eviware.loadui.impl.model.ComponentItemImpl;
import com.eviware.loadui.impl.terminal.ConnectionBase;
import com.eviware.loadui.impl.terminal.OutputTerminalImpl;
import com.eviware.loadui.impl.terminal.TerminalMessageImpl;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.test.BeanInjectorMocker;
import com.eviware.loadui.util.test.TestUtils;

public class ComponentTestUtils
{
	private static final ComponentItem dummyComponent = mock( ComponentItem.class );
	private static final OutputTerminal outputDummy = mock( OutputTerminal.class );
	private static final Set<ConnectionImpl> connections = Collections.synchronizedSet( new HashSet<ConnectionImpl>() );

	static
	{
		System.setProperty( LoadUI.INSTANCE, LoadUI.CONTROLLER );

		when( outputDummy.getTerminalHolder() ).thenReturn( dummyComponent );
	}

	public static BeanInjectorMocker getDefaultBeanInjectorMocker()
	{
		return new BeanInjectorMocker().put( ConversionService.class, new DefaultConversionService() )
				.put( ExecutorService.class, Executors.newCachedThreadPool() )
				.put( ScheduledExecutorService.class, Executors.newSingleThreadScheduledExecutor() )
				.put( AddressableRegistry.class, new AddressableRegistryImpl() );
	}

	@SuppressWarnings( "rawtypes" )
	public static ComponentItem createComponentItem()
	{
		WorkspaceItem workspace = mock( WorkspaceItem.class );
		ProjectItem project = mock( ProjectItem.class );
		when( project.getWorkspace() ).thenReturn( workspace );
		when( project.getProject() ).thenReturn( project );
		when( project.isRunning() ).thenReturn( true );
		final CounterSupport counterSupport = new CounterSupport();
		counterSupport.init( project );
		when( project.getCounter( anyString() ) ).thenAnswer( new Answer<Counter>()
		{
			@Override
			public Counter answer( InvocationOnMock invocation ) throws Throwable
			{
				return counterSupport.getCounter( ( String )invocation.getArguments()[0] );
			}
		} );
		when( project.connect( any( OutputTerminal.class ), any( InputTerminal.class ) ) ).thenAnswer(
				new Answer<Connection>()
				{
					@Override
					public Connection answer( InvocationOnMock invocation ) throws Throwable
					{
						OutputTerminal output = ( OutputTerminal )invocation.getArguments()[0];
						InputTerminal input = ( InputTerminal )invocation.getArguments()[1];

						return connect( output, input );
					}
				} );
		when( ( Collection )project.getConnections() ).thenReturn( connections );

		ComponentItemImpl component = ComponentItemImpl.newInstance( project, ComponentItemConfig.Factory.newInstance() );
		return component;
	}

	public static void setComponentBehavior( ComponentItem component, ComponentBehavior behavior )
	{
		assert component instanceof ComponentItemImpl;
		( ( ComponentItemImpl )component ).setBehavior( behavior );
	}

	public static void sendMessage( InputTerminal terminal, Map<String, ?> message )
	{
		ComponentItem component = ( ComponentItem )terminal.getTerminalHolder();
		TerminalMessageImpl terminalMessage = new TerminalMessageImpl( BeanInjector.getBean( ConversionService.class ) );
		terminalMessage.putAll( message );
		component.handleTerminalEvent( terminal, new TerminalMessageEvent( outputDummy, terminalMessage ) );

		if( component.getClass().getSimpleName().contains( "Mockito" ) )
		{
			return;
		}

		try
		{
			TestUtils.awaitEvents( component );
		}
		catch( InterruptedException e )
		{
			e.printStackTrace();
		}
		catch( ExecutionException e )
		{
			e.printStackTrace();
		}
		catch( TimeoutException e )
		{
			e.printStackTrace();
		}
	}

	public static BlockingQueue<TerminalMessage> getMessagesFrom( OutputTerminal terminal )
	{
		LinkedBlockingQueue<TerminalMessage> queue = new LinkedBlockingQueue<TerminalMessage>();
		terminal.addEventListener( TerminalMessageEvent.class, new MessageListener( queue ) );

		return queue;
	}

	private static Connection connect( OutputTerminal output, InputTerminal input )
	{
		ConnectionImpl connection = new ConnectionImpl( output, input );
		connections.add( connection );

		if( output instanceof OutputTerminalImpl )
		{
			try
			{
				TestUtils.awaitEvents( output );
			}
			catch( InterruptedException e )
			{
				e.printStackTrace();
			}
			catch( ExecutionException e )
			{
				e.printStackTrace();
			}
			catch( TimeoutException e )
			{
				e.printStackTrace();
			}
		}

		return connection;
	}

	private static class MessageListener implements EventHandler<TerminalMessageEvent>
	{
		private final BlockingQueue<TerminalMessage> queue;

		private MessageListener( BlockingQueue<TerminalMessage> queue )
		{
			this.queue = queue;
		}

		@Override
		public void handleEvent( TerminalMessageEvent event )
		{
			queue.add( event.getMessage() );
		}
	}

	private static class ConnectionImpl extends ConnectionBase implements EventHandler<TerminalConnectionEvent>
	{
		private ConnectionImpl( OutputTerminal output, InputTerminal input )
		{
			super( output, input );

			if( output instanceof OutputTerminalImpl )
			{
				output.addEventListener( TerminalConnectionEvent.class, this );
				output.fireEvent( new TerminalConnectionEvent( this, output, input, TerminalConnectionEvent.Event.CONNECT ) );
			}
		}

		@Override
		public void disconnect()
		{
			connections.remove( this );
			if( getOutputTerminal() instanceof OutputTerminalImpl )
			{
				getOutputTerminal().fireEvent(
						new TerminalConnectionEvent( this, getOutputTerminal(), getInputTerminal(),
								TerminalConnectionEvent.Event.DISCONNECT ) );
				try
				{
					TestUtils.awaitEvents( getOutputTerminal() );
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
				}
				catch( ExecutionException e )
				{
					e.printStackTrace();
				}
				catch( TimeoutException e )
				{
					e.printStackTrace();
				}
			}
		}

		@Override
		public void handleEvent( TerminalConnectionEvent event )
		{
			if( getInputTerminal().getTerminalHolder() != null )
			{
				getInputTerminal().getTerminalHolder().handleTerminalEvent( getInputTerminal(), event );
			}
		}
	}
}