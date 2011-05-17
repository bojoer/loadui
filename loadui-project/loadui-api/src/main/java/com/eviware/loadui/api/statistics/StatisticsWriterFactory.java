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
package com.eviware.loadui.api.statistics;

import java.util.Map;

/**
 * Factory for instantiating a specific type of StatisticsWriter. Any
 * implementation of this should be exposed as an OSGi service to be picked up
 * by the framework. Type names should be unique.
 * 
 * @author dain.nilsson
 */
public interface StatisticsWriterFactory
{
	/**
	 * Gets the type String of the StatisticsWriterFactory.
	 * 
	 * @return
	 */
	public String getType();

	/**
	 * Creates a StatisticsWriter of the factory's type, for the given
	 * StatisticVariable.
	 * 
	 * @param statisticsManager
	 * @param variable
	 * @param config
	 *           A Map containing configuration options for the StatisticWriter.
	 *           The contents of this map differs from Writer to Writer. An empty
	 *           map should always be an acceptable configuration.
	 * @return
	 */
	public StatisticsWriter createStatisticsWriter( StatisticsManager statisticsManager, StatisticVariable variable,
			Map<String, Object> config );
}
