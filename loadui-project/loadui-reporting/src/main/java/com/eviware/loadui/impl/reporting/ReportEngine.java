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
package com.eviware.loadui.impl.reporting;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportEngine
{
	public enum ReportFormats
	{
		PDF, XLS, HTML, RTF, CSV, TXT, XML, JASPER_PRINT
	};

	private static final Logger log = LoggerFactory.getLogger( ReportEngine.class );

	private static final File reportDirectory = new File( "reports" );

	private final TreeMap<String, LReportTemplate> reports = new TreeMap<String, LReportTemplate>();

	private final ReportProtocolFactory protocolFactory;

	public ReportEngine()
	{
		loadReports();

		protocolFactory = new ReportProtocolFactory( this );
	}

	public void generateJasperReport( JRDataSource dataSource, String selectedReportName, File outfile, String format )
			throws JRException
	{
		generateJasperReport( dataSource, selectedReportName, outfile, format, null );
	}

	public void generateJasperReport( JRDataSource dataSource, String selectedReportName, File outfile, String format,
			JasperPrint prepend ) throws JRException
	{
		LReportTemplate selectedReport = getReport( selectedReportName );
		// fill report with data
		if( selectedReport != null )
		{
			ReportFillWorker reportWorker = new ReportFillWorker( dataSource, selectedReport );

			JasperPrint jp = reportWorker.getJasperReport();
			if( prepend != null )
			{
				jp = mergeJprints( prepend, jp );
			}

			if( jp != null )
			{
				ReportFormats rf = ReportFormats.valueOf( format );

				JRAbstractExporter jrExporter = null;
				log.debug( "  got param: {}", rf.toString() );
				switch( rf )
				{
				case JASPER_PRINT :
					try
					{
						ObjectOutput oo;
						oo = new ObjectOutputStream( new FileOutputStream( outfile ) );
						try
						{
							oo.writeObject( jp );
						}
						finally
						{
							oo.close();
						}
					}
					catch( IOException e )
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return;
				case PDF :
					jrExporter = new JRPdfExporter();
					break;
				case XLS :
					jrExporter = new JExcelApiExporter();
					break;
				case HTML :
					jrExporter = new JRHtmlExporter();
					break;
				case RTF :
					jrExporter = new JRRtfExporter();
					break;
				case CSV :
					jrExporter = new JRCsvExporter();
					break;
				case XML :
					jrExporter = new JRXmlExporter();
					break;
				default :
					// TXT
					jrExporter = new JRTextExporter();
					jrExporter.setParameter( JRTextExporterParameter.CHARACTER_WIDTH, new Float( 10 ) );
					jrExporter.setParameter( JRTextExporterParameter.CHARACTER_HEIGHT, new Float( 10 ) );
					break;
				}
				jrExporter.setParameter( JRExporterParameter.OUTPUT_FILE_NAME, outfile.getAbsolutePath() );
				jrExporter.setParameter( JRExporterParameter.JASPER_PRINT, jp );
				jrExporter.exportReport();
			}
			else
			{
				log.error( "Errors in ReportTemplate!" );
			}
		}
		else
		{
			log.error( "Report do not exists!" );
		}

	}

	public void generateJasperReport( JRDataSource dataSource, String selectedReportName, String title )
			throws JRException
	{
		generateJasperReport( dataSource, selectedReportName, title, null );
	}

	public void generateJasperReport( JRDataSource dataSource, String selectedReportName, String title,
			JasperPrint prepend ) throws JRException
	{
		LReportTemplate selectedReport = getReport( selectedReportName );
		// // fill report with data
		if( selectedReport != null )
		{
			ReportFillWorker reportWorker = new ReportFillWorker( dataSource, selectedReport );

			JasperPrint jp = reportWorker.getJasperReport();
			if( prepend != null )
			{
				jp = mergeJprints( prepend, jp );
			}

			if( jp != null )
			{
				jp.setName( "Report for " + title );
				JasperViewer jv = new JasperViewer( jp, false );
				jv.setTitle( "Report for " + title );
				jv.setVisible( true );
				jv.setFitPageZoomRatio();
			}
			else
			{
				log.error( "Errors in ReportTemplate!" );
			}
		}
		else
		{
			log.error( "Report do not exists!" );
		}
	}

	private JasperPrint createReport( JRDataSource dataSource, LReportTemplate selectedReport ) throws JRException
	{
		log.debug( "Creating report!" );
		updateReport( selectedReport );

		LReportTemplate report = new LReportTemplate( selectedReport );

		JasperReport jr = compileReport( report );

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put( JRParameter.REPORT_URL_HANDLER_FACTORY, protocolFactory );

		return JasperFillManager.fillReport( jr, map, dataSource );
	}

	private JasperReport compileReport( LReportTemplate report )
	{
		log.info( "compile report" );
		JasperReport jr = null;
		try
		{
			ByteArrayInputStream in = new ByteArrayInputStream( report.getData().getBytes() );
			JasperDesign design = JRXmlLoader.load( in );

			jr = JasperCompileManager.compileReport( design );
		}
		catch( JRException e )
		{
			e.printStackTrace();
		}
		log.debug( "Compiling report done." );
		return jr;
	}

	/*
	 * check if report template is changed and if it is reload it.
	 */
	private void updateReport( LReportTemplate report )
	{
		report.update();
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

	public LReportTemplate getReport( String name )
	{
		LReportTemplate result = reports.get( name );
		result.update();
		return result;
	}

	@SuppressWarnings( "unchecked" )
	public JasperPrint mergeJprints( JasperPrint jp1, JasperPrint jp2 )
	{
		List<JRPrintPage> jPages2 = jp2.getPages();
		for( Iterator<JRPrintPage> iter = jPages2.iterator(); iter.hasNext(); )
		{
			JRPrintPage jPage = iter.next();
			jp1.addPage( jPage );
		}
		jp1.setPageHeight( jp2.getPageHeight() );
		jp1.setPageWidth( jp2.getPageWidth() );
		jp1.setOrientation( jp2.getOrientationValue() );
		jp1.setDefaultFont( jp2.getDefaultFont() );
		return jp1;
	}

	private class ReportFillWorker
	{

		private JasperPrint jp;
		private final LReportTemplate report;
		private final JRDataSource dataSource;

		public ReportFillWorker( JRDataSource dataSource, LReportTemplate selectedReport )
		{
			report = selectedReport;
			this.dataSource = dataSource;
		}

		public JasperPrint getJasperReport()
		{
			try
			{
				jp = createReport( dataSource, report );
			}
			catch( Throwable e )
			{
				e.printStackTrace();
				jp = null;
			}

			return jp;
		}
	}
}