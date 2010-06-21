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
package com.eviware.loadui.api.deferrable;

import java.util.Collection;

/**
 * Schedules and runs Deferrables.
 * 
 * @author dain.nilsson
 */
@Deprecated
public interface DeferrableManager
{
	/**
	 * Get a Collection of all currently running Deferrables.
	 * 
	 * @return
	 */
	Collection<Deferrable> getDeferrables();

	/**
	 * Starts a Deferrable which will be run until completion.
	 * 
	 * @param deferrable
	 *           A Deferrable to run.
	 */
	void defer( Deferrable deferrable );

	/**
	 * Cancels a running Deferrable.
	 * 
	 * @param deferrable
	 */
	void cancel( Deferrable deferrable );

	/**
	 * Forces the run method of all running Deferrables to be invoked.
	 */
	void resolve();
}
