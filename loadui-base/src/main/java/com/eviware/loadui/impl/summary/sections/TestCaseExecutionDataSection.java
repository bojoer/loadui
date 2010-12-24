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
package com.eviware.loadui.impl.summary.sections;

import java.text.SimpleDateFormat;

import com.eviware.loadui.api.component.categories.AnalysisCategory;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.impl.model.SceneItemImpl;
import com.eviware.loadui.impl.summary.MutableSectionImpl;
import com.eviware.loadui.util.summary.CalendarUtils;

public class TestCaseExecutionDataSection extends MutableSectionImpl implements ExecutionDataSection
{

	SceneItemImpl testcase;
	SimpleDateFormat format = new SimpleDateFormat( "HH:mm:ss" );

	public TestCaseExecutionDataSection( SceneItem sceneItem )
	{
		super( "Execution Data" );
		testcase = ( SceneItemImpl )sceneItem;
		addValue( "Execution Time", getExecutionTime() );// hh:mm:ss
		addValue( "Start Time", getStartTime() );
		addValue( "End Time", getEndTime() );
		addValue( "Total number of requests", getTotalNumberOfSamples() );
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
		return CalendarUtils.getFormattedPeriod( testcase.getStartTime(), testcase.getEndTime() );
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
		return format.format( testcase.getStartTime() );
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
		return format.format( testcase.getEndTime() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.eviware.loadui.impl.summary.sections.ExecutionDataSection#
	 * getTotalNumberOfSamples()
	 */
	public String getTotalNumberOfSamples()
	{
		return String.valueOf( testcase.getCounter( CanvasItem.SAMPLE_COUNTER ).get() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.eviware.loadui.impl.summary.sections.ExecutionDataSection#
	 * getTotalNumberOfAssertions()
	 */
	public String getTotalNumberOfAssertions()
	{
		int cnt = 0;
		for( ComponentItem component : testcase.getComponents() )
		{
			if( component.getType().equalsIgnoreCase( "assertion" ) & component.getBehavior() instanceof AnalysisCategory )
				cnt += component.getCounter( CanvasItem.ASSERTION_COUNTER ).get();
		}
		return String.valueOf( testcase.getCounter( CanvasItem.ASSERTION_COUNTER ).get() + cnt );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.eviware.loadui.impl.summary.sections.ExecutionDataSection#
	 * getTotalNumberOfFailedAssertions()
	 */
	public String getTotalNumberOfFailedAssertions()
	{
		return String.valueOf( testcase.getCounter( CanvasItem.FAILURE_COUNTER ).get() );
	}
}
