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

import java.util.Map;

import javax.swing.table.TableModel;

import com.eviware.loadui.api.component.categories.AnalysisCategory;
import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.impl.model.ProjectItemImpl;
import com.eviware.loadui.impl.summary.MutableSectionImpl;
import com.eviware.loadui.impl.summary.sections.tablemodels.TestCaseAssertionMetricsTableModel;
import com.eviware.loadui.impl.summary.sections.tablemodels.TestCaseSapmlerStatisticsTable;

public class ProjectExecutionMetricsSection extends MutableSectionImpl implements ExecutionMetricsSection
{

	ProjectItemImpl project;

	public ProjectExecutionMetricsSection( ProjectItemImpl projectItemImpl )
	{
		super( "Execution Metrics" );

		project = projectItemImpl;

		addValue( "Failure Ratio", getFailedAssertions() );
		addTable( "Runners", getRunnersMetrics() );
		addTable( "Assertions", getAssertionsMetrics() );
	}

	@Override
	public TableModel getAssertionsMetrics()
	{
		TestCaseAssertionMetricsTableModel table = new TestCaseAssertionMetricsTableModel();
		for( SceneItem testcase : project.getScenes() )
			for( ComponentItem component : testcase.getComponents() )
				if( component.getType().equals( "Assertion" ) )
					table.add( table.new AssertionMetricsModel( component ) );
		for( ComponentItem component : project.getComponents() )
		{
			if( component.getType().equals( "Assertion" ) )
				table.add( table.new AssertionMetricsModel( component ) );
		}
		return table;
	}

	@Override
	public String getFailedAssertions()
	{
		long failed = project.getCounter( CanvasItem.FAILURE_COUNTER ).get();
		long total = getTotalNumberOfAssertions();
		int perc = ( int )( total > 0 ? failed * 100 / total : 0 );
		return perc + "%"; //failed + " / " + total + " (" + perc + " %)";
	}
	
	private long getTotalNumberOfAssertions()
	{
		int cnt = 0;
		for( ComponentItem component : project.getComponents() )
		{
			if( component.getType().equalsIgnoreCase( "assertion" ) & component.getBehavior() instanceof AnalysisCategory )
				cnt += component.getCounter( CanvasItem.ASSERTION_COUNTER ).get() ;
		}
		for( SceneItem scene : project.getScenes() )
			for( ComponentItem component : scene.getComponents() )
			{
				if( component.getType().equalsIgnoreCase( "assertion" )
						& component.getBehavior() instanceof AnalysisCategory )
					cnt += component.getCounter( CanvasItem.ASSERTION_COUNTER ).get() ;
			}
		return  project.getCounter( CanvasItem.ASSERTION_COUNTER ).get() + cnt;
	}

	@Override
	public TableModel getRunnersMetrics()
	{
		TestCaseSapmlerStatisticsTable table = new TestCaseSapmlerStatisticsTable();
		for( SceneItem tc : project.getScenes() )
			for( ComponentItem component : tc.getComponents() )
				if( component.getBehavior() instanceof RunnerCategory )
				{
					Map<String, String> stats = ( ( RunnerCategory )component.getBehavior() ).getStatistics();
					table.add( table.new TestCaseSamplerStatisticsModel( component.getLabel(), stats ) );
				}
		for( ComponentItem component : project.getComponents() )
			if( component.getBehavior() instanceof RunnerCategory )
			{
				Map<String, String> stats = ( ( RunnerCategory )component.getBehavior() ).getStatistics();
				table.add( table.new TestCaseSamplerStatisticsModel( component.getLabel(), stats ) );
			}
		return table;
	}

}