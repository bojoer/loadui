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
package com.eviware.loadui.api.layout;

/**
 * Provides a set of options which are available for selection for a Property.
 * 
 * @author dain.nilsson
 * 
 * @param <T>
 *           The type of the Property.
 */
public interface OptionsProvider<T> extends Iterable<T>
{
	public static final String OPTIONS = "options";

	/**
	 * Gets the label for a particular option.
	 * 
	 * @param option
	 *           The object to get a label for.
	 * @return The label for the given object.
	 */
	public String labelFor( T option );

	/**
	 * Registers a Runnable instance to run when new options are set.
	 * 
	 * @param onOptionsChange
	 */
	public void registerListener( Runnable onOptionsChange );

	/**
	 * Registers a OptionsListener instance to notify when new options are set.
	 * 
	 * @param listener
	 */
	public void registerListener( OptionsListener listener );

	/**
	 * Unregisters a previously registered listener.
	 * 
	 * @param onOptionsChange
	 */
	public void unregisterListener( Runnable onOptionsChange );

	/**
	 * Unregisters a previously registered listener.
	 * 
	 * @param listener
	 */
	public void unregisterListener( OptionsListener listener );

	/**
	 * Listens to an OptionsProvider and is notified whenever options change.
	 * 
	 * @author dain.nilsson
	 */
	public interface OptionsListener
	{
		/**
		 * Called whenever the options change for an OptionsProvider for which
		 * this listener is registered.
		 * 
		 * @param optionsProvider
		 */
		public void onOptionsChange( OptionsProvider<?> optionsProvider );
	}
}
