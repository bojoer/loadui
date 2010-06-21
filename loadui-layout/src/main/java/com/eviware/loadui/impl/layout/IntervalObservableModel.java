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
package com.eviware.loadui.impl.layout;

import java.util.Observable;

import com.eviware.loadui.api.scheduler.Interval;

public class IntervalObservableModel extends Observable implements Interval {

	private Long duration = 0L;
	
	private Boolean running = false;

	private long interval;

	private long startAt;

	private String error;
	
	@Override
	public void reset() {
		
	}

	@Override
	public boolean setDuration(long duration) {
		if( ! running )
			this.duration = duration;
		return true;
	}

	@Override
	public void setErrorMessage(String error) {
		this.error = error;
	}

	@Override
	public boolean setInterval(long interval) {
		this.interval = interval;
		return false;
	}

	@Override
	public boolean setStart(long start) {
		this.startAt = start;
		return false;
	}

	@Override
	public void start() {
		this.running = true;
	}

	@Override
	public void stop() {
		this.running = false;
	}
	
	public void update() {
		setChanged();
		notifyObservers();
	}

	@Override
	public Long getDuration() {
		return this.duration;
	}

	@Override
	public Long getInterval() {
		return this.interval;
	}

	@Override
	public Long getStart() {
		return this.startAt;
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}
}
