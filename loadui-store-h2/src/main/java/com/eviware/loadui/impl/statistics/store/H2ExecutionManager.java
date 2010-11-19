package com.eviware.loadui.impl.statistics.store;

import java.io.File;
import java.util.HashMap;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcConnectionPool;

public class H2ExecutionManager extends ExecutionManagerImpl
{
	public static final String DB_BASEDIR = new File( System.getProperty( "loadui.home" ), "controller".equals( System
			.getProperty( "loadui.instance" ) ) ? "data_c" : "data_a" ).toURI().toString() + "/";

	public static final String SQL_CREATE_TABLE_EXPRESSION = "CREATE TABLE";
	public static final String SQL_PRIMARY_KEY_EXPRESSION = "PRIMARY KEY";
	public static final String SQL_ADD_PRIMARY_KEY_INDEX_EXPRESSION = "ALTER TABLE ? ADD CONSTRAINT ?_pk_index PRIMARY KEY(?)";
	

	public static final String TYPE_INTEGER = "INT";
	public static final String TYPE_BIGINT = "BIGINT";
	public static final String TYPE_DOUBLE = "DOUBLE";
	public static final String TYPE_STRING = "VARCHAR(255)";

	private static HashMap<Class<? extends Object>, String> typeConversionMap;

	static
	{
		typeConversionMap = new HashMap<Class<? extends Object>, String>();
		typeConversionMap.put( Integer.class, TYPE_INTEGER );
		typeConversionMap.put( Long.class, TYPE_BIGINT );
		typeConversionMap.put( Double.class, TYPE_DOUBLE );
		typeConversionMap.put( String.class, TYPE_STRING );
	}

	@Override
	protected DataSource createDataSource( String db )
	{
		JdbcConnectionPool cp = JdbcConnectionPool.create( "jdbc:h2:" + DB_BASEDIR + db, "sa", "sa" );
		//JdbcConnectionPool cp = JdbcConnectionPool.create( "jdbc:h2:~/_data/" + db, "sa", "sa" );
		cp.setMaxConnections( 5 );
		return cp;
	}

	@Override
	public String getCreateTableExpression()
	{
		return SQL_CREATE_TABLE_EXPRESSION;
	}

	@Override
	public String getPrimaryKeyExpression()
	{
		return SQL_PRIMARY_KEY_EXPRESSION;
	}

	@Override
	public String getAddPrimaryKeyIndexExpression()
	{
		return SQL_ADD_PRIMARY_KEY_INDEX_EXPRESSION;
	}
	
	@Override
	protected HashMap<Class<? extends Object>, String> getTypeConversionMap()
	{
		return typeConversionMap;
	}

	/**
	 * Called before stopping the application. Closes any open connections to
	 * databases.
	 */
	public void release()
	{
		super.dispose();
	}
}
