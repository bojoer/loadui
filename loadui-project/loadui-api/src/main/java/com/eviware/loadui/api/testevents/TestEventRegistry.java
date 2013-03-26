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
package com.eviware.loadui.api.testevents;

import javax.annotation.Nullable;

import com.eviware.loadui.api.testevents.TestEvent;

/**
 * Instantiates a TestEvent from the stored TestEvent data, using available
 * EventTest.Factories.
 * 
 * @author dain.nilsson
 */
public interface TestEventRegistry
{
	/**
	 * Gets the TestEvent.Factory registered for the given type, if available.
	 * Returns null if no factory is found.
	 * 
	 * @param type
	 * @return
	 */
	@Nullable
	public TestEvent.Factory<?> lookupFactory( String type );

	/**
	 * Gets the TestEvent.Factory registered for the given type, if available.
	 * Returns null if no factory is found.
	 * 
	 * @param type
	 * @return
	 */
	@Nullable
	public <T extends TestEvent> TestEvent.Factory<T> lookupFactory( Class<T> type );
}
