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
package com.eviware.loadui.api.chart;

/**
 * Empty implementation of <code>ChartListener</code> interface.
 * 
 * @author predrag.vucetic
 * 
 */
public class ChartAdapter implements ChartListener
{

	@Override
	public void pointAddedToModel( ChartSerie cs, Point p )
	{
	}

	@Override
	public void serieCleared( ChartSerie cs )
	{
	}

	@Override
	public void serieEnabled( ChartSerie cs )
	{
	}

	@Override
	public void chartCleared()
	{
	}

	@Override
	public void testStateChanged( boolean running )
	{
	}

}
