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
package com.eviware.loadui.api.serialization;

import javax.annotation.Nonnull;

/**
 * A value which can be observed for updates. Note that a ListenableValue may be
 * updated while retaining the same value as the previous update.
 * 
 * @author dain.nilsson
 * 
 * @param <T>
 */
public interface ListenableValue<T> extends Value<T>
{
	/**
	 * Adds a ValueListener to be notified of any update to the value.
	 * 
	 * @param listener
	 */
	public void addListener( @Nonnull ValueListener<? super T> listener );

	/**
	 * Removes a ValueListener from being notified of value updates.
	 * 
	 * @param listener
	 */
	public void removeListener( @Nonnull ValueListener<? super T> listener );

	/**
	 * A Listener interface for listening to updates to a ListenableValue.
	 * 
	 * @author dain.nilsson
	 * 
	 * @param <T>
	 */
	public interface ValueListener<T>
	{
		/**
		 * Notify the ValueListener that the value has been updated.
		 * 
		 * @param value
		 */
		public void update( T value );
	}
}
