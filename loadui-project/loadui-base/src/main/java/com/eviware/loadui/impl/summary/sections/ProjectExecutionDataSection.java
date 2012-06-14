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

import javax.swing.table.TableModel;

import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.impl.model.ProjectItemImpl;
import com.eviware.loadui.impl.model.SceneItemImpl;
import com.eviware.loadui.impl.summary.MutableSectionImpl;
import com.eviware.loadui.impl.summary.sections.tablemodels.TestCaseDataTableModel;
import com.eviware.loadui.util.summary.CalendarUtils;

final public class ProjectExecutionDataSection extends MutableSectionImpl
{

	ProjectItemImpl project;

	public ProjectExecutionDataSection( ProjectItemImpl projectItemImpl )
	{

		super( "Execution Data" );
		project = projectItemImpl;
		addValue( "Duration", getExecutionTime() );
		addValue( "Start Time", getStartTime() );
		addValue( "End Time", getEndTime() );
		addValue( "Total number of requests", getTotalNumberOfRequests() );
		addValue( "Total number of failed requests", getTotalNumberOfFailedRequests() );
		addValue( "Total number of assertions", getTotalNumberOfAssertions() );
		addValue( "Total number of failed assertions", getTotalNumberOfFailedAssertions() );

		addTable( "Scenario Data", getTestcaseDataTable() );
	}

	public TableModel getTestcaseDataTable()
	{
		TestCaseDataTableModel model = new TestCaseDataTableModel();
		for( SceneItemImpl testcase : project.getChildren() )
		{
			if( testcase.getStartTime() != null && testcase.getEndTime() != null )
				model.add( new TestCaseDataTableModel.TestCaseDataModel( testcase ) );
		}
		return model;
	}

	public String getEndTime()
	{
		return CalendarUtils.formatAbsoluteTime( project.getEndTime() );
	}

	public String getExecutionTime()
	{
		return CalendarUtils.formatInterval( project.getStartTime(), project.getEndTime() );
	}

	public String getStartTime()
	{
		return CalendarUtils.formatAbsoluteTime( project.getStartTime() );
	}

	public String getTotalNumberOfAssertions()
	{
		return String.valueOf( project.getCounter( CanvasItem.ASSERTION_COUNTER ).get() );
	}

	public String getTotalNumberOfFailedAssertions()
	{
		return String.valueOf( project.getCounter( CanvasItem.ASSERTION_FAILURE_COUNTER ).get() );
	}

	public String getTotalNumberOfRequests()
	{
		return String.valueOf( project.getCounter( CanvasItem.SAMPLE_COUNTER ).get() );
	}

	public String getTotalNumberOfFailedRequests()
	{
		return String.valueOf( project.getCounter( CanvasItem.REQUEST_FAILURE_COUNTER ).get() );
	}
}
