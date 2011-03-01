package com.eviware.loadui.util.reporting.datasources;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.summary.Section;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

public class SectionDataSource extends JRAbstractBeanDataSource
{

	Logger logger = LoggerFactory.getLogger( SectionDataSource.class );
	private List<Section> section;
	private int cnt = -1;

	public SectionDataSource( List<Section> list )
	{
		super( true );
		this.section = list;
	}

	@Override
	public void moveFirst() throws JRException
	{
		cnt = 0;
	}

	@Override
	public Object getFieldValue( JRField field ) throws JRException
	{
		if( field.getName().equals( "values" ) )
			return new ValuesDataSource( section.get( cnt ).getValues() );
		if( field.getName().equals( "tables" ) )
			return new TablesDataSource( section.get( cnt ).getTables() );
		if( field.getName().equals( "title" ) )
		{
			return section.get( cnt ).getTitle();
		}
		return null;
	}

	@Override
	public boolean next() throws JRException
	{
		return ++cnt < section.size();
	}

}
