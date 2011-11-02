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

import java.awt.Image;
import java.io.File;
import java.util.Collection;
import java.util.Set;

import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.model.AttributeHolder;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEventSourceDescriptor;
import com.eviware.loadui.api.testevents.TestEventTypeDescriptor;
import com.eviware.loadui.api.traits.Deletable;
import com.eviware.loadui.api.traits.Labeled;

/**
 * Represents statistical data gathered during a load test Execution.
 * 
 * @author dain.nilsson
 */
public interface Execution extends Labeled.Mutable, EventFirer, AttributeHolder, Deletable
{
	/**
	 * BaseEvent key for notifying that this Execution has been archived.
	 */
	public final static String ARCHIVED = Execution.class.getSimpleName() + "@archived";

	/**
	 * BaseEvent key for notifying that this Execution has been deleted.
	 */
	public final static String ICON = Execution.class.getSimpleName() + "@icon";

	/**
	 * Gets the Executions ID.
	 * 
	 * @return
	 */
	public String getId();

	/**
	 * Gets the start time of the test execution, measured as a Unix Timestamp
	 * given in milliseconds.
	 * 
	 * @return
	 */
	public long getStartTime();

	/**
	 * Gets the Track with the specified ID.
	 * 
	 * @param trackId
	 * @return
	 */
	public Track getTrack( String trackId );

	/**
	 * Gets a Collection of the IDs of the contained Tracks.
	 * 
	 * @return
	 */
	public Collection<String> getTrackIds();

	/**
	 * Gets the contained TestEventTypeDescriptors.
	 * 
	 * @return
	 */
	public Set<TestEventTypeDescriptor> getEventTypes();

	/**
	 * Returns the number of TestEvents stored for the Execution, matching any of
	 * the given sources (or all, if no sources are given).
	 * 
	 * @param sources
	 * @return
	 */
	public int getTestEventCount( TestEventSourceDescriptor... sources );

	/**
	 * Returns all stored TestEvents between the given interval, of the given
	 * type.
	 * 
	 * @param startTime
	 * @param endTime
	 * @param sources
	 *           The label of TestEventSources of TestEvent to filter on,
	 *           multiple types gives TestEvents matching any of the types, no
	 *           sources given will return all TestEvents.
	 * @return
	 */
	public Iterable<TestEvent.Entry> getTestEventRange( long startTime, long endTime,
			TestEventSourceDescriptor... sources );

	/**
	 * Returns an Iterable of the TestEvents starting with the TestEvent at the
	 * position indicated by the index parameter (the index is applied after
	 * filtering). When the reversed boolean is true, the iterator will move
	 * backwards through the available TestEvents, returning them in
	 * reverse-chronological order.
	 * 
	 * @param index
	 * @param reversed
	 * @param sources
	 * @return
	 */
	public Iterable<TestEvent.Entry> getTestEvents( int index, boolean reversed, TestEventSourceDescriptor... sources );

	/**
	 * Determines if this execution has been archived or not.
	 * 
	 * @return true if execution is archived, false otherwise.
	 */
	public boolean isArchived();

	/**
	 * Marks execution as archived.
	 */
	public void archive();

	/**
	 * Gets the length of this execution.
	 */
	public long getLength();

	/**
	 * Returns a File-object containing the Summary report (serialized
	 * JasperPrint-object) from this Execution.
	 */
	public File getSummaryReport();

	/**
	 * Returns a image icon.
	 */
	public Image getIcon();

	/**
	 * Sets an image icon.
	 */
	public void setIcon( Image image );
}
