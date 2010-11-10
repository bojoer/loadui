package com.eviware.loadui.api.statistics.store;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.StatisticsWriter;

/**
 * Used to handle database operations. Reading and writing on both agents and
 * controller.
 * 
 * @author predrag.vucetic
 * 
 */
public interface StorageManager
{

	/**
	 * Initializes manager for new run. Should be called when test starts.
	 * 
	 * @param testId
	 *           ID of test that was started.
	 */
	public void start( String testId );

	/**
	 * Releases resources and closes the database.
	 */
	public void stop();

	/**
	 * Writes data to the database, extracting it from a StatisticsWriter. This
	 * is intended to be used on agents, where source is supposed always to be
	 * "local"
	 * 
	 * @param writer
	 *           StatisticWriter which current data needs to be saved to the
	 *           database
	 * @param source
	 *           Data source (e.g. Agent, Local, Aggregate)
	 * @throws SQLException
	 *            If error occurs during writing
	 */
	public void write( StatisticsWriter writer, String source ) throws SQLException;

	/**
	 * Writes data to the database. This is intended to be used on controller to
	 * insert data sent from the agent.
	 * 
	 * @param writer
	 *           StatisticWriter used for metadata
	 * @param source
	 *           Data source (e.g. AgentA, AgentB, Local, Aggregate)
	 * @param data
	 *           Data to write to the database
	 * @throws SQLException
	 *            If error occurs during writing
	 */
	public void write( StatisticsWriter writer, String source, Map<String, ? extends Number> data ) throws SQLException;

	/**
	 * Reads data from the database. This is intended retriving data from the
	 * database suitable for charts.
	 * 
	 * @param test
	 *           Test which data should be retrieved
	 * @param agents
	 *           Agents which values should be aggregated
	 * @param statistics
	 *           Statistics that should be retrieved
	 * @param timestamp
	 *           All point with timestamp greater than this should be retrieved.
	 * @return Map containing lists of DataPoints, grouped by statistics.
	 */
	public HashMap<String, List<DataPoint<Number>>> read( String test, String[] agents, String[] statistics,
			long timestamp );

}
