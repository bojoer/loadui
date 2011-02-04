/*
 * Copyright 2011 eviware software ab
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

import com.eviware.loadui.api.model.OrderedCollection;
import com.eviware.loadui.api.model.Releasable;

/**
 * A Page holding a number of ChartGroups. Allows creation and reordering of the
 * contained ChartGroups.
 * 
 * @author dain.nilsson
 */
public interface StatisticPage extends OrderedCollection<ChartGroup>, Releasable
{
	// BaseEvent key fired when the title of the StatisticPage changes.
	public static final String TITLE = StatisticPage.class.getName() + "@title";

	/**
	 * Gets the title of the StatisticPage.
	 * 
	 * @return
	 */
	public String getTitle();

	/**
	 * Sets the title of the StatisticPage.
	 * 
	 * @param title
	 */
	public void setTitle( String title );

	/**
	 * Creates and returns a new ChartGroup of the given type, with the given
	 * title, placing it at the end of the existing ChartGroups.
	 * 
	 * @param title
	 * @param type
	 * @return
	 */
	public ChartGroup createChartGroup( String type, String title );

	/**
	 * Moved a contained ChartGroup to the given index.
	 * 
	 * @param chartGroup
	 * @param index
	 */
	public void moveChartGroup( ChartGroup chartGroup, int index );

	/**
	 * Deletes the StatisticPage.
	 */
	public void delete();
}
