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
package com.eviware.loadui.api.statistics.model;

import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.model.AttributeHolder;
import com.eviware.loadui.api.statistics.StatisticHolder;

/**
 * A Chart of a specific type, displaying data from a specific source of a
 * StatisticHolder.
 * 
 * @author dain.nilsson
 */
public interface Chart extends AttributeHolder, EventFirer
{
	/**
	 * Gets the StatisticHolder for which the Chart displays data.
	 * 
	 * @return
	 */
	public StatisticHolder getStatisticHolder();

	/**
	 * Gets the parent ChartGroup.
	 * 
	 * @return
	 */
	public ChartGroup getChartGroup();

	/**
	 * Deletes the Chart.
	 */
	public void delete();
}
