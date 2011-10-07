/*
 * Copyright 2011 SmartBear Software
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
package com.eviware.loadui.util.groovy;

import groovy.lang.Closure;

import java.util.HashMap;
import java.util.Map;

import com.eviware.loadui.api.layout.ActionLayoutComponent;
import com.eviware.loadui.api.layout.LayoutContainer;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.impl.layout.ActionLayoutComponentImpl;
import com.eviware.loadui.impl.layout.LabelLayoutComponentImpl;
import com.eviware.loadui.impl.layout.LayoutComponentImpl;
import com.eviware.loadui.impl.layout.LayoutContainerImpl;
import com.eviware.loadui.impl.layout.PropertyLayoutComponentImpl;
import com.eviware.loadui.impl.layout.SeparatorLayoutComponentImpl;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.layout.DelayedFormattedString;
import com.eviware.loadui.util.layout.FormattedString;
import com.google.common.collect.Maps;

/**
 * Provides a Groovy DSL for creating a LayoutContainer structure.
 * 
 * @author dain.nilsson
 */
public class LayoutBuilder
{
	private final LayoutContainer root;
	private LayoutContainer current;

	public <T extends LayoutContainer> LayoutBuilder( T root )
	{
		this.root = root;
		current = root;
	}

	public LayoutContainer build()
	{
		if( !root.isFrozen() )
		{
			root.freeze();
		}

		return root;
	}

	@Deprecated
	public void box( Closure<?> closure )
	{
		box( "", "", "", closure );
	}

	@Deprecated
	public void box( String layoutConstraints, Closure<?> closure )
	{
		box( layoutConstraints, "", "", closure );
	}

	@Deprecated
	public void box( String layoutConstraints, String colConstraints, Closure<?> closure )
	{
		box( layoutConstraints, colConstraints, "", closure );
	}

	@Deprecated
	public void box( String layoutConstraints, String colConstraints, String rowConstraints, Closure<?> closure )
	{
		box( layoutConstraints, colConstraints, rowConstraints, "", closure );
	}

	@Deprecated
	public void box( String layoutConstraints, String colConstraints, String rowConstraints, String constraints,
			Closure<?> closure )
	{
		LayoutContainer parent = current;
		current = new LayoutContainerImpl( layoutConstraints, colConstraints, rowConstraints, constraints );
		parent.add( current );
		closure.setDelegate( this );
		closure.call();
		current = parent;
	}

	public void box( Map<String, ?> args, Closure<?> closure )
	{
		LayoutContainer parent = current;
		current = new LayoutContainerImpl( args );
		parent.add( current );
		closure.setDelegate( this );
		closure.call();
		current = parent;
	}

	@Deprecated
	public <T> void property( Property<T> property )
	{
		property( property, null, "", false, "" );
	}

	@Deprecated
	public <T> void property( Property<T> property, String label )
	{
		property( property, label, "", false, "" );
	}

	@Deprecated
	public <T> void property( Property<T> property, String label, String constraints )
	{
		property( property, label, constraints, false, "" );
	}

	@Deprecated
	public <T> void property( Property<T> property, String label, String constraints, boolean readOnly )
	{
		property( property, label, constraints, readOnly, "" );
	}

	@Deprecated
	public <T> void property( Property<T> property, String label, String constraints, boolean readOnly, String hint )
	{
		current.add( new PropertyLayoutComponentImpl<T>( property, label, constraints, readOnly, hint ) );
	}

	public <T> void property( Map<String, ?> args )
	{
		current.add( new PropertyLayoutComponentImpl<T>( args ) );
	}

	public ActionLayoutComponent action( Map<String, ?> args )
	{
		ActionLayoutComponentImpl action = new ActionLayoutComponentImpl( args );
		current.add( action );
		return action;
	}

	@Deprecated
	public void label( String label )
	{
		label( label, "" );
	}

	@Deprecated
	public void label( String label, String constraints )
	{
		current.add( new LabelLayoutComponentImpl( label, constraints ) );
	}

	public void label( Map<String, ?> args )
	{
		current.add( new LabelLayoutComponentImpl( args ) );
	}

	@Deprecated
	public void separator()
	{
		separator( false, "" );
	}

	@Deprecated
	public void separator( boolean vertical )
	{
		separator( vertical, "" );
	}

	@Deprecated
	public void separator( boolean vertical, String constraints )
	{
		current.add( new SeparatorLayoutComponentImpl( vertical, constraints ) );
	}

	public void separator( Map<String, ?> args )
	{
		current.add( new SeparatorLayoutComponentImpl( args ) );
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public void node( Map<String, ?> args )
	{
		if( args.containsKey( "property" ) )
		{
			current.add( new PropertyLayoutComponentImpl( args ) );
		}
		else if( args.containsKey( "content" ) && !args.containsKey( "fString" ) )
		{
			HashMap<String, Object> newArgs = Maps.newHashMap( args );
			Object content = newArgs.get( "content" );
			FormattedString fString = content instanceof Closure ? new DelayedFormattedString( "%s", ( Closure )content )
					: new FormattedString( String.valueOf( content ) );
			newArgs.put( "fString", fString );
			current.add( new FormattedStringLayoutComponent( newArgs ) );
		}
		else if( args.containsKey( "action" ) )
		{
			current.add( new ActionLayoutComponentImpl( args ) );
		}
		else
		{
			current.add( new LayoutComponentImpl( args ) );
		}
	}

	private static class FormattedStringLayoutComponent extends LayoutComponentImpl implements Releasable
	{
		public FormattedStringLayoutComponent( Map<String, ?> args )
		{
			super( args );
		}

		@Override
		public void release()
		{
			ReleasableUtils.releaseAll( get( "fString" ) );
		}
	}
}