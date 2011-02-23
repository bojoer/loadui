/*
 * Copyright 2011 eviware software ab
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
package com.eviware.loadui.util.reporting;

import java.io.File;
import java.io.FilenameFilter;
import java.util.TreeMap;

import net.sf.jasperreports.engine.JRException;

import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.summary.Summary;
import com.eviware.loadui.util.reporting.datasources.SummaryDataSource;
import com.eviware.loadui.util.reporting.datasources.statistics.ExecutionDataSource;

public class JasperReportManager
{

	private static JasperReportManager _instance = null;

	private static final File reportDirectory = new File( "reports" );

	private TreeMap<String, LReportTemplate> reports = new TreeMap<String, LReportTemplate>();

	private JasperReportManager()
	{
		// load report templates
		loadReports();
	}

	/*
	 * Need to be singleton, since it works with file system and init could done
	 * only once.
	 */
	public static JasperReportManager getInstance()
	{
		if( _instance == null )
			_instance = new JasperReportManager();
		return _instance;
	}

	// loads all jasper reports from reports dir
	private void loadReports()
	{
		File[] reports = reportDirectory.listFiles( new FilenameFilter()
		{

			@Override
			public boolean accept( File dir, String name )
			{
				return name.endsWith( "jrxml" );
			}
		} );
		for( File reportTemplate : reports )
		{
			this.reports.put( reportTemplate.getName().replace( ".jrxml", "" ),
					new LReportTemplate( reportTemplate.getName(), reportTemplate ) );
		}
	}

	/*
	 * This will create a report and open JasperViewer
	 */
	public void createReport( Summary summary )
	{
		try
		{
			ReportEngine.generateJasperReport( new SummaryDataSource( summary ), reports.get( "SummaryReport" ), summary
					.getChapters().keySet().iterator().next() );
		}
		catch( JRException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * this will create report and save it in given file and given format
	 */
	public void createReport( Summary summary, File file, String format )
	{
		try
		{
			ReportEngine.generateJasperReport( new SummaryDataSource( summary ), reports.get( "SummaryReport" ), file,
					format );
		}
		catch( JRException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createReport( Execution execution, StatisticPage page )
	{
		try
		{
			ReportEngine.generateJasperReport( new ExecutionDataSource( execution, page ), reports.get( "ResultsReport" ),
					execution.getLabel() );
		}
		catch( JRException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * this will create report and save it in given file and given format
	 */
	public void createReport( Execution execution, StatisticPage page, File file, String format )
	{
		try
		{
			ReportEngine.generateJasperReport( new ExecutionDataSource( execution, page ), reports.get( "SummaryReport" ),
					file, format );
		}
		catch( JRException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public LReportTemplate getReport( String name )
	{
		LReportTemplate result = reports.get( name );
		result.update();
		return result;
	}
}
