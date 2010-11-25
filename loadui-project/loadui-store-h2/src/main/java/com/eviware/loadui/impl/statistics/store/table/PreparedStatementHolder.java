package com.eviware.loadui.impl.statistics.store.table;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.eviware.loadui.impl.statistics.store.util.JDBCUtil1;

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
		JDBCUtil1.close( preparedStatement );
		preparedStatement = null;
		statementHolder = null;
	}
}
