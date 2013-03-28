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

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.eviware.loadui.api.summary.MutableChapter;
import com.eviware.loadui.api.summary.MutableSection;
import com.eviware.loadui.api.summary.Section;

public class MutableChapterImpl implements MutableChapter
{

	private String title;
	private final LinkedHashMap<String, String> values = new LinkedHashMap<>();
	private Date date;
	private String decription;
	private final LinkedHashMap<String, MutableSection> sections = new LinkedHashMap<>();

	public MutableChapterImpl( String title )
	{
		this.title = title;
		this.date = new Date();
	}

	@Override
	public MutableSection addSection( String name )
	{
		MutableSection section = new MutableSectionImpl( name );
		sections.put( name, section );
		return section;
	}

	@Override
	public void addValue( String name, String value )
	{
		values.put( name, value );
	}

	@Override
	public void setDate( Date date )
	{
		this.date = new Date( date.getTime() );
	}

	@Override
	public void setDescription( String description )
	{
		this.decription = description;
	}

	@Override
	public void setTitle( String title )
	{
		this.title = title;
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	@Override
	public MutableSection getSection( String name )
	{
		return sections.get( name );
	}

	@Override
	public Date getDate()
	{
		return new Date( date.getTime() );
	}

	@Override
	public String getDescription()
	{
		return decription;
	}

	@Override
	public List<Section> getSections()
	{
		return new ArrayList<Section>( sections.values() );
	}

	@Override
	public Map<String, String> getValues()
	{
		return values;
	}

	public void addSection( @Nonnull MutableSectionImpl section )
	{
		sections.put( section.getTitle(), section );
	}
}
