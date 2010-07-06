/*
 * Copyright 2010 eviware software ab
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
import java.util.Arrays;
import java.util.HashMap;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.summary.Chapter;
import com.eviware.loadui.api.summary.Summary;
import com.eviware.loadui.util.reporting.datasources.ChapterDataSource;

public class ReportEngine
{

	static Logger log = LoggerFactory.getLogger(ReportEngine.class);

	public static String generateJasperReport(Chapter chapter, LReportTemplate selectedReport) throws JRException
	{
		// // fill report with data
		if (selectedReport != null)
		{
			ReportFillWorker reportWorker = new ReportFillWorker(chapter, selectedReport);

			JasperPrint jp = reportWorker.getJasperReport();

			if (jp != null)
			{
				jp.setName("Report for []");
				JasperViewer jv = new JasperViewer(jp, false);
				jv.setTitle("Report for []");
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

		return null;
	}

	private static class ReportFillWorker
	{

		private JasperPrint jp;
		private LReportTemplate report;
		private Chapter chapter;

		public ReportFillWorker(Chapter chapter, LReportTemplate selectedReport)
		{
			report = selectedReport;
			this.chapter = chapter;
		}

		public JasperPrint getJasperReport()
		{
			try
			{
				jp = createReport(chapter, report);
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				jp = null;
			}

			return jp;
		}
	}

	protected static JasperPrint createReport(Chapter chapter, LReportTemplate selectedReport) throws JRException
	{
		log.debug("Creating report!");
		updateReport(selectedReport);

		LReportTemplate report = new LReportTemplate(selectedReport);

		JasperReport jr = compileReport(report);
		ReportProtocolFactory factory = new ReportProtocolFactory();

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(JRParameter.REPORT_URL_HANDLER_FACTORY, factory);

		map.put("ChapterDataSource", new ChapterDataSource(chapter));

		return JasperFillManager.fillReport(jr, map, new JRBeanCollectionDataSource(Arrays.asList(chapter)));
	}

	private static JasperReport compileReport(LReportTemplate report)
	{
		log.info("compile report");
		JasperReport jr = null;
		try
		{
			ByteArrayInputStream in = new ByteArrayInputStream(report.getData().getBytes());
			JasperDesign design = JRXmlLoader.load(in);

			JRDesignParameter param = new JRDesignParameter();

			param = new JRDesignParameter();
			param.setName("ChapterDataSource");
			param.setValueClass(ChapterDataSource.class);
			design.addParameter(param);

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
