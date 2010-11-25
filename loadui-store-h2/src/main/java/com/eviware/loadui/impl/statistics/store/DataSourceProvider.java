package com.eviware.loadui.impl.statistics.store;

import javax.sql.DataSource;

public interface DataSourceProvider
{
	/**
	 * Creates data source for the specified database
	 * 
	 * @param dbName
	 *           Name of the database for which data source needs to be created
	 * @return Created data source
	 */
	public DataSource createDataSource( String dbName );
}
