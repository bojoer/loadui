package com.eviware.loadui.util.reporting.datasources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.summary.Chapter;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

public class ChapterDataSource extends JRAbstractBeanDataSource
{
	Logger logger = LoggerFactory.getLogger(ChapterDataSource.class);

	private Chapter chapter = null;

	private int cnt = -1;

	public ChapterDataSource(Chapter chapter)
	{
		super(true);
		this.chapter = chapter;
	}

	@Override
	public void moveFirst() throws JRException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object getFieldValue(JRField field) throws JRException
	{
		logger.debug("Looking for " + field.getName());
		if ( field.getName().equals("values") ) {
			return new ValuesDataSource(chapter.getValues());
		}
		if ( field.getName().equals("sections") ) {
			return new SectionDataSource(chapter.getSections());
		}
		return getFieldValue(chapter, field);
	}

	@Override
	public boolean next() throws JRException
	{
		cnt++;
		if (cnt == 1)
			return false;
		else
			return true;
	}

}
