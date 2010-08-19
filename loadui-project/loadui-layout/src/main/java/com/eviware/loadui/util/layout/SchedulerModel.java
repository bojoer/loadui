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
package com.eviware.loadui.util.layout;

import java.util.ArrayList;
import java.util.Observable;

/**
 * @author predrag.vucetic
 */
public class SchedulerModel extends Observable {

	private ArrayList<Day> days = new ArrayList<Day>();

	private long time = 0;
	private long duration = 0;
	private long runsCount = 0;

	public ArrayList<Day> getDays() {
		return days;
	}

	public void setOneDay(Day day) {
		days.clear();
		days.add(day);
		setChanged();
	}

	public void setAllDays() {
		days.clear();
		days.add(Day.MONDAY);
		days.add(Day.TUESDAY);
		days.add(Day.WEDNESDAY);
		days.add(Day.THURSDAY);
		days.add(Day.FRIDAY);
		days.add(Day.SATURDAY);
		days.add(Day.SUNDAY);
		setChanged();
	}
	
	public void setDays(ArrayList<Day> days) {
		this.days = days;
		setChanged();
	}

	public long getTime() {
		return time;
	}

	public void setTime(long scheduleTime) {
		this.time = scheduleTime;
		setChanged();
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
		setChanged();
	}

	public long getRunsCount() {
		return runsCount;
	}

	public void setRunsCount(long runsCount) {
		this.runsCount = runsCount;
		setChanged();
	}

	public Boolean[] getDaysAsBoolean(){
		Boolean[] d = new Boolean[7];
		d[0] = days.contains(Day.MONDAY);
		d[1] = days.contains(Day.TUESDAY);
		d[2] = days.contains(Day.WEDNESDAY);
		d[3] = days.contains(Day.THURSDAY);
		d[4] = days.contains(Day.FRIDAY);
		d[5] = days.contains(Day.SATURDAY);
		d[6] = days.contains(Day.SUNDAY);
		return d;
	}
	
	public enum Day {
		SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
	}

}