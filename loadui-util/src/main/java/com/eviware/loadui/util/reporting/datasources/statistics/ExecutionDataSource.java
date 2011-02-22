package com.eviware.loadui.util.reporting.datasources.statistics;

import com.eviware.loadui.api.statistics.store.Execution;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

public class ExecutionDataSource extends JRAbstractBeanDataSource
{
	private final Execution execution;
	private boolean next = true;

	public ExecutionDataSource( Execution execution )
	{
		super( true );
		this.execution = execution;
	}

	@Override
	public void moveFirst() throws JRException
	{
		next = true;
	}

	@Override
	public Object getFieldValue( JRField arg0 ) throws JRException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean next() throws JRException
	{
		if( next )
		{
			next = false;
			return true;
		}
		return false;
	}

}
