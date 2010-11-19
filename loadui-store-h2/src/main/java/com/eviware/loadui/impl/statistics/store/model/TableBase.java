package com.eviware.loadui.impl.statistics.store.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.eviware.loadui.impl.statistics.store.util.JDBCUtil;
import com.eviware.loadui.impl.statistics.store.util.PreparedStatementHolder;
import com.eviware.loadui.impl.statistics.store.util.StatementHolder;

public class TableBase
{

	public static final String TABLE_NAME_PREFIX = "_";

	private List<String[]> selectCriteria = new ArrayList<String[]>();

	private Map<String, Class<? extends Object>> staticFields = new HashMap<String, Class<? extends Object>>();

	private List<String> pkSequence = new ArrayList<String>();

	protected void registerStaticField( String name, Class<? extends Object> type )
	{
		staticFields.put( name, type );
	}

	protected void addToPkSequence( String field )
	{
		pkSequence.add( field );
	}

	protected void addSelectCriteria( String field, String criteria )
	{
		selectCriteria.add( new String[] { field, criteria } );
	}

	private String dbName;

	private String externalName;

	private String tableName;

	private String createTableExpr;

	private String pkExpr;

	private HashMap<Class<? extends Object>, String> typeConversionMap;

	private PreparedStatementHolder insertStatement;

	private PreparedStatementHolder selectStatement;

	private String createScript;

	private String addPkIndexScript;

	private PreparedStatement deleteStatement;

	private Map<String, ? extends Class<? extends Object>> dynamicFields = new HashMap<String, Class<? extends Object>>();

	private Connection connection;

	private TableBase parentTable;

	private Map<String, PreparedStatement> extraStatementMap;

	public void init( String dbName, String name, String createTableExpr, String pkExpr,
			Map<String, ? extends Class<? extends Object>> dynamicFields,
			HashMap<Class<? extends Object>, String> typeConversionMap, Connection connection )
	{
		this.externalName = name;
		this.tableName = buildTableName( name );
		this.dbName = dbName;
		this.createTableExpr = createTableExpr;
		this.pkExpr = pkExpr;
		this.typeConversionMap = typeConversionMap;
		this.dynamicFields = dynamicFields;
		this.connection = connection;

		try
		{
			addPkIndexScript = createAddPkIndexScript();
			createScript = createTableCreateScript();
			if( !exist() )
			{
				create();
			}

			StatementHolder sh = createTableInsertScript();
			insertStatement = new PreparedStatementHolder( connection.prepareStatement( sh.getStatementSql() ), sh );
			sh = createTableSelectScript();
			selectStatement = new PreparedStatementHolder( connection.prepareStatement( sh.getStatementSql() ), sh );
			deleteStatement = connection.prepareStatement( "DELETE FROM " + tableName );
		}
		catch( SQLException e )
		{
			throw new RuntimeException( "Unable to initialize statements for table handling!", e );
		}
	}

	protected boolean exist()
	{
		Statement stm = null;
		try
		{
			stm = connection.createStatement();
			stm.execute( "select * from " + tableName + " where 1 = 2" );
			return true;
		}
		catch( SQLException e )
		{
			return false;
		}
		finally
		{
			JDBCUtil.close( stm );
		}
	}

	protected StatementHolder createTableSelectScript()
	{
		StatementHolder sh = new StatementHolder();
		StringBuffer crieriaParams = new StringBuffer();
		for( int i = 0; i < selectCriteria.size(); i++ )
		{
			crieriaParams.append( selectCriteria.get( i )[0] + " " + selectCriteria.get( i )[1] );

			if( i < selectCriteria.size() - 1 )
			{
				crieriaParams.append( " AND " );
			}
			sh.addArgument( selectCriteria.get( i )[0] );
		}
		if( crieriaParams.length() > 0 )
		{
			crieriaParams.insert( 0, " WHERE " );
		}

		sh.setStatementSql( "SELECT * FROM " + tableName + crieriaParams.toString() );
		return sh;
	}

	protected StatementHolder createTableInsertScript()
	{
		StatementHolder sh = new StatementHolder();
		String key;
		StringBuffer names = new StringBuffer();
		StringBuffer values = new StringBuffer();

		Iterator<String> keys = staticFields.keySet().iterator();
		while( keys.hasNext() )
		{
			key = keys.next();
			names.append( key );
			values.append( "?" );
			if( keys.hasNext() )
			{
				names.append( ", " );
				values.append( ", " );
			}
			sh.addArgument( key );
		}
		if( dynamicFields != null && dynamicFields.size() > 0 )
		{
			names.append( ", " );
			values.append( ", " );
			keys = dynamicFields.keySet().iterator();
			while( keys.hasNext() )
			{
				key = keys.next();
				names.append( key );
				values.append( "?" );
				if( keys.hasNext() )
				{
					names.append( ", " );
					values.append( ", " );
				}
				sh.addArgument( key );
			}
		}
		sh.setStatementSql( "INSERT INTO " + tableName + "( " + names.toString() + " ) VALUES ( " + values.toString()
				+ " )" );
		return sh;
	}

