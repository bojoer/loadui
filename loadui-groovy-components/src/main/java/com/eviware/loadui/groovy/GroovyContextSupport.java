/*
 * Copyright 2010 eviware software ab
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.groovy;

import java.util.Collections;
import java.util.EventObject;
import java.util.Map;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.ComponentContext.Scope;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.layout.SettingsLayoutContainer;
import com.eviware.loadui.api.serialization.Value;
import com.eviware.loadui.impl.layout.LayoutContainerImpl;
import com.eviware.loadui.impl.layout.SettingsLayoutContainerImpl;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;

public class GroovyContextSupport extends GroovyObjectSupport
{
	private final ComponentContext context;

	public GroovyContextSupport( ComponentContext context )
	{
		this.context = context;
	}

	public void layout( Closure closure )
	{
		Map<String, ?> map = Collections.emptyMap();
		layout( map, closure );
	}

	public void layout( Map<String, ?> args, Closure closure )
	{
		LayoutBuilder layoutBuilder = new LayoutBuilder( new LayoutContainerImpl( args ) );
		closure.setDelegate( layoutBuilder );
		closure.call();
		context.setLayout( layoutBuilder.build() );
	}

	public void settings( Closure closure )
	{
		Map<String, ?> map = Collections.emptyMap();
		settings( map, closure );
	}

	public void settings( Map<String, ?> args, Closure closure )
	{
		SettingsLayoutContainer layoutContainer = new SettingsLayoutContainerImpl( args );
		LayoutBuilder layoutBuilder = new LayoutBuilder( layoutContainer );
		closure.setDelegate( layoutBuilder );
		closure.call();
		context.addSettingsTab( ( SettingsLayoutContainer )layoutBuilder.build() );
	}

	public void triggerAction( String actionName, String scope )
	{
		context.triggerAction( actionName, Scope.valueOf( scope ) );
	}

	public void triggerAction( String actionName )
	{
		context.triggerAction( actionName, Scope.COMPONENT );
	}

	public <T extends EventObject> EventHandler<T> addEventListener( Class<T> type, final Closure closure )
	{
		EventHandler<T> listener = new EventHandler<T>()
		{
			@Override
			public void handleEvent( T event )
			{
				closure.call( event );
			}
		};
		context.addEventListener( type, listener );
		return listener;
	}

	public Value<?> value( Closure closure )
	{
		return new ClosureValue( closure );
	}

	private class ClosureValue implements Value<Object>
	{
		private final Closure closure;

		public ClosureValue( Closure closure )
		{
			this.closure = closure;
		}

		@Override
		public Class<Object> getType()
		{
			return Object.class;
		}

		@Override
		public Object getValue()
		{
			return closure.call();
		}
	}
}
