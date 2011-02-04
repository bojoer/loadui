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

/**
 * A BehaviorProvider is used to instantiate ComponentBehaviors either from a
 * ComponentDescriptor or by loading a previously stored ComponentBehavior.
 * 
 * @author dain.nilsson
 */
public interface BehaviorProvider
{
	/**
	 * Creates and initializes a new ComponentBehavior.
	 * 
	 * @param descriptor
	 *           A ComponentDescriptor describing the ComponentBehavior to
	 *           create. It must be supported by the BehaviorProvider.
	 * @param context
	 *           The ComponentContext to make available to the ComponentBehavior.
	 * @return The newly created ComponentBehavior, or null if the
	 *         BehaviorProvider was unable to create one.
	 */
	public ComponentBehavior createBehavior( ComponentDescriptor descriptor, ComponentContext context )
			throws ComponentCreationException;

	/**
	 * Loads a previously saved ComponentBehavior.
	 * 
	 * @param componentType
	 *           A String defining the type of the ComponentBehavior.
	 * @param context
	 *           A ComponentContext that was previously initialized using the
	 *           createBehavior method of this same ComponentBehavior.
	 * @return The loaded ComponentBehavior, or null if the BehaviorProvider was
	 *         unable to load the ComponentBehavior.
	 */
	public ComponentBehavior loadBehavior( String componentType, ComponentContext context )
			throws ComponentCreationException;

	public class ComponentCreationException extends Exception
	{
		private static final long serialVersionUID = 1299528546052371222L;

		private final String componentType;

		public ComponentCreationException( String type )
		{
			this( type, null, null );
		}

		public ComponentCreationException( String type, String message )
		{
			this( type, message, null );
		}

		public ComponentCreationException( String type, Throwable throwable )
		{
			this( type, null, throwable );
		}

		public ComponentCreationException( String type, String message, Throwable throwable )
		{
			super( message, throwable );

			componentType = type;
		}

		public String getComponentType()
		{
			return componentType;
		}
	}
}
