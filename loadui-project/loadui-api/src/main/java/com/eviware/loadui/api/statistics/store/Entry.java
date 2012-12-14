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
package com.eviware.loadui.api.statistics.store;

import java.util.Set;

/**
 * An entry of a Track, corresponds to a database record where the timestamp is
 * the key and the values are the data.
 * 
 * @author dain.nilsson
 */
public interface Entry
{
	/**
	 * Gets the timestamp of the Entry measured in the number of milliseconds
	 * which which have passed since the start of the Execution.
	 * 
	 * @return
	 */
	public long getTimestamp();

	/**
	 * Gets the names of the values in the Entry.
	 * 
	 * @return
	 */
	public Set<String> getNames();

	/**
	 * Gets the value for the given name.
	 * 
	 * @param name
	 * @return
	 */
	public Number getValue( String name );
}
