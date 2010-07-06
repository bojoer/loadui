package com.eviware.loadui.util.reporting.datasources;

import java.util.Map;

import javax.swing.table.TableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;

public class TablesDataSource extends JRAbstractBeanDataSource
{

	Logger logger = LoggerFactory.getLogger(TablesDataSource.class);
	
	private Map<String, TableModel> map;
	private int cnt = -1;
	private String[] keys;
	private TableModel[] tables;
	
	public TablesDataSource(Map<String, TableModel> map)
	{
		super(true);
		this.map = map;
		this.keys = map.keySet().toArray(new String[0]);
		this.tables = map.values().toArray(new TableModel[0]);
	}

	@Override
	public void moveFirst() throws JRException
	{
		cnt = 0;
	}

	@Override
	public Object getFieldValue(JRField field) throws JRException
	{
		logger.debug("Looking for field: " + field.getName());
		if (field.getName().equals("title")) 
			return keys[cnt];
		if (field.getName().equals("table") )
			return new JRTableModelDataSource(tables[cnt]);
		return null;
	}

	@Override
	public boolean next() throws JRException
	{
		return ++cnt < map.size();
	}

}
