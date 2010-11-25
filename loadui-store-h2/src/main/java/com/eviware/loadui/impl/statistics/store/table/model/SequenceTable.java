package com.eviware.loadui.impl.statistics.store.table.model;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eviware.loadui.impl.statistics.store.table.ConnectionProvider;
import com.eviware.loadui.impl.statistics.store.table.MetadataProvider;
import com.eviware.loadui.impl.statistics.store.table.TableBase;
import com.eviware.loadui.impl.statistics.store.table.TableDescriptor;
import com.eviware.loadui.impl.statistics.store.table.TableProvider;

public class SequenceTable extends TableBase
{
	public static final String SELECT_ARG_TABLE_EQ = "table_eq";
	public static final String SELECT_ARG_COLUMN_EQ = "column_eq";

	public static final String SEQUENCE_TABLE_NAME = "sequence_table";

	public static final String STATIC_FIELD_TABLE = "__TABLE";
	public static final String STATIC_FIELD_COLUMN = "__COLUMN";
	public static final String STATIC_FIELD_VALUE = "__VALUE";

	public static final String STATEMENT_UPDATE_VALUE = "updateValueStatement";

	public SequenceTable( String dbName, ConnectionProvider connectionProvider, MetadataProvider metadataProvider,
			TableProvider tableProvider )
	{
		super( dbName, SEQUENCE_TABLE_NAME, null, connectionProvider, metadataProvider, tableProvider );

		prepareStatement( STATEMENT_UPDATE_VALUE, "update " + getTableName() + " set " + STATIC_FIELD_VALUE
				+ " = ? where " + STATIC_FIELD_TABLE + " = ? and " + STATIC_FIELD_COLUMN + " = ?" );
	}

	public Integer next( String tableName, String column )
	{
		try
		{
			Integer id;
			Map<String, Object> data = new HashMap<String, Object>();
			data.put( SELECT_ARG_TABLE_EQ, tableName );
			data.put( SELECT_ARG_COLUMN_EQ, column );
			List<Map<String, Object>> rs = select( data );
			if( rs.size() == 0 )
			{
				id = 0;
				data.clear();
				data.put( STATIC_FIELD_TABLE, tableName );
				data.put( STATIC_FIELD_COLUMN, column );
				data.put( STATIC_FIELD_VALUE, id );
				insert( data );
			}
			else
			{
				id = ( Integer )rs.get( 0 ).get( STATIC_FIELD_VALUE ) + 1;
				Object[] params = new Object[3];
				params[0] = id;
				params[1] = tableName;
				params[2] = column;
				execute( STATEMENT_UPDATE_VALUE, params );
			}
			commit();
			return id;
		}
		catch( SQLException e )
		{
			throw new RuntimeException( "Unable to retrieve next sequence value!", e );
		}
	}

	@Override
	protected void initializeDescriptor( TableDescriptor descriptor )
	{
		descriptor.addStaticField( STATIC_FIELD_TABLE, String.class );
		descriptor.addStaticField( STATIC_FIELD_COLUMN, String.class );
		descriptor.addStaticField( STATIC_FIELD_VALUE, Integer.class );

		descriptor.addToPkSequence( STATIC_FIELD_TABLE );
		descriptor.addToPkSequence( STATIC_FIELD_COLUMN );

		descriptor.addSelectCriteria( SELECT_ARG_TABLE_EQ, STATIC_FIELD_TABLE, "=?" );
		descriptor.addSelectCriteria( SELECT_ARG_COLUMN_EQ, STATIC_FIELD_COLUMN, "=?" );
	}

}
