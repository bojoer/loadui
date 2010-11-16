package com.eviware.loadui.impl.statistics.store.util;

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

import javax.sql.DataSource;

public class MetaDataManager
{
	public static final String METATABLE_NAME = "__meta_table";

	public static final String COLUMN_TRACKID = "trackid";

	public static final String PK_COLUMN = COLUMN_TRACKID;

	public static final String SELECT_BY_TRACK = "select * from " + METATABLE_NAME + " where " + COLUMN_TRACKID + " = ?";
	
	public static final String DELETE_BY_TRACK = "delete from " + METATABLE_NAME + " where " + COLUMN_TRACKID + " = ?";
	
	public static final String DELETE_ALL = "delete from " + METATABLE_NAME;

	private static Map<String, Class<? extends Object>> metaTableStructure;

	private InsertStatementHolder insertStatementHolder;

	static
	{
		metaTableStructure = new HashMap<String, Class<? extends Object>>();
		metaTableStructure.put( COLUMN_TRACKID, String.class );
	}

	private Map<String, Connection> connectionMap;
	private Map<String, PreparedStatementHolder> writeStatementMap;
	private Map<String, PreparedStatement> readStatementMap;
	private Map<String, PreparedStatement> deleteTrackStatementMap;
	private Map<String, PreparedStatement> deleteAllStatementMap;

	private String createTableExpr;
	private String pkExpr;
	private HashMap<Class<? extends Object>, String> typeConversionMap;

	public MetaDataManager( String createTableExpr, String pkExpr,
			HashMap<Class<? extends Object>, String> typeConversionMap )
	{
		super();
		this.createTableExpr = createTableExpr;
		this.pkExpr = pkExpr;
		this.typeConversionMap = typeConversionMap;
		insertStatementHolder = SQLUtil.createTableInsertScript( METATABLE_NAME, metaTableStructure.keySet() );
	}

	private boolean tableExists( String executionId )
	{
		Connection connection = connectionMap.get( executionId );
		Statement stm = null;
		try
		{
			stm = connection.createStatement();
			stm.execute( "select * from " + METATABLE_NAME + " where 1 = 2" );
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

	public void createMetaTable( String executionId, Connection connection )
	{
		connectionMap.put( executionId, connection );
		if( !tableExists( executionId ) )
		{
			Statement stm = null;
			try
			{
				String createSql = SQLUtil.createTableCreateScript( createTableExpr, METATABLE_NAME, pkExpr, PK_COLUMN,
						metaTableStructure, typeConversionMap );
				stm = connection.createStatement();
				stm.execute( createSql );
			}
			catch( SQLException e )
			{
				throw new RuntimeException( "Creating metadata table failed!", e );
			}
			finally
			{
				JDBCUtil.close( stm );
			}
		}
	}

	public void write( String executionId, Map<String, ? extends Object> data ) throws SQLException
	{
		Connection conn = connectionMap.get( executionId );
		PreparedStatementHolder psh = writeStatementMap.get( executionId );
		if( psh == null )
		{
			PreparedStatement pstm = conn.prepareStatement( insertStatementHolder.getStatementSql() );
			psh = new PreparedStatementHolder( pstm, insertStatementHolder );
			writeStatementMap.put( executionId, psh );
		}
		psh.setArguments( data );
		psh.executeUpdate();
		conn.commit();
	}

	public Map<String, Object> read( String executionId, String trackId ) throws SQLException
	{
		Map<String, Object> result = new HashMap<String, Object>();
		Connection conn = connectionMap.get( executionId );
		PreparedStatement pstm = readStatementMap.get( executionId );
		if( pstm == null )
		{
			pstm = conn.prepareStatement( SELECT_BY_TRACK );
			readStatementMap.put( executionId, pstm );
		}
		pstm.setObject( 1, trackId );
		ResultSet rs = pstm.executeQuery();
		if( rs.next() )
		{
			for( int i = 0; i < rs.getMetaData().getColumnCount(); i++ )
			{
				result.put( rs.getMetaData().getColumnName( i + 1 ), rs.getObject( i + 1 ) );
			}
		}
		JDBCUtil.close( rs );
		return result;
	}

	public void deleteTrack( String executionId, String trackId ) throws SQLException
	{
		Connection conn = connectionMap.get( executionId );
		PreparedStatement pstm = deleteTrackStatementMap.get( executionId );
		if( pstm == null )
		{
			pstm = conn.prepareStatement( DELETE_BY_TRACK );
			deleteTrackStatementMap.put( executionId, pstm );
		}
		pstm.setObject( 1, trackId );
		pstm.executeQuery();
		conn.commit();
	}
	
	public void deleteAll( String executionId ) throws SQLException
	{
		Connection conn = connectionMap.get( executionId );
		PreparedStatement pstm = deleteAllStatementMap.get( executionId );
		if( pstm == null )
		{
			pstm = conn.prepareStatement( DELETE_ALL );
			deleteAllStatementMap.put( executionId, pstm );
		}
		pstm.executeQuery();
		conn.commit();
	}

	public void dispose()
	{
		for( PreparedStatementHolder p : writeStatementMap.values()){
			p.dispose();
		}
		for( PreparedStatement p : readStatementMap.values()){
			JDBCUtil.close( p );
		}
		for( PreparedStatement p : deleteTrackStatementMap.values()){
			JDBCUtil.close( p );
		}
		for( PreparedStatement p : deleteAllStatementMap.values()){
			JDBCUtil.close( p );
		}
		for( Connection c : connectionMap.values()){
			JDBCUtil.close( c );
		}
	}
}
