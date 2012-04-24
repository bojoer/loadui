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
package com.eviware.loadui.impl.summary.sections;

import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.impl.model.SceneItemImpl;
import com.eviware.loadui.impl.summary.MutableSectionImpl;
import com.eviware.loadui.util.summary.CalendarUtils;

public class TestCaseExecutionDataSection extends MutableSectionImpl implements ExecutionDataSection
{

	SceneItemImpl testcase;

	public TestCaseExecutionDataSection( SceneItemImpl sceneItem )
	{
		super( "Execution Data" );
		testcase = sceneItem;
		addValue( "Duration", getExecutionTime() );// hh:mm:ss
		addValue( "Start Time", getStartTime() );
		addValue( "End Time", getEndTime() );
		addValue( "Total number of requests", getTotalNumberOfRequests() );
		addValue( "Total number of failed requests", getTotalNumberOfFailedRequests() );
		addValue( "Total number of assertions", getTotalNumberOfAssertions() );
		addValue( "Total number of failed assertions", getTotalNumberOfFailedAssertions() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.eviware.loadui.impl.summary.sections.ExecutionDataSection#
	 * getExecutionTime()
	 */
	@Override
	public final String getExecutionTime()
	{
		return CalendarUtils.formatInterval( testcase.getStartTime(), testcase.getEndTime() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.loadui.impl.summary.sections.ExecutionDataSection#getStartTime
	 * ()
	 */
	@Override
	public final String getStartTime()
	{
		if( testcase.getStartTime() == null )
			return "N/A";
		return CalendarUtils.formatAbsoluteTime( testcase.getStartTime() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.loadui.impl.summary.sections.ExecutionDataSection#getEndTime
	 * ()
	 */
	@Override
	public final String getEndTime()
	{
		if( testcase.getEndTime() == null )
			return "N/A";
		return CalendarUtils.formatAbsoluteTime( testcase.getEndTime() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.eviware.loadui.impl.summary.sections.ExecutionDataSection#
	 * getTotalNumberOfSamples()
	 */
	@Override
	public final String getTotalNumberOfRequests()
	{
		return String.valueOf( testcase.getCounter( CanvasItem.SAMPLE_COUNTER ).get() );
	}

	public final String getTotalNumberOfFailedRequests()
	{
		return String.valueOf( testcase.getCounter( CanvasItem.REQUEST_FAILURE_COUNTER ).get() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.eviware.loadui.impl.summary.sections.ExecutionDataSection#
	 * getTotalNumberOfAssertions()
	 */
	@Override
	public final String getTotalNumberOfAssertions()
	{
		return String.valueOf( testcase.getCounter( CanvasItem.ASSERTION_COUNTER ).get() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.eviware.loadui.impl.summary.sections.ExecutionDataSection#
	 * getTotalNumberOfFailedAssertions()
	 */
	@Override
	public final String getTotalNumberOfFailedAssertions()
	{
		return String.valueOf( testcase.getCounter( CanvasItem.ASSERTION_FAILURE_COUNTER ).get() );
	}
}
