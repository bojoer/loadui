package com.eviware.loadui.impl.statistics.store.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCUtil1
{

	public static void close( ResultSet resultSet )
	{
		try
		{
			resultSet.close();
		}
		catch( SQLException e )
		{
			// do nothing
		}
	}

	public static void close( Statement statement )
	{
		try
		{
			statement.close();
		}
		catch( SQLException e )
		{
			// do nothing
		}
	}

	public static void close( Connection connection )
	{
		try
		{
			connection.close();
		}
		catch( SQLException e )
		{
			// do nothing
		}
	}

}
