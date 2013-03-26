/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.api.chart;

@Deprecated
public class ChartSerie
{

	private int index;

	private String name;

	private boolean enabled = true;

	private boolean defaultAxis = true;

	public ChartSerie( String name, boolean enabled )
	{
		this.name = name;
		this.enabled = enabled;
	}

	public ChartSerie( String name, boolean enabled, boolean defaultAxis )
	{
		this.name = name;
		this.enabled = enabled;
		this.defaultAxis = defaultAxis;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled( boolean enabled )
	{
		this.enabled = enabled;
	}

	public int getIndex()
	{
		return index;
	}

	public void setIndex( int index )
	{
		this.index = index;
	}

	public boolean isDefaultAxis()
	{
		return defaultAxis;
	}

}
