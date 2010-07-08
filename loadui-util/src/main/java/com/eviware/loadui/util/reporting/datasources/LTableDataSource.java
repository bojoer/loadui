package com.eviware.loadui.util.reporting.datasources;

import java.util.ArrayList;

import javax.swing.table.TableModel;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;

public class LTableDataSource extends JRTableModelDataSource
{

	private TableModel model = null;
	
	public LTableDataSource(TableModel model)
	{
		super(model);
		this.model = model;
	}

	@Override
	public Object getFieldValue(JRField jrField) throws JRException
	{
		if( jrField.getName().equals("columns")) {
			ArrayList<String> result = new ArrayList<String>();
			for( int i = 0; i < model.getColumnCount(); i++ ) {
				result.add(model.getColumnName(i));
			}
			return result;
		}
		if (jrField.getName().startsWith("COLUMN_"))
		{
			int index = Integer.parseInt(jrField.getName().substring(7));
			if ( index < model.getColumnCount() )
				return super.getFieldValue(jrField);
		}
		return null;
	}
}
