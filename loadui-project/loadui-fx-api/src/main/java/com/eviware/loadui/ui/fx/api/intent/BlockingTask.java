/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.ui.fx.api.intent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.GaussianBlurBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import com.eviware.loadui.ui.fx.api.LoaduiFXConstants;
import com.eviware.loadui.ui.fx.api.control.TaskProgressIndicator;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ObjectArrays;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class BlockingTask
{
	private static final String OTHER_STAGES = "OTHER_STAGES";
	private static final BlockingTaskBehavior BEHAVIOR = new BlockingTaskBehavior();
	private static final List<Stage> stages = new ArrayList<>();

	public static void install( Scene scene )
	{
		BEHAVIOR.install( scene );
		scene.getRoot().getProperties().put( OTHER_STAGES, stages );
	}

	public static void uninstall( Scene scene )
	{
		BEHAVIOR.uninstall( scene );
		scene.getRoot().getProperties().remove( OTHER_STAGES );
	}

	protected static class BlockingTaskBehavior
	{

		protected final ExecutorService executor = Executors.newSingleThreadExecutor( new ThreadFactoryBuilder()
				.setDaemon( true ).setNameFormat( "BlockingTask runner" ).build() );

		protected final GaussianBlur blur = GaussianBlurBuilder.create().radius( 0 ).build();

		private final LoadingCache<Scene, BlockingTaskHandler> handlers = CacheBuilder.newBuilder().weakKeys()
				.build( new CacheLoader<Scene, BlockingTaskHandler>()
				{
					@Override
					public BlockingTaskHandler load( final Scene scene ) throws Exception
					{
						return new BlockingTaskHandler( scene );
					}

				} );

		private void install( Scene scene )
		{
			scene.addEventHandler( IntentEvent.INTENT_RUN_BLOCKING, handlers.getUnchecked( scene ) );
		}

		private void uninstall( Scene scene )
		{
			scene.removeEventHandler( IntentEvent.INTENT_RUN_BLOCKING, handlers.getIfPresent( scene ) );
		}

		protected Future<?> processRunnable( Scene scene, Runnable runnable, Node... nodes )
		{
			final Parent root = scene.getRoot();
			final TaskProgressIndicator taskProgressIndicator = getTaskProgressIndicatorFor( runnable );
			int spacing = nodes.length == 0 ? 0 : 10;
			VBox container = null;

			final Stage dialog = StageBuilder
					.create()
					.style( StageStyle.UNDECORATED )
					.scene(
							SceneBuilder
									.create()
									.stylesheets( LoaduiFXConstants.getLoaduiStylesheets() )
									.root(
											container = VBoxBuilder.create().padding( new Insets( spacing ) ).spacing( spacing )
													.children( ObjectArrays.concat( taskProgressIndicator, nodes ) ).build() )
									.build() ).build();

			dialog.initModality( Modality.APPLICATION_MODAL );
			taskProgressIndicator.getProgressIndicator().minWidthProperty()
					.bind( Bindings.subtract( container.widthProperty(), 20 ) );
			root.setEffect( blur );
			new Timeline( new KeyFrame( new Duration( 500 ),
					new KeyValue( blur.radiusProperty(), 10, Interpolator.EASE_IN ) ) ).playFromStart();
			final Window window = scene.getWindow();
			dialog.initOwner( window );
			dialog.show();
			stages.add( dialog );
			dialog.setX( window.getX() + ( window.getWidth() - dialog.getWidth() ) / 2 );
			dialog.setY( window.getY() + ( window.getHeight() - dialog.getHeight() ) / 2 );

			Future<?> future = executor.submit( runnable );
			executor.submit( taskDoneRunnable( root, taskProgressIndicator, dialog ) );
			return future;
		}

		private Runnable taskDoneRunnable( final Parent root, final TaskProgressIndicator taskProgressIndicator,
				final Stage dialog )
		{
			return new Runnable()
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
							stages.remove( dialog );
							taskProgressIndicator.dispose();
							if( root.getEffect() == blur )
							{
								root.setEffect( null );
							}
						}
					} );
				}
			};
		}

		private TaskProgressIndicator getTaskProgressIndicatorFor( Runnable runnable )
		{
			return ( runnable instanceof Task ) ? new TaskProgressIndicator( ( Task<?> )runnable )
					: new TaskProgressIndicator();
		}

		private class BlockingTaskHandler implements EventHandler<IntentEvent<? extends Runnable>>
		{
			final Scene scene;

			BlockingTaskHandler( final Scene scene )
			{
				this.scene = scene;
			}

			@Override
			public void handle( IntentEvent<? extends Runnable> event )
			{
				Runnable runnable = event.getArg();
				processRunnable( scene, runnable );
				event.consume();
			}

		}

	}
}
