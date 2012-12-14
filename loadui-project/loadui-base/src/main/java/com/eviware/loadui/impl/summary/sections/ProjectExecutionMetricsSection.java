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

import java.util.Collection;
import java.util.Map;

import javax.swing.table.TableModel;

import com.eviware.loadui.api.assertion.AssertionAddon;
import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.impl.summary.MutableSectionImpl;
import com.eviware.loadui.impl.summary.sections.tablemodels.AssertionMetricsTableModel;
import com.eviware.loadui.impl.summary.sections.tablemodels.TestCaseSamplerStatisticsTable;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class ProjectExecutionMetricsSection extends MutableSectionImpl
{
	private final Function<CanvasItem, Iterable<? extends AssertionItem<?>>> getAssertions = new Function<CanvasItem, Iterable<? extends AssertionItem<?>>>()
	{
		@Override
		public Collection<? extends AssertionItem<?>> apply( CanvasItem child )
		{
			return child.getAddon( AssertionAddon.class ).getAssertions();
		}
	};

	public ProjectExecutionMetricsSection( ProjectItem project )
	{
		super( "Execution Metrics" );

		addValue( "Assertion Failure Ratio", getFailedAssertions( project ) );
		addValue( "Request Failure Ratio", getFailedRequests( project ) );
		addTable( "Runners", getRunnersMetrics( project ) );

		Iterable<AssertionItem<?>> childAssertions = Iterables.concat( Iterables.transform( project.getChildren(),
				getAssertions ) );

		Iterable<AssertionItem<?>> allAssertions = Iterables.concat( childAssertions,
				project.getAddon( AssertionAddon.class ).getAssertions() );

		addTable( "Assertions", new AssertionMetricsTableModel( allAssertions ) );
	}

	private static String getFailedAssertions( ProjectItem project )
	{
		long failed = project.getCounter( CanvasItem.ASSERTION_FAILURE_COUNTER ).get();
		long total = project.getCounter( CanvasItem.ASSERTION_COUNTER ).get();
		int perc = ( int )( total > 0 ? failed * 100 / total : 0 );

		return perc + "%";
	}

	private static String getFailedRequests( ProjectItem project )
	{
		long failed = project.getCounter( CanvasItem.REQUEST_FAILURE_COUNTER ).get();
		long total = project.getCounter( CanvasItem.REQUEST_COUNTER ).get();
		int perc = ( int )( total > 0 ? failed * 100 / total : 0 );

		return perc + "%";
	}

	private static TableModel getRunnersMetrics( ProjectItem project )
	{
		TestCaseSamplerStatisticsTable table = new TestCaseSamplerStatisticsTable();
		for( SceneItem tc : project.getChildren() )
			for( ComponentItem component : tc.getComponents() )
				if( component.getBehavior() instanceof RunnerCategory )
				{
					Map<String, String> stats = ( ( RunnerCategory )component.getBehavior() ).getStatistics();
					table.add( new TestCaseSamplerStatisticsTable.TestCaseSamplerStatisticsModel( component.getLabel(),
							stats ) );
				}
		for( ComponentItem component : project.getComponents() )
			if( component.getBehavior() instanceof RunnerCategory )
			{
				Map<String, String> stats = ( ( RunnerCategory )component.getBehavior() ).getStatistics();
				table.add( new TestCaseSamplerStatisticsTable.TestCaseSamplerStatisticsModel( component.getLabel(), stats ) );
			}
		return table;
	}

}
