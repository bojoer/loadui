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

import java.io.File;
import java.io.FilenameFilter;
import java.util.TreeMap;

import net.sf.jasperreports.engine.JRException;

import com.eviware.loadui.api.summary.Chapter;
import com.eviware.loadui.api.summary.Summary;

public class JasperReportManager {

	private static JasperReportManager _instance = null;
	
	private static final File reportDirectory = new File("reports");
	
	private TreeMap<String, LReportTemplate> reports = new TreeMap<String, LReportTemplate>(); 
	
	private JasperReportManager() {
		// load report templates
		loadReports();
	}
	
	/*
	 * Need to be singleton, since it works with file system and init could done only once.
	 */
	public static JasperReportManager getInstance() {
		if( _instance == null ) 
			_instance = new JasperReportManager();
		return _instance;
	}
	
	// loads all jasper reports from reports dir
	private void loadReports() {
		File[] reports = reportDirectory.listFiles(new FilenameFilter() 
		{
			
			@Override
			public boolean accept(File dir, String name)
			{
				return name.endsWith("jrxml");
			}
		});
		for( File reportTemplate : reports ) {
			this.reports.put(reportTemplate.getName().replace(".jrxml", ""), new LReportTemplate(reportTemplate.getName(), reportTemplate));
		}
	}
	
	public void createReport( Chapter chapter) {
		System.out.println("Create report");
		try
		{
			ReportEngine.generateJasperReport(chapter, reports.get("SummaryReport"));
		}
		catch (JRException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public LReportTemplate getReport(String name) {
		LReportTemplate result = reports.get(name);
		result.update();
		return result;
	}
}
