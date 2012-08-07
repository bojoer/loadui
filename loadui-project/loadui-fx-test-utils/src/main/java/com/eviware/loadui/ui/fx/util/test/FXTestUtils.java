package com.eviware.loadui.ui.fx.util.test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import com.google.common.util.concurrent.SettableFuture;

public class FXTestUtils
{
	public static void bringToFront( final Stage stage ) throws Exception
	{
		invokeAndWait( new Runnable()
		{
			@Override
			public void run()
			{
				stage.setIconified( true );
				stage.setIconified( false );
				stage.toBack();
				stage.toFront();
			}
		}, 5 );
		Thread.sleep( 250 );
	}

	/**
	 * Attempts to wait for events in the JavaFX event thread to complete, as
	 * well as any new events triggered by them.
	 */
	public static void awaitEvents()
	{
		try
		{
			for( int i = 0; i < 10; i++ )
			{
				final Semaphore sem = new Semaphore( 0 );
				Platform.runLater( new Runnable()
				{
					@Override
					public void run()
					{
						sem.release();
					}
				} );

				sem.acquire();
			}
		}
		catch( Throwable e )
		{
			throw new RuntimeException( e );
		}
	}

	/**
	 * Runs the given Callable in the JavaFX thread, waiting for it to complete
	 * before returning. Also attempts to wait for any other JavaFX events that
	 * may have been queued in the Callable to complete. If any Exception is
	 * thrown during execution of the Callable, that exception will be re-thrown
	 * from invokeAndWait.
	 * 
	 * @param task
	 * @param timeoutInSeconds
	 * @throws Throwable
	 */
	public static void invokeAndWait( final Callable<?> task, int timeoutInSeconds ) throws Exception
	{
		final SettableFuture<Void> future = SettableFuture.create();

		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					task.call();
					future.set( null );
				}
				catch( Throwable e )
				{
					future.setException( e );
				}
			}
		} );

		try
		{
			future.get( timeoutInSeconds, TimeUnit.SECONDS );
			awaitEvents();
		}
		catch( ExecutionException e )
		{
			if( e.getCause() instanceof Exception )
			{
				throw ( Exception )e.getCause();
			}
			else
			{
				throw e;
			}
		}
	}

	/**
	 * @see invokeAndWait(Runnable, int)
	 * 
	 * @param task
	 * @param timeoutInSeconds
	 * @throws Throwable
	 */
	public static void invokeAndWait( final Runnable task, int timeoutInSeconds ) throws Exception
	{
		invokeAndWait( new Callable<Void>()
		{
			@Override
			public Void call() throws Exception
			{
				task.run();

				return null;
			}
		}, timeoutInSeconds );
	}

	/**
	 * Launches a JavaFX App in a new Thread.
	 * 
	 * @param appClass
	 * @param args
	 */
	public static void launchApp( final Class<? extends Application> appClass, final String... args )
	{
		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				Application.launch( appClass, args );
			}
		} ).start();
	}
}
