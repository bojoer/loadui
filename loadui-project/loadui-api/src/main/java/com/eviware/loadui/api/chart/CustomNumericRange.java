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

@Deprecated
public class CustomNumericRange extends CustomAbstractRange
{

	private double extraSpace;

	private double low;

	private double high;

	private double scale = 1.0;

	public CustomNumericRange( double low, double high, double extraSpace )
	{
		super();
		this.low = low;
		this.high = high;
		this.extraSpace = extraSpace;
	}

	public double getLow()
	{
		return low;
	}

	public double getHigh()
	{
		return high;
	}

	public double getExtraSpace()
	{
		return extraSpace;
	}

	public void setExtraSpace( double extraSpace )
	{
		this.extraSpace = extraSpace;
	}

	public void setLow( double low )
	{
		this.low = low;
	}

	public void setHigh( double high )
	{
		this.high = high;
	}

	public double getScale()
	{
		return scale;
	}

	public void setScale( double scale )
	{
		this.scale = scale;
	}

}
