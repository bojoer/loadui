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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
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

import com.eviware.loadui.api.summary.Summary;
import com.eviware.loadui.util.reporting.datasources.SummaryDataSource;

public class ReportEngine
{

	public enum ReportFormats
	{
		PDF, XLS, HTML, RTF, CSV, TXT, XML
	};
	
	static Logger log = LoggerFactory.getLogger(ReportEngine.class);

	public static void generateJasperReport(Summary summary, LReportTemplate selectedReport, File outfile, String format)
			throws JRException
	{
		// fill report with data
		if (selectedReport != null)
		{
			ReportFillWorker reportWorker = new ReportFillWorker(summary, selectedReport);

			JasperPrint jp = reportWorker.getJasperReport();

			if (jp != null)
			{
				ReportFormats rf = ReportFormats.valueOf(format);
				JRAbstractExporter jrExporter = null;
				switch (rf)
				{
				case PDF:
					jrExporter = new JRPdfExporter();
					break;
				case XLS:
					jrExporter = new JExcelApiExporter();
					break;
				case HTML:
					jrExporter = new JRHtmlExporter();
					break;
				case RTF:
					jrExporter = new JRRtfExporter();
					break;
				case CSV:
					jrExporter = new JRCsvExporter();
					break;
				case XML:
					jrExporter = new JRXmlExporter();
					break;
				default:
					// TXT
					jrExporter = new JRTextExporter();
					jrExporter.setParameter(JRTextExporterParameter.CHARACTER_WIDTH, new Float(10));
					jrExporter.setParameter(JRTextExporterParameter.CHARACTER_HEIGHT, new Float(10));
					break;
				}
				jrExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outfile.getAbsolutePath());
				jrExporter.setParameter(JRExporterParameter.JASPER_PRINT, jp);
				jrExporter.exportReport();
			}
			else
			{
				log.error("Errors in ReportTemplate!");
			}
		}
		else
		{
			log.error("Report do not exists!");
		}

	}

	public static void generateJasperReport(Summary summary, LReportTemplate selectedReport) throws JRException
	{
		// // fill report with data
		if (selectedReport != null)
		{
			ReportFillWorker reportWorker = new ReportFillWorker(summary, selectedReport);

			JasperPrint jp = reportWorker.getJasperReport();

			if (jp != null)
			{
				String title = summary.getChapters().keySet().iterator().next(); // get
																										// first
				jp.setName("Report for " + title);
				JasperViewer jv = new JasperViewer(jp, false);
				jv.setTitle("Report for " + title);
				jv.setVisible(true);
				jv.setFitPageZoomRatio();
			}
			else
			{
				log.error("Errors in ReportTemplate!");
			}
		}
		else
		{
			log.error("Report do not exists!");
		}

	}

	private static class ReportFillWorker
	{

		private JasperPrint jp;
		private LReportTemplate report;
		private Summary summary;

		public ReportFillWorker(Summary summary, LReportTemplate selectedReport)
		{
			report = selectedReport;
			this.summary = summary;
		}

		public JasperPrint getJasperReport()
		{
			try
			{
				jp = createReport(summary, report);
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				jp = null;
			}

			return jp;
		}
	}

	protected static JasperPrint createReport(Summary summary, LReportTemplate selectedReport) throws JRException
	{
		log.debug("Creating report!");
		updateReport(selectedReport);

		LReportTemplate report = new LReportTemplate(selectedReport);

		JasperReport jr = compileReport(report);
		ReportProtocolFactory factory = new ReportProtocolFactory();

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(JRParameter.REPORT_URL_HANDLER_FACTORY, factory);

		return JasperFillManager.fillReport(jr, map, new SummaryDataSource(summary));
	}

	private static JasperReport compileReport(LReportTemplate report)
	{
		log.info("compile report");
		JasperReport jr = null;
		try
		{
			ByteArrayInputStream in = new ByteArrayInputStream(report.getData().getBytes());
			JasperDesign design = JRXmlLoader.load(in);

			jr = JasperCompileManager.compileReport(design);
		}
		catch (JRException e)
		{
			e.printStackTrace();
		}
		log.debug("Compiling report done.");
		return jr;
	}

	/*
	 * check if report template is changed and if it is reload it.
	 */
	private static void updateReport(LReportTemplate report)
	{
		report.update();
	}

}
