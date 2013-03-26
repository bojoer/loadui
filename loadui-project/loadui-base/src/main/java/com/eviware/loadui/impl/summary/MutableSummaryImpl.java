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
package com.eviware.loadui.impl.summary;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.eviware.loadui.api.summary.Chapter;
import com.eviware.loadui.api.summary.MutableChapter;
import com.eviware.loadui.api.summary.MutableSummary;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class MutableSummaryImpl implements MutableSummary
{
	private final HashMap<String, MutableChapter> chapters = Maps.newLinkedHashMap();
	private final Date startTime;
	private final Date endTime;

	public MutableSummaryImpl( Date startTime, Date endTime )
	{
		this.startTime = new Date( startTime.getTime() );
		this.endTime = new Date( endTime.getTime() );
	}

	@Override
	public MutableChapter addChapter( String name )
	{
		MutableChapter chapter = new MutableChapterImpl( name );
		chapters.put( name, chapter );
		return chapter;
	}

	@Override
	public MutableChapter getChapter( String title )
	{
		return chapters.get( title );
	}

	@Override
	public Map<String, Chapter> getChapters()
	{
		return ImmutableMap.<String, Chapter> copyOf( chapters );
	}

	@Override
	public Date getStartTime()
	{
		return new Date( startTime.getTime() );
	}

	@Override
	public Date getEndTime()
	{
		return new Date( endTime.getTime() );
	}
}
