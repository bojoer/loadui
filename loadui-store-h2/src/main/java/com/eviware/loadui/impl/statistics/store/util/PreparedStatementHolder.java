package com.eviware.loadui.impl.statistics.store.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

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

	public void setArguments( Number timestamp, Map<String, ? extends Object> data ) throws SQLException
	{
		setArgument( 1, timestamp );
		for( int i = 0; i < statementHolder.getArgumentNameList().size(); i++ )
		{
			setArgument( i + 2, data.get( statementHolder.getArgumentNameList().get( i ) ) );
		}
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
		if( value instanceof Long )
		{
			preparedStatement.setLong( index, ( ( Long )value ).longValue() );
		}
		else if( value instanceof Integer )
		{
			preparedStatement.setInt( index, ( ( Integer )value ).intValue() );
		}
		else if( value instanceof Double )
		{
			preparedStatement.setDouble( index, ( ( Double )value ).doubleValue() );
		}
		else if( value instanceof String )
		{
			preparedStatement.setString( index, ( String )value );
		}
		else
		{
			preparedStatement.setObject( index, value );
		}
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
		JDBCUtil.close( preparedStatement );
		preparedStatement = null;
		statementHolder = null;
	}
}
