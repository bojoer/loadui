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
package com.eviware.loadui.impl.summary;

import java.util.LinkedHashMap;
import java.util.Map;

import com.eviware.loadui.api.summary.Chapter;
import com.eviware.loadui.api.summary.MutableChapter;
import com.eviware.loadui.api.summary.MutableSummary;

public class MutableSummaryImpl implements MutableSummary
{

	private LinkedHashMap<String, Chapter> chapters = new LinkedHashMap<String, Chapter>();

	@Override
	public MutableChapter addChapter( String name )
	{
		MutableChapter chapter = new MutableChapterImpl( name );
		chapters.put(name, chapter);
		return chapter;
	}

	@Override
	public MutableChapter getChapter( String title )
	{
		return (MutableChapter)chapters.get(title);
	}

	@Override
	public Map<String, Chapter> getChapters() {
		return chapters;
	}

}
