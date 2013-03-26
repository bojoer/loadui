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

import javafx.event.EventHandler;
import javafx.scene.Scene;

import com.eviware.loadui.api.traits.Deletable;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class DeleteTask
{
	private static final DeleteTaskBehavior BEHAVIOR = new DeleteTaskBehavior();

	public static void install( Scene scene )
	{
		BEHAVIOR.install( scene );
	}

	public static void uninstall( Scene scene )
	{
		BEHAVIOR.uninstall( scene );
	}

	private static class DeleteTaskBehavior
	{
		private final LoadingCache<Scene, EventHandler<IntentEvent<? extends Deletable>>> handlers = CacheBuilder
				.newBuilder().weakKeys().build( new CacheLoader<Scene, EventHandler<IntentEvent<? extends Deletable>>>()
				{
					@Override
					public EventHandler<IntentEvent<? extends Deletable>> load( final Scene scene ) throws Exception
					{
						return new EventHandler<IntentEvent<? extends Deletable>>()
						{
							@Override
							public void handle( IntentEvent<? extends Deletable> event )
							{
								event.getArg().delete();
								event.consume();
							}
						};
					}
				} );

		private void install( Scene node )
		{
			node.addEventHandler( IntentEvent.INTENT_DELETE, handlers.getUnchecked( node ) );
		}

		private void uninstall( Scene node )
		{
			node.removeEventHandler( IntentEvent.INTENT_DELETE, handlers.getIfPresent( node ) );
		}
	}
}
