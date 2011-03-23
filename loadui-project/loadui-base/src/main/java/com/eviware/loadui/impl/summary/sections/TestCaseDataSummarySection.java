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

public class TestCaseDataSummarySection extends MutableSectionImpl implements DataSummarySection
{

	SceneItemImpl testcase;

	public TestCaseDataSummarySection( SceneItem testcase )
	{
		super( testcase.getLabel() );
		this.testcase = ( SceneItemImpl )testcase;
		addValue( "time", getTime() );
		addValue( "requests", getNumberOfSamples() );
		addValue( "failures", getNumberOfFailures() );
		addValue( "status", getStatus() );
	}

	@Override
	public String getNumberOfFailures()
	{
		return String.valueOf( testcase.getCounter( CanvasItem.FAILURE_COUNTER ).get() );
	}

	@Override
	public String getStatus()
	{
		if( testcase.getLimit( CanvasItem.FAILURE_COUNTER ) == -1
				|| testcase.getCounter( CanvasItem.FAILURE_COUNTER ).get() < testcase.getLimit( CanvasItem.FAILURE_COUNTER ) )
		{
			return "Passed";
		}
		else
			return "Failed";
	}

	@Override
	public String getTime()
	{
		return CalendarUtils.formatInterval( testcase.getStartTime(), testcase.getEndTime() );
	}

	@Override
	public String getNumberOfSamples()
	{
		return String.valueOf( testcase.getCounter( CanvasItem.SAMPLE_COUNTER ).get() );
	}

}
