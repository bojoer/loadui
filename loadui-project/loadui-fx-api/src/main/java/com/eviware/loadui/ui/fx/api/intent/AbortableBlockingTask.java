package com.eviware.loadui.ui.fx.api.intent;

import java.util.concurrent.Future;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;

import com.eviware.loadui.ui.fx.api.intent.BlockingTask.BlockingTaskBehavior;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Manager for using {@link IntentEvent#INTENT_RUN_BLOCKING_ABORTABLE} tasks.
 * @author renato
 *
 */
public class AbortableBlockingTask
{

	private static final AbortableBlockingTaskBehavior BEHAVIOR = new AbortableBlockingTaskBehavior();

	public static void install( Scene scene )
	{
		BEHAVIOR.install( scene );
	}

	public static void uninstall( Scene scene )
	{
		BEHAVIOR.uninstall( scene );
	}

	static class AbortableBlockingTaskBehavior extends BlockingTaskBehavior
	{

		private final LoadingCache<Scene, AbortableTaskHandler> handlers = CacheBuilder.newBuilder().weakKeys()
				.build( new CacheLoader<Scene, AbortableTaskHandler>()
				{
					@Override
					public AbortableTaskHandler load( final Scene scene ) throws Exception
					{
						return new AbortableTaskHandler( scene );
					}

				} );

		protected void install( Scene scene )
		{
			scene.addEventHandler( IntentEvent.INTENT_RUN_BLOCKING_ABORTABLE, handlers.getUnchecked( scene ) );
		}

		protected void uninstall( Scene scene )
		{
			scene.removeEventHandler( IntentEvent.INTENT_RUN_BLOCKING_ABORTABLE, handlers.getIfPresent( scene ) );
		}

		private class AbortableTaskHandler implements EventHandler<IntentEvent<? extends AbortableTask>>
		{
			final Scene scene;

			AbortableTaskHandler( final Scene scene )
			{
				this.scene = scene;
			}

			@Override
			public void handle( IntentEvent<? extends AbortableTask> event )
			{
				final AbortableTask task = event.getArg();
				Button abortButton = new Button( "Abort running requests" );
				abortButton.setId( "abort-requests" );
				final Future<?> future = processRunnable( scene, task.onRun, abortButton );

				abortButton.setOnAction( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent _ )
					{
						future.cancel( true );
						task.onAbort.run();
					}
				} );

				event.consume();
			}

		}

	}

}
