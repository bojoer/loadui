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

import javax.swing.table.TableModel;

import com.eviware.loadui.api.component.categories.AnalysisCategory;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.impl.model.ProjectItemImpl;
import com.eviware.loadui.impl.model.SceneItemImpl;
import com.eviware.loadui.impl.summary.MutableSectionImpl;
import com.eviware.loadui.impl.summary.sections.tablemodels.TestCaseDataTableModel;
import com.eviware.loadui.util.summary.CalendarUtils;

public class ProjectExecutionDataSection extends MutableSectionImpl implements ExecutionDataSection
{

	ProjectItemImpl project;
	private SimpleDateFormat dateFormat = new SimpleDateFormat( "HH:mm:ss" );

	public ProjectExecutionDataSection( ProjectItemImpl projectItemImpl )
	{

		super( "Execution Data" );
		project = projectItemImpl;
		addValue( "Execution Time", getExecutionTime() );
		addValue( "Start Time", getStartTime() );
		addValue( "End Time", getEndTime() );
		addValue( "Total number of requests", getTotalNumberOfSamples() );
		addValue( "Total number of assertions", getTotalNumberOfAssertions() );
		addValue( "Total number of failed assertions", getTotalNumberOfFailedAssertions() );

		addTable( "TestCase Data", getTestcaseDataTable() );
	}

	public TableModel getTestcaseDataTable()
	{
		TestCaseDataTableModel model = new TestCaseDataTableModel();
		for( SceneItem testcase : project.getScenes() )
		{
			model.add( model.new TestCaseDataModel( ( SceneItemImpl )testcase ) );
		}
		return model;
	}

	@Override
	public String getEndTime()
	{
		return CalendarUtils.format( project.getEndTime() );
	}

	@Override
	public String getExecutionTime()
	{
		return CalendarUtils.getFormattedPeriod( project.getStartTime(), project.getEndTime() );
	}

	@Override
	public String getStartTime()
	{
		return CalendarUtils.format( project.getStartTime() );
	}

	@Override
	public String getTotalNumberOfAssertions()
	{
		int cnt = 0;
		for( ComponentItem component : project.getComponents() )
		{
			if( component.getType().equalsIgnoreCase( "assertion" ) & component.getBehavior() instanceof AnalysisCategory )
				cnt += component.getCounter( CanvasItem.ASSERTION_COUNTER ).get();
		}
		for( SceneItem scene : project.getScenes() )
			for( ComponentItem component : scene.getComponents() )
			{
				if( component.getType().equalsIgnoreCase( "assertion" )
						& component.getBehavior() instanceof AnalysisCategory )
					cnt += component.getCounter( CanvasItem.ASSERTION_COUNTER ).get();
			}
		return String.valueOf( project.getCounter( CanvasItem.ASSERTION_COUNTER ).get() + cnt );
	}

	@Override
	public String getTotalNumberOfFailedAssertions()
	{
		return String.valueOf( project.getCounter( CanvasItem.FAILURE_COUNTER ).get() );
	}

	@Override
	public String getTotalNumberOfSamples()
	{
		return String.valueOf( project.getCounter( CanvasItem.SAMPLE_COUNTER ).get() );
	}

	private String getCounterValue( String counter )
	{
		long total = project.getCounter( counter ).get();
		for( ComponentItem tc : project.getComponents() )
		{
			total += tc.getCounter( counter ).get();
		}
		return String.valueOf( total );
	}

}
