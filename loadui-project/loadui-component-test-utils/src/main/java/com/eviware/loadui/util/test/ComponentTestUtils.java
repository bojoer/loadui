package com.eviware.loadui.util.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.component.BehaviorProvider;
import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.TerminalConnectionEvent;
import com.eviware.loadui.api.events.TerminalEvent;
import com.eviware.loadui.api.events.TerminalMessageEvent;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.config.ComponentItemConfig;
import com.eviware.loadui.groovy.GroovyBehaviorProvider;
import com.eviware.loadui.impl.model.ComponentItemImpl;
import com.eviware.loadui.impl.terminal.ConnectionBase;
import com.eviware.loadui.impl.terminal.OutputTerminalImpl;
import com.eviware.loadui.impl.terminal.TerminalMessageImpl;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.InitializableUtils;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class ComponentTestUtils
{
	private static final Object lock = new Object();
	private static final ConcurrentMap<ComponentDescriptor, BehaviorProvider> descriptors = Maps.newConcurrentMap();
	private static final Set<ConnectionImpl> connections = Collections.synchronizedSet( new HashSet<ConnectionImpl>() );

	private static final ComponentRegistry registry = mock( ComponentRegistry.class );
	private static final ComponentItem dummyComponent = mock( ComponentItem.class );
	private static final OutputTerminal outputDummy = mock( OutputTerminal.class );
	private static final InputTerminal inputDummy = mock( InputTerminal.class );

	static
	{
		System.setProperty( LoadUI.INSTANCE, LoadUI.CONTROLLER );
		System.setProperty( "groovy.root", "target" + File.separator + ".groovy" );

		doAnswer( new Answer<Void>()
		{
			@Override
			public Void answer( InvocationOnMock invocation ) throws Throwable
			{
				ComponentDescriptor descriptor = ( ComponentDescriptor )invocation.getArguments()[0];
				BehaviorProvider provider = ( BehaviorProvider )invocation.getArguments()[1];
				synchronized( lock )
				{
					descriptors.put( descriptor, provider );
					lock.notifyAll();
				}

				return null;
			}
		} ).when( registry ).registerDescriptor( any( ComponentDescriptor.class ), any( BehaviorProvider.class ) );

		when( outputDummy.getTerminalHolder() ).thenReturn( dummyComponent );
		when( inputDummy.getTerminalHolder() ).thenReturn( dummyComponent );
	}

	public static void initialize( String pathToComponentScripts )
	{
		new GroovyBehaviorProvider( registry, Executors.newSingleThreadScheduledExecutor(), new File(
				pathToComponentScripts ) );
	}

	public static BeanInjectorMocker getDefaultBeanInjectorMocker()
	{
		return new BeanInjectorMocker().put( ConversionService.class, new DefaultConversionService() ).put(
				ExecutorService.class, Executors.newCachedThreadPool() );
	}

	@SuppressWarnings( "rawtypes" )
	public static ComponentItem createComponent( final String componentName ) throws ComponentCreationException
	{
		Optional<ComponentDescriptor> descriptorOptional = null;
		Predicate<ComponentDescriptor> predicate = new Predicate<ComponentDescriptor>()
		{
			@Override
			public boolean apply( ComponentDescriptor input )
			{
				return Objects.equal( componentName, input.getLabel() );
			}
		};

		long deadline = System.currentTimeMillis() + 5000;
		synchronized( lock )
		{
			while( !( descriptorOptional = Iterables.tryFind( descriptors.keySet(), predicate ) ).isPresent()
					&& System.currentTimeMillis() < deadline )
			{
				try
				{
					lock.wait( deadline - System.currentTimeMillis() );
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}
			}
		}

		ComponentDescriptor descriptor = descriptorOptional.get();

		ProjectItem project = mock( ProjectItem.class );
		when( project.getProject() ).thenReturn( project );
		when( project.isRunning() ).thenReturn( true );
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

		ComponentItemImpl component = InitializableUtils.initialize( new ComponentItemImpl( project,
				ComponentItemConfig.Factory.newInstance() ) );
		component.setAttribute( ComponentItem.TYPE, descriptor.getLabel() );
		component.setBehavior( descriptors.get( descriptor ).createBehavior( descriptor, component.getContext() ) );

		return component;
	}

	public static void sendMessage( InputTerminal terminal, Map<String, ?> message )
	{
		ComponentItem component = ( ComponentItem )terminal.getTerminalHolder();
		TerminalMessageImpl terminalMessage = new TerminalMessageImpl( BeanInjector.getBean( ConversionService.class ) );
		terminalMessage.putAll( message );
		component.handleTerminalEvent( terminal, new TerminalMessageEvent( outputDummy, terminalMessage ) );
	}

	public static BlockingQueue<TerminalMessage> getMessagesFrom( OutputTerminal terminal )
	{
		LinkedBlockingQueue<TerminalMessage> queue = new LinkedBlockingQueue<TerminalMessage>();
		terminal.addEventListener( TerminalMessageEvent.class, new MessageListener( queue ) );

		return queue;
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

	private static Connection connect( OutputTerminal output, InputTerminal input )
	{
		ConnectionImpl connection = new ConnectionImpl( output, input );
		connections.add( connection );
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
		return connection;
	}

	public static class ConnectionImpl extends ConnectionBase implements EventHandler<TerminalEvent>
	{
		private ConnectionImpl( OutputTerminal output, InputTerminal input )
		{
			super( output, input );

			if( output instanceof OutputTerminalImpl )
				( ( OutputTerminalImpl )output ).fireEvent( new TerminalConnectionEvent( this, output, input,
						TerminalConnectionEvent.Event.CONNECT ) );
		}

		@Override
		public void disconnect()
		{
			connections.remove( this );
			if( getOutputTerminal() instanceof OutputTerminalImpl )
				( ( OutputTerminalImpl )getOutputTerminal() ).fireEvent( new TerminalConnectionEvent( this,
						getOutputTerminal(), getInputTerminal(), TerminalConnectionEvent.Event.DISCONNECT ) );
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

		@Override
		public void handleEvent( TerminalEvent event )
		{
			getInputTerminal().getTerminalHolder().handleTerminalEvent( getInputTerminal(), event );
		}
	}
}
