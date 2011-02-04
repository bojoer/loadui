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
package com.eviware.loadui.api.chart;

public interface ChartListener {

	/**
	 * Fires when new point has been added to a specific serie of a chart.
	 * 
	 * @param cs Chart serie to which the point has been added to.
	 * @param p Point that has been added to chart serie.
	 */
	public void pointAddedToModel(ChartSerie cs, Point p);

	/**
	 * Fires when chart serie has been cleared
	 * 
	 * @param cs
	 *            Chart serie that has been cleared
	 */
	public void serieCleared(ChartSerie cs);

	/**
	 * Fires after all chart series have been cleared
	 */
	public void chartCleared();

	/**
	 * Fires when chart serie has been enabled or disabled. To check is it is
	 * enabled or disabled use <code>cs.isEnabled()</code> method.
	 * 
	 * @param cs
	 *            Chart serie that has been enabled or disabled
	 */
	public void serieEnabled(ChartSerie cs);
	
	/**
	 * Fires when test starts or stops.
	 * 
	 * @param running
	 *            true if test is running, false otherwise.
	 */
	public void testStateChanged(boolean running);

}
