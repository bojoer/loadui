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
package com.eviware.loadui.api.summary;

public class SampleStatsImpl implements SampleStats {

	long timeTaken;
	long time;
	long size;

	public SampleStatsImpl() {
		this.time = -1;
		this.size = -1;
		this.timeTaken = -1;
	}

	public SampleStatsImpl(long time, long size, long timeTaken) {
		this.timeTaken = timeTaken;
		this.time = time;
		this.size = size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.loadui.api.component.categories.SampleStatsInf#getTimeTaken()
	 */
	public long getTimeTaken() {
		return timeTaken;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.loadui.api.component.categories.SampleStatsInf#getTime()
	 */
	public long getTime() {
		return time;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.loadui.api.component.categories.SampleStatsInf#getSize()
	 */
	public long getSize() {
		return size;
	}

	@Override
	public String toString() {
		return time + " - " + timeTaken + " - " + size;
	}

}