	protected String createTableCreateScript()
	{
		StringBuffer b = new StringBuffer();
		b.append( createTableExpr );
		b.append( " " );
		b.append( tableName );
		b.append( " ( " );

		String key;
		Iterator<String> keys = staticFields.keySet().iterator();
		while( keys.hasNext() )
		{
			key = keys.next();
			b.append( key );
			b.append( " " );
			b.append( typeConversionMap.get( staticFields.get( key ) ) );
			if( pkSequence.contains( key ) )
			{
				b.append( " NOT NULL " );
			}
			if( keys.hasNext() )
			{
				b.append( ", " );
			}
		}
		if( dynamicFields != null && dynamicFields.size() > 0 )
		{
			b.append( ", " );
			keys = dynamicFields.keySet().iterator();
			while( keys.hasNext() )
			{
				key = keys.next();
				b.append( key );
				b.append( " " );
				b.append( typeConversionMap.get( dynamicFields.get( key ) ) );
				if( keys.hasNext() )
				{
					b.append( ", " );
				}
			}
		}
		b.append( " ) " );
		return b.toString();
	}

	protected String createAddPkIndexScript()
	{
		if( pkSequence.size() == 0 )
		{
			return null;
		}
		StringBuffer b = new StringBuffer();
		for( int i = 0; i < pkSequence.size(); i++ )
		{
			b.append( pkSequence.get( i ) );
			if( i < pkSequence.size() - 1 )
			{
				b.append( ", " );
			}
		}
		return pkExpr.replaceFirst( "\\?", tableName ).replaceFirst( "\\?", tableName )
				.replaceFirst( "\\?", b.toString() );
	}

	public void insert( Map<String, ? extends Object> data ) throws SQLException
	{
		insertStatement.setArguments( data );
		insertStatement.executeUpdate();
	}

	private void create() throws SQLException
	{
		Statement stm = connection.createStatement();
		try
		{
			stm.execute( createScript );
			if( addPkIndexScript != null )
			{
				stm.execute( addPkIndexScript );
			}
		}
		catch( SQLException e )
		{
			throw e;
		}
		finally
		{
			JDBCUtil.close( stm );
		}
	}

	public List<Map<String, ? extends Object>> select( Map<String, Object> data ) throws SQLException
	{
		List<Map<String, ? extends Object>> result = new ArrayList<Map<String, ? extends Object>>();
		selectStatement.setArguments( data );
		ResultSet rs = selectStatement.executeQuery();
		Map<String, Object> row;
		while( rs.next() )
		{
			row = new HashMap<String, Object>();
			for( int i = 0; i < rs.getMetaData().getColumnCount(); i++ )
			{
				row.put( rs.getMetaData().getColumnName( i + 1 ), rs.getObject( i + 1 ) );
			}
			result.add( row );
		}
		return result;
	}

	public void delete() throws SQLException
	{
		deleteStatement.execute();
		commit();
	}

	public void dispose()
	{
		selectStatement.dispose();
		insertStatement.dispose();
		JDBCUtil.close( deleteStatement );
		if( extraStatementMap != null )
		{
			Iterator<Entry<String, PreparedStatement>> iterator = extraStatementMap.entrySet().iterator();
			while( iterator.hasNext() )
			{
				JDBCUtil.close( iterator.next().getValue() );
			}
		}
		JDBCUtil.close( connection );
	}

	public TableBase getParentTable()
	{
		return parentTable;
	}

	public void setParentTable( TableBase parentTable )
	{
		this.parentTable = parentTable;
	}

	public String getExternalName()
	{
		return externalName;
	}

	public String getTableName()
	{
		return tableName;
	}

	public String getDbName()
	{
		return dbName;
	}

	protected void execute( String statement, Object[] params ) throws SQLException
	{
		PreparedStatement stm = extraStatementMap.get( statement );
		if( stm != null )
		{
			for( int i = 0; i < params.length; i++ )
			{
				stm.setObject( i + 1, params[i] );
			}
			stm.execute();
		}
	}

	protected void prepareStatement( String name, String sql )
	{
		try
		{
			if( extraStatementMap == null )
			{
				extraStatementMap = new HashMap<String, PreparedStatement>();
			}
			extraStatementMap.put( name, connection.prepareStatement( sql ) );
		}
		catch( SQLException e )
		{
			throw new RuntimeException( "Unable to initialize extra statement!", e );
		}
	}

	protected void commit() throws SQLException
	{
		connection.commit();
	}

	protected String buildTableName( String name )
	{
		return TABLE_NAME_PREFIX + name;
	}
}
