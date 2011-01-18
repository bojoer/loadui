/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.impl.statistics.store;

import java.io.File;
import java.util.HashMap;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcConnectionPool;

import com.eviware.loadui.LoadUI;

public class H2ExecutionManager extends ExecutionManagerImpl
{
	public static final String DB_BASEDIR = new File( System.getProperty( LoadUI.LOADUI_HOME ),
			LoadUI.CONTROLLER.equals( System.getProperty( LoadUI.INSTANCE ) ) ? "data_c" : "data_a" ).toURI().toString()
			.replaceAll( "%20", " " )
			+ File.separator;

	public static final String SQL_CREATE_TABLE_EXPRESSION = "CREATE TABLE";
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
	public DataSource createDataSource( String db )
	{
		JdbcConnectionPool cp = JdbcConnectionPool.create( "jdbc:h2:" + DB_BASEDIR + db + ";DB_CLOSE_ON_EXIT=FALSE",
				"sa", "sa" );
		// JdbcConnectionPool cp = JdbcConnectionPool.create( "jdbc:h2:~/_data/" +
		// db, "sa", "sa" );
		cp.setMaxConnections( 5 );
		return cp;
	}

	/**
	 * Called before stopping the application. Closes any open connections to
	 * databases.
	 */
	public void release()
	{
		super.dispose();
	}

	@Override
	protected void initializeDatabaseMetadata( DatabaseMetadata metadata )
	{
		metadata.setAddPrimaryKeyIndexExpression( SQL_ADD_PRIMARY_KEY_INDEX_EXPRESSION );
		metadata.setCreateTableExpression( SQL_CREATE_TABLE_EXPRESSION );

		metadata.addTypeConversionPair( Integer.class, TYPE_INTEGER );
		metadata.addTypeConversionPair( Long.class, TYPE_BIGINT );
		metadata.addTypeConversionPair( Double.class, TYPE_DOUBLE );
		metadata.addTypeConversionPair( String.class, TYPE_STRING );
	}

}
