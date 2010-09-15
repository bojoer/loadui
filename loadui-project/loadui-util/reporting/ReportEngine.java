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

import java.util.Arrays;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

import com.eviware.loadui.api.summary.Summary;

public class ReportEngine
{

	public static String generateJasperReport(Summary sumary, ReportTemplate selectedReport)
			throws JRException
	{
		// // fill report with data
		if (selectedReport != null)
		{
			ReportFillWorker reportWorker = new ReportFillWorker(selectedReport);
			try
			{
				//progressDialog.run(reportWorker);
			}
			catch (Exception e)
			{
			//	SoapUI.logError(e);
			}
			JasperPrint jp = reportWorker.getJasperReport();

			if (jp != null)
			{
				jp.setName("Report for []");
				JasperViewer jv = new JasperViewer(jp, false);
				jv.setTitle("Report for []");
				jv.setVisible(true);
				jv.setFitPageZoomRatio();
			}
		}

		return null;
	}
	
	private static class ReportFillWorker
	{

		private JasperPrint jp;
		private ReportTemplate report;

		public ReportFillWorker( ReportTemplate selectedReport )
		{
			report = selectedReport;
		}

		public Object construct( )
		{
			try
			{
				jp = createReport( null, report );
			}
			catch( Throwable e )
			{
				e.printStackTrace();
				jp = null;
			}

			return jp;
		}

		public JasperPrint getJasperReport()
		{
			return jp;
		}
	}
	
	protected static JasperPrint createReport( Summary summary, ReportTemplate selectedReport ) throws JRException, Exception
	{
		updateReport( selectedReport );

		ReportTemplate report = new ReportTemplate( selectedReport );

		JasperReport jr = compileReport( report );

		return JasperFillManager.fillReport( jr, null, new JRBeanCollectionDataSource( Arrays.asList( summary ) ) );
	}

	private static JasperReport compileReport(ReportTemplate report)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * check if report template is changed and if it is reload it. 
	 */
	private static void updateReport(ReportTemplate selectedReport)
	{
		// TODO Auto-generated method stub
		
	}
}
