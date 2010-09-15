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
package com.eviware.loadui.api.chart;

import java.util.Calendar;
import java.util.Date;

public class CustomTimeRange extends CustomAbstractRange {

	//axis visible range in milliseconds
	private long period;
	
	//axis refresh rate in milliseconds
	private long rate;
	
	private long low;

	private long high;

	public CustomTimeRange(long period, long rate) {
		super();
		long now = System.currentTimeMillis();
		this.low = now;
		this.high = now + period;
		this.period = period;
		this.rate = rate;
	}
	
	public CustomTimeRange(long low, long high, long period, long rate) {
		super();
		this.low = low;
		this.high = high;
		this.period = period;
		this.rate = rate;
	}

	public CustomTimeRange(Date low, Date high, long period, long rate) {
		super();
		this.low = low.getTime();
		this.high = high.getTime();
		this.period = period;
		this.rate = rate;
	}

	public CustomTimeRange(Calendar low, Calendar high, long period, long rate) {
		super();
		this.low = low.getTimeInMillis();
		this.high = high.getTimeInMillis();
		this.period = period;
		this.rate = rate;
	}

	public long getLow() {
		return low;
	}

	public long getHigh() {
		return high;
	}

	public long getPeriod() {
		return period;
	}

	public void setPeriod(long period) {
		this.period = period;
	}

	public void setLow(long low) {
		this.low = low;
	}

	public void setHigh(long high) {
		this.high = high;
	}

	public long getRate() {
		return rate;
	}

	public void setRate(long rate) {
		this.rate = rate;
	}
}
