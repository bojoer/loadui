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
package com.eviware.loadui.api.testevents;

import com.eviware.loadui.api.traits.Labeled;

/**
 * A persisted event shown in the TestEventLog.
 * 
 * @author dain.nilsson
 */
public interface TestEvent
{
	/**
	 * Gets the timestamp of the TestEvent measured in milliseconds since the
	 * start of the Execution.
	 * 
	 * @return
	 */
	public long getTimestamp();

	/**
	 * Returns the type of the TestEvent. This should either be the class of the
	 * TestEvent instance itself, or a superclass to it.
	 * 
	 * @return
	 */
	public <T extends TestEvent> Class<T> getType();

	/**
	 * A factory responsible for instantiating a TestEvent from its serialized
	 * data, and for serializing a TestEvents data into a binary format.
	 * 
	 * @author dain.nilsson
	 * 
	 * @param <T>
	 */
	public interface Factory<T extends TestEvent> extends Labeled
	{
		/**
		 * Returns the type of TestEvent instantiated by the Factory.
		 * 
		 * @return
		 */
		public Class<T> getType();

		/**
		 * Instantiates a TestEvent of the Factories type, using the given data.
		 * 
		 * @param timestamp
		 * @param data
		 * @return
		 */
		public T createTestEvent( long timestamp, byte[] sourceData, byte[] entryData );

		/**
		 * Gets the serialized data for the given TestEvent, which can later be
		 * used to recreate the TestEvent.
		 * 
		 * @param testEvent
		 * @return
		 */
		public byte[] getDataForTestEvent( T testEvent );
	}

	/**
	 * Implemented by an Object which is able to log TestEvents of a given type.
	 * This object is used as a key for filtering the TestEvent log.
	 * 
	 * @author dain.nilsson
	 * 
	 * @param <T>
	 */
	public interface Source<T extends TestEvent> extends Labeled
	{
		/**
		 * Gets the type of the TestEvents logged by this TestEventSource.
		 * 
		 * @return
		 */
		public Class<T> getType();

		/**
		 * Gets the TestEventSource configuration data, used to recreate the
		 * TestEvent without access to the original TestEventSource, passed to the
		 * TestEvent.Factory.
		 * 
		 * @return
		 */
		public byte[] getData();

		/**
		 * A String which must be unique for the type of TestEventSource with a
		 * specific configuration. Any time the label, or the byte[] returned by
		 * getData() changes, so must the hash. This method should be optimized as
		 * it will potentially be run often.
		 * 
		 * @return
		 */
		public String getHash();
	}
}