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
package com.eviware.loadui.api.component;

import java.util.Collection;

/**
 * A registry for registering and unregistering ComponentDescriptors and
 * BehaviorProviders that the system is able to handle.
 * 
 * @author dain.nilsson
 */
public interface ComponentRegistry extends BehaviorProvider
{
	/**
	 * Get all registered ComponentDescriptors.
	 * 
	 * @return A Collection of ComponentDescriptors.
	 */
	public Collection<ComponentDescriptor> getDescriptors();

	/**
	 * Registers a new ComponentDescriptor with its backing BehaviorProvider.
	 * This will also register the ComponentDescriptors type with the provider.
	 * 
	 * @param descriptor
	 *           The ComponentDescriptor to register.
	 * @param provider
	 *           A BehaviorProvider capable of instantiating a ComponentBehavior
	 *           from the given descriptor.
	 */
	public void registerDescriptor( ComponentDescriptor descriptor, BehaviorProvider provider );

	/**
	 * Registers a new BehaviorProvider for the given type.
	 * 
	 * @param type
	 *           The type to register.
	 * @param provider
	 *           A BehaviorProvider capable of instantiating a ComponentBehavior
	 *           from the given type.
	 */
	public void registerType( String type, BehaviorProvider provider );

	/**
	 * Unregisters a descriptor.
	 * 
	 * @param descriptor
	 *           The ComponentDescriptor to unregister.
	 */
	public void unregisterDescriptor( ComponentDescriptor descriptor );
	
	/**
	 * Finds a descriptor.
	 * 
	 * @param label
	 *           The label of the descriptor to find.
	 * @return The Descriptor with the label, or null if it cannot be found
	 */
	public ComponentDescriptor findDescriptor( String label );

	/**
	 * Unregisters any descriptor with the given type.
	 * 
	 * @param descriptor
	 *           The type of ComponentDescriptors to unregister.
	 */
	public void unregisterType( String componentType );

	/**
	 * Unregisters a BehaviorProvider, and any ComponentDescriptor backed by the
	 * given provider.
	 * 
	 * @param provider
	 *           The BehaviorProvider to unregister.
	 */
	public void unregisterProvider( BehaviorProvider provider );

	/**
	 * Adds a DescriptorListener to be notified when the registered
	 * ComponentDescriptors change.
	 * 
	 * @param listener
	 *           The listener to notify.
	 */
	public void addDescriptorListener( DescriptorListener listener );

	/**
	 * Removes a DescriptorListener to be notified when the registered
	 * ComponentDescriptors change.
	 * 
	 * @param listener
	 *           The listener to remove.
	 */
	public void removeDescriptorListener( DescriptorListener listener );

	/**
	 * Listener interface for being notified of changes to the ComponentRegistry.
	 * 
	 * @author dain.nilsson
	 */
	public interface DescriptorListener
	{
		/**
		 * Called when a descriptor is added.
		 * 
		 * @param descriptor
		 *           The added ComponentDescriptor.
		 */
		public void descriptorAdded( ComponentDescriptor descriptor );

		/**
		 * Called when a descriptor is removed.
		 * 
		 * @param descriptor
		 *           The removed ComponentDescriptor.
		 */
		public void descriptorRemoved( ComponentDescriptor descriptor );
	}
}
