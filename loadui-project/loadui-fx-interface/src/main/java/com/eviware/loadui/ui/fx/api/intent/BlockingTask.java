package com.eviware.loadui.ui.fx.api.intent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.GaussianBlurBuilder;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import com.eviware.loadui.ui.fx.control.TaskProgressIndicator;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class BlockingTask
{
	private static final BlockingTaskBehavior BEHAVIOR = new BlockingTaskBehavior();

	public static void install( Scene scene )
	{
		BEHAVIOR.install( scene );
	}

	public static void uninstall( Scene scene )
	{
		BEHAVIOR.uninstall( scene );
	}

	private static class BlockingTaskBehavior
	{
		private final GaussianBlur blur = GaussianBlurBuilder.create().radius( 0 ).build();

		private final LoadingCache<Scene, EventHandler<IntentEvent<? extends Runnable>>> handlers = CacheBuilder
				.newBuilder().weakKeys().build( new CacheLoader<Scene, EventHandler<IntentEvent<? extends Runnable>>>()
				{
					@Override
					public EventHandler<IntentEvent<? extends Runnable>> load( final Scene scene ) throws Exception
					{
						return new EventHandler<IntentEvent<? extends Runnable>>()
						{

							@Override
							public void handle( IntentEvent<? extends Runnable> event )
							{
								final Parent root = scene.getRoot();

								Runnable runnable = event.getArg();

								final TaskProgressIndicator taskProgressIndicator = new TaskProgressIndicator( runnable );
								final Stage dialog = StageBuilder.create().style( StageStyle.UNDECORATED )
										.scene( SceneBuilder.create().root( taskProgressIndicator ).build() ).build();
								dialog.initModality( Modality.APPLICATION_MODAL );
								root.setEffect( blur );
								new Timeline( new KeyFrame( new Duration( 500 ), new KeyValue( blur.radiusProperty(), 10,
										Interpolator.EASE_IN ) ) ).playFromStart();
								final Window window = scene.getWindow();
								dialog.initOwner( window );
								dialog.show();
								dialog.setX( window.getX() + ( window.getWidth() - dialog.getWidth() ) / 2 );
								dialog.setY( window.getY() + ( window.getHeight() - dialog.getHeight() ) / 2 );

								executor.submit( runnable );
								executor.submit( new Runnable()
								{
									@Override
									public void run()
									{
										Platform.runLater( new Runnable()
										{
											@Override
											public void run()
											{
												dialog.hide();
												taskProgressIndicator.dispose();
												if( root.getEffect() == blur )
												{
													root.setEffect( null );
												}
											}
										} );
									}
								} );

								event.consume();
							}
						};
					}
				} );

		private final ExecutorService executor;

		private BlockingTaskBehavior()
		{
			executor = Executors.newSingleThreadExecutor( new ThreadFactoryBuilder().setDaemon( true )
					.setNameFormat( "BlockingTask runner" ).build() );
		}

		private void install( Scene node )
		{
			node.addEventHandler( IntentEvent.INTENT_RUN_BLOCKING, handlers.getUnchecked( node ) );
		}

		private void uninstall( Scene node )
		{
			node.removeEventHandler( IntentEvent.INTENT_RUN_BLOCKING, handlers.getIfPresent( node ) );
		}
	}
}
