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
package com.eviware.loadui.ui.fx.util;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Scene;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;

public class InspectorHelpers
{
	public static ReadOnlyProperty<ProjectItem> projectProperty( ReadOnlyProperty<Scene> sceneProperty )
	{
		final ReadOnlyObjectWrapper<ProjectItem> project = new ReadOnlyObjectWrapper<>();

		final EventHandler<IntentEvent<?>> sceneIntentHandler = new EventHandler<IntentEvent<?>>()
		{
			private Observable projectLoaded;

			@Override
			public void handle( IntentEvent<?> event )
			{
				if( event.getArg() instanceof ProjectRef )
				{
					final ProjectRef ref = ( ProjectRef )event.getArg();
					if( ref.isEnabled() )
					{
						projectLoaded = null;
						project.set( ref.getProject() );
					}
					else
					{
						project.set( null );
						projectLoaded = Properties.observeEvent( ref, ProjectRef.LOADED );
						projectLoaded.addListener( new InvalidationListener()
						{
							@Override
							public void invalidated( Observable arg0 )
							{
								project.set( ref.getProject() );
								projectLoaded.removeListener( this );
								projectLoaded = null;
							}
						} );
					}
				}
			}
		};

		sceneProperty.addListener( new ChangeListener<Scene>()
		{
			@Override
			public void changed( ObservableValue<? extends Scene> arg0, Scene oldScene, Scene newScene )
			{
				if( oldScene != null )
				{
					oldScene.removeEventFilter( IntentEvent.INTENT_OPEN, sceneIntentHandler );
				}
				if( newScene != null )
				{
					newScene.addEventFilter( IntentEvent.INTENT_OPEN, sceneIntentHandler );
				}
			}
		} );
		Scene scene = sceneProperty.getValue();
		if( scene != null )
		{
			scene.addEventFilter( IntentEvent.INTENT_OPEN, sceneIntentHandler );
		}

		return project.getReadOnlyProperty();
	}
}
