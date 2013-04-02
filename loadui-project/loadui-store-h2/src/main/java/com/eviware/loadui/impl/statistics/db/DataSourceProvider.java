/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.impl.statistics.db;

import javax.sql.DataSource;

/**
 * Implement this interface to provide creation and disposal of data sources.
 * The implementation of this interface is database specific.
 * 
 * @author predrag.vucetic
 * 
 */
public interface DataSourceProvider<Type extends DataSource>
{
	/**
	 * Creates data source for the specified database
	 * 
	 * @param dbName
	 *           Name of the database for which data source needs to be created
	 * @return Created data source
	 */
	public Type createDataSource( String dbName );

	/**
	 * Releases data source
	 * 
	 * @param dataSource
	 *           data source to release
	 */
	public void releaseDataSource( Type dataSource );
}
