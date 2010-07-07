package com.eviware.loadui.util.reporting.datasources;

import com.eviware.loadui.api.summary.Section;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

public class SummarySectionDataSource extends JRAbstractBeanDataSource
{

	private Section section;
	private int cnt = -1;
	
	public SummarySectionDataSource(Section section)
	{
		super(true);
		this.section = section;
	}

	@Override
	public void moveFirst() throws JRException
	{
		cnt = 0;
	}

	@Override
	public Object getFieldValue(JRField field) throws JRException
	{
		if (field.getName().equals("time"))
			return section.getValues().get("time");
		if (field.getName().equals("samples"))
			return section.getValues().get("samples");
		if (field.getName().equals("afailures"))
			return section.getValues().get("assertion failures");
		if (field.getName().equals("status"))
			return section.getValues().get("status");
		
		return null;
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
