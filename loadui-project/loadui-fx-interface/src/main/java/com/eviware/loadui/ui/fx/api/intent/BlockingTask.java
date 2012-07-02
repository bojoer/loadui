package com.eviware.loadui.ui.fx.api.intent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.SceneBuilder;
import javafx.scene.effect.Effect;
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

	public static void install( Node node )
	{
		BEHAVIOR.install( node );
	}

	public static void uninstall( Node node )
	{
		BEHAVIOR.uninstall( node );
	}

	private static class BlockingTaskBehavior
	{
		private final LoadingCache<Node, EventHandler<IntentEvent<? extends Runnable>>> handlers = CacheBuilder
				.newBuilder().weakKeys().build( new CacheLoader<Node, EventHandler<IntentEvent<? extends Runnable>>>()
				{
					@Override
					public EventHandler<IntentEvent<? extends Runnable>> load( final Node key ) throws Exception
					{
						return new EventHandler<IntentEvent<? extends Runnable>>()
						{
							@Override
							public void handle( IntentEvent<? extends Runnable> event )
							{
								Runnable runnable = event.getArg();

								final Stage dialog = StageBuilder.create().style( StageStyle.UNDECORATED )
										.scene( SceneBuilder.create().root( new TaskProgressIndicator( runnable ) ).build() )
										.build();
								dialog.initModality( Modality.APPLICATION_MODAL );
								final Effect oldEffect = key.getEffect();
								GaussianBlur blur = GaussianBlurBuilder.create().radius( 0 ).build();
								key.setEffect( blur );
								new Timeline( new KeyFrame( new Duration( 500 ), new KeyValue( blur.radiusProperty(), 10,
										Interpolator.EASE_IN ) ) ).playFromStart();
								final Window window = key.getScene().getWindow();
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
												key.setEffect( oldEffect );
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

		private void install( Node node )
		{
			node.addEventHandler( IntentEvent.INTENT_RUN_BLOCKING, handlers.getUnchecked( node ) );
		}

		private void uninstall( Node node )
		{
			node.removeEventHandler( IntentEvent.INTENT_RUN_BLOCKING, handlers.getIfPresent( node ) );
		}
	}
}
