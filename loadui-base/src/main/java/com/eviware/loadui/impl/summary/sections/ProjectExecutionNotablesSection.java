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

import java.util.ArrayList;

import javax.swing.table.TableModel;

import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.summary.SampleStats;
import com.eviware.loadui.impl.model.ProjectItemImpl;
import com.eviware.loadui.impl.summary.MutableSectionImpl;
import com.eviware.loadui.impl.summary.sections.tablemodels.TestCaseTopSamplesTable;

public class ProjectExecutionNotablesSection extends MutableSectionImpl implements ExecutionNotablesSection
{

	ProjectItemImpl project;

	public ProjectExecutionNotablesSection( ProjectItemImpl projectItemImpl )
	{
		super( "Execution Notables" );
		project = projectItemImpl;
		addTable( "Top 5 Samples", getTop5Samples() );
		addTable( "Bottom 5 Samples", getBottom5Samples() );
	}

	@Override
	public synchronized TableModel getBottom5Samples()
	{
		TestCaseTopSamplesTable table = new TestCaseTopSamplesTable();

		for( SceneItem tc : project.getScenes() )
			for( ComponentItem component : tc.getComponents() )
				if( component.getBehavior() instanceof RunnerCategory )
				{
					for( SampleStats stat : ( ( RunnerCategory )component.getBehavior() ).getBottomSamples() )
						table.addBottom( component.getLabel(), stat );
				}

		for( ComponentItem component : project.getComponents() )
			if( component.getBehavior() instanceof RunnerCategory )
			{
				for( SampleStats stat : ( ( RunnerCategory )component.getBehavior() ).getBottomSamples() )
					table.addBottom( component.getLabel(), stat );
			}

		return table;
	}

	@Override
	public synchronized TableModel getTop5Samples()
	{
		TestCaseTopSamplesTable table = new TestCaseTopSamplesTable();
		for( SceneItem tc : project.getScenes() )
			for( ComponentItem component : tc.getComponents() )
			{

				if( component.getBehavior() instanceof RunnerCategory )
				{
					for( SampleStats stat : ( ( RunnerCategory )component.getBehavior() ).getTopSamples() )
						table.addTop( component.getLabel(), stat );
				}
			}

		for( ComponentItem component : project.getComponents() )
		{
			if( component.getBehavior() instanceof RunnerCategory )
			{
				for( SampleStats stat : ( ( RunnerCategory )component.getBehavior() ).getTopSamples() )
					table.addTop( component.getLabel(), stat );
			}
		}
		return table;
	}

}
