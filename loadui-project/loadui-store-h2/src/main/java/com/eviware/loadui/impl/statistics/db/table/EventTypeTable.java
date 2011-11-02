package com.eviware.loadui.impl.statistics.db.table;

import java.sql.SQLException;
import java.util.Map;

import com.eviware.loadui.impl.statistics.db.ConnectionRegistry;
import com.eviware.loadui.impl.statistics.db.DatabaseMetadata;
import com.eviware.loadui.impl.statistics.db.TableRegistry;

public class EventTypeTable extends TableBase
{
	private static final String STATIC_FIELD_EVENTTYPE_ID = "__ID";
	private static final String STATIC_FIELD_EVENTTYPE_LABEL = "EVENTTYPE";
	private static final String STATIC_FIELD_EVENTTYPE_TYPE = "TYPE";

	public EventTypeTable( String dbName, String name, Map<String, ? extends Class<? extends Object>> dynamicFields,
			ConnectionRegistry connectionRegistry, DatabaseMetadata databaseMetadata, TableRegistry tableRegistry )
			throws SQLException
	{
		super( dbName, name, dynamicFields, connectionRegistry, databaseMetadata, tableRegistry );
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void initializeDescriptor( TableDescriptor descriptor )
	{
		descriptor.addStaticField( STATIC_FIELD_EVENTTYPE_ID, Integer.class );
		descriptor.addStaticField( STATIC_FIELD_EVENTTYPE_LABEL, String.class );
		descriptor.addStaticField( STATIC_FIELD_EVENTTYPE_TYPE, String.class );

		descriptor.addToPkSequence( STATIC_FIELD_EVENTTYPE_ID );
	}

	@Override
	protected boolean useTableSpecificConnection()
	{
		return true;
	}

}
