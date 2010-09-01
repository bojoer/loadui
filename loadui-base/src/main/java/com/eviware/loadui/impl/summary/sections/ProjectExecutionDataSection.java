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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.table.TableModel;

import com.eviware.loadui.api.component.categories.AnalysisCategory;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.impl.model.ProjectItemImpl;
import com.eviware.loadui.impl.model.SceneItemImpl;
import com.eviware.loadui.impl.summary.MutableSectionImpl;
import com.eviware.loadui.impl.summary.sections.tablemodels.TestCaseDataTableModel;

public class ProjectExecutionDataSection extends MutableSectionImpl implements ExecutionDataSection
{

	private static final long HOUR = 3600000L;
	ProjectItemImpl project;
	private SimpleDateFormat dateFormat = new SimpleDateFormat( "hh:mm:ss" );

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
		return project.getEndTime() != null ? dateFormat.format( project.getEndTime() ) : "N/A";
	}

	@Override
	public String getExecutionTime()
	{
		SimpleDateFormat dateFormat;
		if( project.getStartTime() != null )
		{
			Calendar end = Calendar.getInstance();
			end.setTime(project.getEndTime());
			
			Calendar start = Calendar.getInstance();
			start.setTime(project.getStartTime());
			
			end.add(Calendar.YEAR, -start.get(Calendar.YEAR));
			end.add(Calendar.MONTH, -start.get(Calendar.MONTH));
			end.add(Calendar.DATE, -start.get(Calendar.DATE));
			end.add(Calendar.HOUR, -start.get(Calendar.HOUR));
			end.add(Calendar.MINUTE, -start.get(Calendar.MINUTE));
			end.add(Calendar.SECOND, -start.get(Calendar.SECOND));
			end.set(Calendar.MILLISECOND, 0);
			
			Date dd = end.getTime();
			if( project.getEndTime().getTime() - project.getStartTime().getTime() < HOUR )
				dateFormat = new SimpleDateFormat( "00:mm:ss" );
			else
				dateFormat = new SimpleDateFormat( "hh:mm:ss" );
			return dateFormat.format( dd );
		}
		else
		{
			return "N/A";
		}
	}

	@Override
	public String getStartTime()
	{	
		return project.getStartTime() != null ? dateFormat.format( project.getStartTime() ) : "N/A";
	}

	@Override
	public String getTotalNumberOfAssertions()
	{
		int cnt = 0;
		for( ComponentItem component : project.getComponents() )
		{
			if( component.getType().equalsIgnoreCase( "assertion" ) & component.getBehavior() instanceof AnalysisCategory )
				cnt++ ;
		}
		for( SceneItem scene : project.getScenes() )
			for( ComponentItem component : scene.getComponents() )
			{
				if( component.getType().equalsIgnoreCase( "assertion" )
						& component.getBehavior() instanceof AnalysisCategory )
					cnt++ ;
			}
		return String.valueOf( cnt );
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
