package com.eviware.loadui.util.reporting.datasources;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

public class ValuesDataSource extends JRAbstractBeanDataSource
{

	Logger logger = LoggerFactory.getLogger(ValuesDataSource.class);

	private Map<String, String> map;
	private int cnt = -1;
	private String[] keys;
	private String[] values;
	
	public ValuesDataSource(Map<String, String> map)
	{
		super(true);
		
		this.map = map;
		keys = map.keySet().toArray(new String[0]);
		values = map.values().toArray(new String[0]);
	}

	@Override
	public void moveFirst() throws JRException
	{
		cnt = 0;
	}

	@Override
	public Object getFieldValue(JRField field) throws JRException
	{
//		logger.debug("Looking for " + field.getName());
		if( field.getName().equals("key"))
			return keys[cnt];
		if( field.getName().equals("value"))
			return values[cnt];
		return null;
	}

	@Override
	public boolean next() throws JRException
	{
		return ++cnt<map.size();
	}

}
