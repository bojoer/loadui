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
import java.util.Date;

import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.impl.model.ProjectItemImpl;
import com.eviware.loadui.impl.summary.MutableSectionImpl;

public class ProjectDataSummarySection extends MutableSectionImpl implements
		DataSummarySection {

	private static final long HOUR = 3600000L;
	private ProjectItemImpl project;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");

	public ProjectDataSummarySection(ProjectItemImpl projectItemImpl) {
		super(projectItemImpl.getLabel());

		project = projectItemImpl;

		addValue("time", getTime());
		addValue("samples", getNumberOfSamples());
		addValue("assertion failures", getNumberOfFailures());
		addValue("status", getStatus());
	}

	@Override
	public String getNumberOfFailures() {
		return String.valueOf(project.getCounter(CanvasItem.FAILURE_COUNTER)
				.get());
	}

	@Override
	public String getStatus() {
		if (project.getLimit(CanvasItem.FAILURE_COUNTER) == -1 || 
			project.getCounter(CanvasItem.FAILURE_COUNTER).get() < project
			.getLimit(CanvasItem.FAILURE_COUNTER)) {
			return "Passed";
		} else
			return "Failed";
	}

	@Override
	public String getTime() {
		if (project.getStartTime() != null) {
			Date time = new Date(project.getEndTime().getTime()
					- project.getStartTime().getTime());
			if (new Date().getTime() - project.getStartTime().getTime() < HOUR)
				dateFormat = new SimpleDateFormat("00:mm:ss");
			else
				dateFormat = new SimpleDateFormat("hh:mm:ss");
			return dateFormat.format(time);
		} else {
			return "N/A";
		}
	}

	@Override
	public String getNumberOfSamples() {
		return String.valueOf(project.getCounter(CanvasItem.SAMPLE_COUNTER)
				.get());
	}

}
