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

import com.eviware.loadui.api.model.OrderedCollection;

/**
 * Holds a number of StatisticPages, and allows creation, and reordering of
 * these.
 * 
 * @author dain.nilsson
 */
public interface StatisticPages extends OrderedCollection<StatisticPage>
{
	/**
	 * Creates and returns a new StatisticPage with the given title, placing it
	 * at the end of the existing StatisticPages.
	 * 
	 * @param title
	 * @return
	 */
	public StatisticPage createPage( String title );

	/**
	 * Moved a contained StatisticPage to the given index.
	 * 
	 * @param page
	 * @param index
	 */
	public void movePage( StatisticPage page, int index );
}
