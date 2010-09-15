package com.eviware.loadui.util.reporting.datasources;

import java.util.Iterator;

import com.eviware.loadui.api.summary.Summary;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

public class SummaryDataSource extends JRAbstractBeanDataSource
{

	private Summary summary;
	private Iterator<String> iterator;

	public SummaryDataSource(Summary summary)
	{
		super(true);
		this.summary = summary;
		iterator = summary.getChapters().keySet().iterator();
	}

	@Override
	public void moveFirst() throws JRException
	{
		iterator = summary.getChapters().keySet().iterator();
	}

	@Override
	public Object getFieldValue(JRField field) throws JRException
	{
		if (field.getName().equals("chapter"))
			return new ChapterDataSource(summary.getChapters().get(iterator.next()));
		return null;
	}

	@Override
	public boolean next() throws JRException
	{
		return iterator.hasNext();
	}
}
