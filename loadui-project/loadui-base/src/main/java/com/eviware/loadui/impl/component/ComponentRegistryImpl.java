/*
 * Copyright 2011 eviware software ab
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
package com.eviware.loadui.impl.component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.component.BehaviorProvider;
import com.eviware.loadui.api.component.ComponentBehavior;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.component.ComponentRegistry;

public class ComponentRegistryImpl implements ComponentRegistry
{
	public static final Logger log = LoggerFactory.getLogger( ComponentRegistryImpl.class );

	private final Map<ComponentDescriptor, BehaviorProvider> descriptors = new HashMap<ComponentDescriptor, BehaviorProvider>();
	private final Map<String, BehaviorProvider> types = new HashMap<String, BehaviorProvider>();
	private final Set<DescriptorListener> listeners = new HashSet<DescriptorListener>();

	@Override
	public Collection<ComponentDescriptor> getDescriptors()
	{
		return Collections.unmodifiableSet( descriptors.keySet() );
	}

	@Override
	public void registerDescriptor( ComponentDescriptor descriptor, BehaviorProvider provider )
	{
		if( provider == null || descriptor == null )
			throw new IllegalArgumentException(
					"Cannot register ComponentDescriptor with null provider, or null descriptor!" );
		descriptors.put( descriptor, provider );
		types.put( descriptor.getType(), provider );
		log.info( "Registered Component Descriptor: {}", descriptor.getLabel() );
		fireDescriptorAdded( descriptor );
	}

	public void registerType( String type, BehaviorProvider provider )
	{
		if( provider == null || type == null )
			throw new IllegalArgumentException( "Cannot register ComponentDescriptor with null provider, or null type!" );

		types.put( type, provider );
	}

	@Override
	public void unregisterDescriptor( ComponentDescriptor descriptor )
	{
		if( descriptors.remove( descriptor ) != null )
			fireDescriptorRemoved( descriptor );

		for( ComponentDescriptor d : descriptors.keySet() )
			if( d.getType().equals( descriptor.getType() ) )
				return;
		types.remove( descriptor.getType() );
	}

	@Override
	public ComponentDescriptor findDescriptor( String label )
	{
		for( ComponentDescriptor cd : getDescriptors() )
		{
			if( cd.getLabel().equals( label ) )
				return cd;
		}
		return null;
	}

	@Override
	public void unregisterProvider( BehaviorProvider provider )
	{
		for( Iterator<Entry<ComponentDescriptor, BehaviorProvider>> it = descriptors.entrySet().iterator(); it.hasNext(); )
		{
			Entry<ComponentDescriptor, BehaviorProvider> entry = it.next();
			if( entry.getValue() == provider )
			{
				fireDescriptorRemoved( entry.getKey() );
				it.remove();
			}
		}
		for( Iterator<Entry<String, BehaviorProvider>> it = types.entrySet().iterator(); it.hasNext(); )
		{
			Entry<String, BehaviorProvider> entry = it.next();
			if( entry.getValue() == provider )
				it.remove();
		}
	}

	@Override
	public void unregisterType( String componentType )
	{
		types.remove( componentType );
		for( Iterator<Entry<ComponentDescriptor, BehaviorProvider>> it = descriptors.entrySet().iterator(); it.hasNext(); )
		{
			Entry<ComponentDescriptor, BehaviorProvider> entry = it.next();
			if( entry.getKey().getType().equals( componentType ) )
			{
				fireDescriptorRemoved( entry.getKey() );
				it.remove();
			}
		}
	}

	@Override
	public ComponentBehavior createBehavior( ComponentDescriptor descriptor, ComponentContext context )
			throws ComponentCreationException
	{
		if( descriptors.containsKey( descriptor ) )
			return descriptors.get( descriptor ).createBehavior( descriptor, context );
		throw new RuntimeException( "No Provider exists for the given ComponentDescriptor!" );
	}

	@Override
	public ComponentBehavior loadBehavior( String componentType, ComponentContext context )
			throws ComponentCreationException
	{
		if( types.containsKey( componentType ) )
			return types.get( componentType ).loadBehavior( componentType, context );
		throw new RuntimeException( "No Provider exists for the type '" + componentType + "'!" );
	}

	private void fireDescriptorAdded( ComponentDescriptor descriptor )
	{
		for( DescriptorListener listener : listeners )
			listener.descriptorAdded( descriptor );
	}

	private void fireDescriptorRemoved( ComponentDescriptor descriptor )
	{
		for( DescriptorListener listener : listeners )
			listener.descriptorRemoved( descriptor );
	}

	@Override
	public void addDescriptorListener( DescriptorListener listener )
	{
		listeners.add( listener );
	}

	@Override
	public void removeDescriptorListener( DescriptorListener listener )
	{
		listeners.remove( listener );
	}
}
