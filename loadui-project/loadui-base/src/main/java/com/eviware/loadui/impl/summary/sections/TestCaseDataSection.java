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
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.impl.summary.MutableSectionImpl;
import com.eviware.loadui.util.summary.CalendarUtils;

public class TestCaseDataSection extends MutableSectionImpl
{
	SceneItem testcase;

	public TestCaseDataSection( SceneItem sceneItem )
	{
		super( "Scenario Data" );
		testcase = sceneItem;
		addValue( "Number of components", getNumberOfComponents() );
		addValue( "Number of connections", getNumberOfConnections() );
		addValue( "Time Limit", getLimit() );
		addValue( "Request Limit", getSampleLimit() );
		addValue( "Assertion Limit", getFailureLimit() );
	}

	private String getNumberOfComponents()
	{
		return String.valueOf( testcase.getComponents().size() );
	}

	private String getNumberOfConnections()
	{
		return String.valueOf( testcase.getConnections().size() );
	}

	private String getLimit()
	{
		if( testcase.getLimit( CanvasItem.TIMER_COUNTER ) > -1 )
			return CalendarUtils.formatInterval( testcase.getLimit( CanvasItem.TIMER_COUNTER ) * 1000 );
		return "N/A";
	}

	private String getSampleLimit()
	{
		if( testcase.getLimit( CanvasItem.SAMPLE_COUNTER ) > -1 )
			return String.valueOf( testcase.getLimit( CanvasItem.SAMPLE_COUNTER ) );
		return "N/A";
	}

	private String getFailureLimit()
	{
		if( testcase.getLimit( CanvasItem.FAILURE_COUNTER ) > -1 )
			return String.valueOf( testcase.getLimit( CanvasItem.FAILURE_COUNTER ) );
		return "N/A";
	}
}
