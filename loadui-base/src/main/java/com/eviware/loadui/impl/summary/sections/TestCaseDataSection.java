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
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.impl.model.SceneItemImpl;
import com.eviware.loadui.impl.summary.MutableSectionImpl;

public class TestCaseDataSection extends MutableSectionImpl implements
		DataSection {

	SceneItemImpl testcase;

	public TestCaseDataSection(SceneItem sceneItem) {
		super("TestCase Data");
		testcase = (SceneItemImpl) sceneItem;
		addValue("Number of components", getNumberOfComponents());
		addValue("Number of connections", getNumberOfConnections());
		addValue("Time Limit", getLimit());
		addValue("Sample Limit", getSampleLimit());
		addValue("Assertion Limit", getAssertionLimit());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.loadui.impl.summary.sections.DataSection#getNumberOfComponents
	 * ()
	 */
	public String getNumberOfComponents() {
		return String.valueOf(testcase.getComponents().size());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.loadui.impl.summary.sections.DataSection#getNumberOfConnections
	 * ()
	 */
	public String getNumberOfConnections() {
		return String.valueOf(testcase.getConnections().size());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.loadui.impl.summary.sections.DataSection#getLimit()
	 */
	public String getLimit() {
		if (testcase.getLimit(CanvasItem.TIMER_COUNTER) > -1) {
			Date time = new Date(testcase.getLimit(CanvasItem.TIMER_COUNTER));
			return new SimpleDateFormat("hh:mm:ss").format(time);
		} else
			return "N/A";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.loadui.impl.summary.sections.DataSection#getSampleLimit()
	 */
	public String getSampleLimit() {
		if (testcase.getLimit(CanvasItem.SAMPLE_COUNTER) > -1)
			return String.valueOf(testcase.getLimit(CanvasItem.SAMPLE_COUNTER));
		else
			return "N/A";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.loadui.impl.summary.sections.DataSection#getAssertionLimit()
	 */
	public String getAssertionLimit() {
		if (testcase.getLimit(CanvasItem.FAILURE_COUNTER) > -1)
			return String.valueOf(testcase
					.getLimit(CanvasItem.FAILURE_COUNTER));
		else
			return "N/A";
	}
}
