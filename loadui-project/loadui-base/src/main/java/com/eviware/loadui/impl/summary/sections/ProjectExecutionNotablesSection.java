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

import java.util.List;

import javax.swing.table.TableModel;

import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.summary.SampleStats;
import com.eviware.loadui.impl.model.ProjectItemImpl;
import com.eviware.loadui.impl.summary.MutableSectionImpl;
import com.eviware.loadui.impl.summary.sections.tablemodels.TestCaseTopSamplesTable;

public class ProjectExecutionNotablesSection extends MutableSectionImpl
{
	ProjectItemImpl project;

	public ProjectExecutionNotablesSection( ProjectItemImpl projectItemImpl )
	{
		super( "Execution Notables" );
		project = projectItemImpl;
		addTable( "Top 5 Requests", get5MostExtremeSamples( true ) );
		addTable( "Bottom 5 Requests", get5MostExtremeSamples( false ) );
	}

	public synchronized final TableModel get5MostExtremeSamples( boolean getTopSamples )
	{
		TestCaseTopSamplesTable table = new TestCaseTopSamplesTable();

		for( SceneItem tc : project.getChildren() )
			for( ComponentItem component : tc.getComponents() )
				if( component.getBehavior() instanceof RunnerCategory )
				{
					RunnerCategory runnerCat = ( RunnerCategory )component.getBehavior();
					List<SampleStats> sampleStatsList = getTopSamples ? runnerCat.getTopSamples() : runnerCat
							.getBottomSamples();
					for( SampleStats stat : sampleStatsList )
						table.add( component.getLabel(), stat, getTopSamples );
				}

		for( ComponentItem component : project.getComponents() )
			if( component.getBehavior() instanceof RunnerCategory )
			{
				RunnerCategory runnerCat = ( RunnerCategory )component.getBehavior();
				List<SampleStats> sampleStatsList = getTopSamples ? runnerCat.getTopSamples() : runnerCat
						.getBottomSamples();
				for( SampleStats stat : sampleStatsList )
					table.add( component.getLabel(), stat, getTopSamples );
			}
		table.finalizeOrdering( getTopSamples );
		return table;
	}
}
