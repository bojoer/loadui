/*
 * Copyright 2011 eviware software ab
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
package com.eviware.loadui.impl.statistics.store.table;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.eviware.loadui.impl.statistics.store.util.JdbcUtil;

public class PreparedStatementHolder
{
	private PreparedStatement preparedStatement;

	private StatementHolder statementHolder;

	public PreparedStatementHolder( PreparedStatement preparedStatement, StatementHolder statementHolder )
	{
		this.preparedStatement = preparedStatement;
		this.statementHolder = statementHolder;
	}

	public PreparedStatement getPreparedStatement()
	{
		return preparedStatement;
	}

	public void setArguments( Map<String, ? extends Object> data ) throws SQLException
	{
		for( int i = 0; i < statementHolder.getArgumentNameList().size(); i++ )
		{
			setArgument( i + 1, data.get( statementHolder.getArgumentNameList().get( i ) ) );
		}
	}

	public void setArgument( int index, Object value ) throws SQLException
	{
		preparedStatement.setObject( index, value );
	}

	public void executeUpdate() throws SQLException
	{
		preparedStatement.executeUpdate();
	}

	public ResultSet executeQuery() throws SQLException
	{
		return preparedStatement.executeQuery();
	}

	public void dispose()
	{
		JdbcUtil.close( preparedStatement );
		preparedStatement = null;
		statementHolder = null;
	}
}
