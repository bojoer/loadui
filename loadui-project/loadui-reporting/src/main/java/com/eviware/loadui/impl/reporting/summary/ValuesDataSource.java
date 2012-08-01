/*
 * Copyright 2011 SmartBear Software
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
package com.eviware.loadui.impl.reporting.summary;

import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValuesDataSource extends JRAbstractBeanDataSource
{
	protected static final Logger log = LoggerFactory.getLogger( ValuesDataSource.class );

	private final Map<String, String> map;
	private int cnt = -1;
	private final String[] keys;
	private final String[] values;

	public ValuesDataSource( Map<String, String> map )
	{
		super( true );

		this.map = map;
		keys = map.keySet().toArray( new String[0] );
		values = map.values().toArray( new String[0] );
	}

	@Override
	public void moveFirst() throws JRException
	{
		cnt = 0;
	}

	@Override
	public Object getFieldValue( JRField field ) throws JRException
	{
		// logger.debug("Looking for " + field.getName());
		if( field.getName().equals( "key" ) )
			return keys[cnt];
		if( field.getName().equals( "value" ) )
			return values[cnt];
		return null;
	}

	@Override
	public boolean next() throws JRException
	{
		return ++cnt < map.size();
	}

}
