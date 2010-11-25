package com.eviware.loadui.impl.statistics.store.table;

import java.sql.Connection;
import java.sql.SQLException;


public interface ConnectionProvider
{
	public Connection getConnection( TableBase table ) throws SQLException;
}
