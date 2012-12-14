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
package com.eviware.loadui.api.statistics.model.chart;

import com.eviware.loadui.api.model.AttributeHolder;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.traits.Labeled;

/**
 * Provides a model for a specific type of Chart.
 * 
 * @author dain.nilsson
 */
public interface ChartView extends AttributeHolder, Labeled
{
	/**
	 * Get the ChartGroup which this ChartView belongs to.
	 * 
	 * @return
	 */
	public ChartGroup getChartGroup();
}
