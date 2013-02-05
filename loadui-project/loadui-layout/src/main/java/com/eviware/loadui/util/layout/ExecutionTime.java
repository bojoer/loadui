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
package com.eviware.loadui.util.layout;

import java.util.Calendar;

public class ExecutionTime {

	private int day = 1;
	private int hour = 0;
	private int minute = 0;
	private int second = 0;

	public ExecutionTime() {
		Calendar c = Calendar.getInstance();
		int cDay = c.get(Calendar.DAY_OF_WEEK);
		if (cDay == 1) {
			cDay = 8;
		}
		cDay--;
		this.day = cDay;
		this.hour = c.get(Calendar.HOUR_OF_DAY);
		this.minute = c.get(Calendar.MINUTE);
		this.second = c.get(Calendar.SECOND);
	}

	public ExecutionTime(int day, int hour, int minute, int second) {
		super();
		this.day = day;
		this.hour = hour;
		this.minute = minute;
		this.second = second;
	}

	public int getDay() {
		return day;
	}

	public int getHour() {
		return hour;
	}

	public int getMinute() {
		return minute;
	}

	public int getTime() {
		return (((day * 24 + hour) * 60 + minute) * 60 + second) * 1000;
	}

	@Override
	public String toString() {
		return day + " " + hour + " " + minute + " " + second;
	}
}