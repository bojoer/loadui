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

/**
 * Mutable version of a StatisticVariable which is used to provide data to its
 * writers.
 * 
 * @author dain.nilsson
 */
public interface MutableStatisticVariable extends StatisticVariable
{
	/**
	 * Updates the MutableStatisticVariable with new data, which will be passed
	 * to the attached StatisticsWriters.
	 * 
	 * @param timestamp
	 * @param value
	 */
	public void update( long timestamp, Number value );
}
