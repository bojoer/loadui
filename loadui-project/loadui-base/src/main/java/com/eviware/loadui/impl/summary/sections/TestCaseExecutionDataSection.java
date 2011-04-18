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
package com.eviware.loadui.impl.summary.sections;

import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.impl.model.SceneItemImpl;
import com.eviware.loadui.impl.summary.MutableSectionImpl;
import com.eviware.loadui.util.summary.CalendarUtils;

public class TestCaseExecutionDataSection extends MutableSectionImpl implements ExecutionDataSection
{

	SceneItemImpl testcase;

	public TestCaseExecutionDataSection( SceneItem sceneItem )
	{
		super( "Execution Data" );
		testcase = ( SceneItemImpl )sceneItem;
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
	public String getExecutionTime()
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
	public String getStartTime()
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
	public String getEndTime()
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
	public String getTotalNumberOfRequests()
	{
		return String.valueOf( testcase.getCounter( CanvasItem.SAMPLE_COUNTER ).get() );
	}

	public String getTotalNumberOfFailedRequests()
	{
		return String.valueOf( testcase.getCounter( CanvasItem.REQUEST_FAILURE_COUNTER ).get() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.eviware.loadui.impl.summary.sections.ExecutionDataSection#
	 * getTotalNumberOfAssertions()
	 */
	public String getTotalNumberOfAssertions()
	{
		return String.valueOf( testcase.getCounter( CanvasItem.ASSERTION_COUNTER ).get() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.eviware.loadui.impl.summary.sections.ExecutionDataSection#
	 * getTotalNumberOfFailedAssertions()
	 */
	public String getTotalNumberOfFailedAssertions()
	{
		return String.valueOf( testcase.getCounter( CanvasItem.ASSERTION_FAILURE_COUNTER ).get() );
	}
}
