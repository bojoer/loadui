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

public class SourceTable extends TableBase
{
	public static final String STATIC_FIELD_SOURCE_NAME = "__SOURCE";
	public static final String STATIC_FIELD_SOURCEID = "__SOURCE_ID";

	private Map<String, Integer> inMemoryTable = new HashMap<String, Integer>();

	public SourceTable( String dbName, String name, ConnectionProvider connectionProvider, MetadataProvider metadataProvider,
			TableProvider tableProvider )
	{
		super( dbName, name, null, connectionProvider, metadataProvider, tableProvider );

		try
		{
			List<Map<String, Object>> result = select( null );
			for( int i = 0; i < result.size(); i++ )
			{
				inMemoryTable.put( ( String )result.get( i ).get( STATIC_FIELD_SOURCE_NAME ), ( Integer )result.get( i )
						.get( STATIC_FIELD_SOURCEID ) );
			}
		}
		catch( SQLException e )
		{
			throw new RuntimeException( "Unable to read data from table!", e );
		}
	}

	@Override
	public void insert( Map<String, ? extends Object> data ) throws SQLException
	{
		super.insert( data );
		// TODO commit for now.
		commit();
		inMemoryTable.put( ( String )data.get( STATIC_FIELD_SOURCE_NAME ), ( Integer )data.get( STATIC_FIELD_SOURCEID ) );
	}

	public synchronized Integer getSourceId( String source )
	{
		if( inMemoryTable.get( source ) == null )
		{
			Map<String, Object> data = new HashMap<String, Object>();
			data.put( STATIC_FIELD_SOURCE_NAME, source );

			SequenceTable st = ( SequenceTable )getTable( SequenceTable.SEQUENCE_TABLE_NAME );

			data.put( STATIC_FIELD_SOURCEID, st.next( getExternalName(), STATIC_FIELD_SOURCEID ) );
			try
			{
				insert( data );
				commit();
			}
			catch( SQLException e )
			{
				throw new RuntimeException( "Unable to retrieve sourceid value!", e );
			}
		}
		return inMemoryTable.get( source );
	}

	@Override
	public void dispose()
	{
		super.dispose();
		inMemoryTable.clear();
		inMemoryTable = null;
	}

	@Override
	protected void initializeDescriptor( TableDescriptor descriptor )
	{
		descriptor.addStaticField( STATIC_FIELD_SOURCE_NAME, String.class );
		descriptor.addStaticField( STATIC_FIELD_SOURCEID, Integer.class );

		descriptor.addToPkSequence( STATIC_FIELD_SOURCE_NAME );
	}

}
