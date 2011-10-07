/*
 * Copyright 2011 SmartBear Software
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
package com.eviware.loadui.api.reporting;

import java.awt.Image;
import java.io.File;
import java.util.Collection;
import java.util.Map;

import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.summary.Summary;

/**
 * Generates reports.
 * 
 * @author dain.nilsson
 */
public interface ReportingManager
{
	/**
	 * Creates a report for the given Summary and displays it to the user.
	 * 
	 * @param summary
	 */
	public void createReport( Summary summary );

	/**
	 * Creates a report for the given Summary and saves it to the specified file,
	 * using the specified format.
	 * 
	 * @param summary
	 * @param file
	 * @param format
	 */
	public void createReport( Summary summary, File file, String format );

	/**
	 * Creates a Statistics Report using the given label, for the given
	 * StatisticPages. The report is based in the Execution data and the given
	 * charts. The report is shown to the user.
	 * 
	 * @param label
	 * @param execution
	 * @param pages
	 * @param charts
	 */
	public void createReport( String label, Execution execution, Collection<StatisticPage> pages,
			Map<Object, Image> charts );

	/**
	 * Creates a Statistics Report using the given label, for the given
	 * StatisticPages. The report is based in the Execution data and the given
	 * charts. The report is saved to the given file using the format specified.
	 * 
	 * @param label
	 * @param execution
	 * @param pages
	 * @param charts
	 * @param file
	 * @param format
	 */
	public void createReport( String label, Execution execution, Collection<StatisticPage> pages,
			Map<Object, Image> charts, File file, String format );

	/**
	 * Creates a Statistics Report (and prepends another report to it) using the given
	 * label, for the given StatisticPages. The report is based in the Execution
	 * data and the given charts. The report is shown to the user.
	 * 
	 * @param label
	 * @param execution
	 * @param pages
	 * @param charts
	 * @param jpFileToPrepend
	 */
	public void createReport( String label, Execution execution, Collection<StatisticPage> pages,
			Map<Object, Image> charts, File jpFileToPrepend );

	/**
	 * Creates a Statistics Report (and prepends another report to it) using the given
	 * label, for the given StatisticPages. The report is based in the Execution
	 * data and the given charts. The report is saved to the given file using the
	 * format specified.
	 * 
	 * @param label
	 * @param execution
	 * @param pages
	 * @param charts
	 * @param file
	 * @param format
	 * @param jpFileToPrepend
	 */
	public void createReport( String label, Execution execution, Collection<StatisticPage> pages,
			Map<Object, Image> charts, File file, String format, File jpFileToPrepend );

}
