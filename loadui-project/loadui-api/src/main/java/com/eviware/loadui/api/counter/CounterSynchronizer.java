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
package com.eviware.loadui.api.counter;

import java.util.Map;

import com.eviware.loadui.api.messaging.MessageEndpoint;

public interface CounterSynchronizer
{
	public final static String CHANNEL = "/" + CounterSynchronizer.class.getName();

	public final static String COUNTER_HOLDER_ID = "counterHolderId";

	public void syncAggregator( String ownerId, Aggregator aggregator );

	public void unsyncAggregator( String ownerId );

	public void syncCounters( CounterHolder counterHolder, MessageEndpoint endpoint );

	public void unsyncCounters( CounterHolder counterHolder );

	public interface Aggregator
	{
		public void updateChildValues( MessageEndpoint child, Map<String, String> values );
	}
}
