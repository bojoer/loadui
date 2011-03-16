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
package com.eviware.loadui.fx.statistics.toolbar;

import javafx.scene.image.Image;

import com.eviware.loadui.fx.FxUtils.__ROOT__;
import com.eviware.loadui.fx.statistics.toolbar.items.AnalysisToolbarItem;

import com.eviware.loadui.util.StringUtils;

public def RESPONSE_TIMES = AnalysisToolbarItem {
	label: "Response Times"
	icon: Image { url: "{__ROOT__}images/png/response-time.png" }
	templateScript: StringUtils.multiline(
		"import com.eviware.loadui.api.model.ComponentItem",
		"import com.eviware.loadui.api.model.ProjectItem",
		"import com.eviware.loadui.api.component.categories.RunnerCategory",
		"",
		"if( statisticHolder instanceof ComponentItem && statisticHolder.behavior instanceof RunnerCategory ) \{",
		"    chartGroup.type = 'com.eviware.loadui.api.statistics.model.chart.LineChartView'",
		"    def chart = chartGroup.createChart( statisticHolder )",
		"    def chartView = chartGroup.getChartViewForChart( chart )",
		"    chartView.addSegment( 'Time Taken', 'AVERAGE', 'main' )",
		"\} else if( statisticHolder instanceof ProjectItem ) \{",
		"    chartGroup.type = 'com.eviware.loadui.api.statistics.model.chart.LineChartView'",
		"    def chart = chartGroup.createChart( statisticHolder )",
		"    def chartView = chartGroup.getChartViewForChart( chart )",
		"    chartView.addSegment( 'Requests', 'PER_SECOND', 'main' )",
		"\}"
	)
}

public def REQUEST_THROUGHPUT = AnalysisToolbarItem {
	label: "Request Throughput"
	icon: Image { url: "{__ROOT__}images/png/request-throughput.png" }
	templateScript: StringUtils.multiline(
		"import com.eviware.loadui.api.model.ComponentItem",
		"import com.eviware.loadui.api.model.ProjectItem",
		"import com.eviware.loadui.api.component.categories.RunnerCategory",
		"",
		"if( statisticHolder instanceof ComponentItem && statisticHolder.behavior instanceof RunnerCategory ) \{",
		"    chartGroup.type = 'com.eviware.loadui.api.statistics.model.chart.LineChartView'",
		"    def chart = chartGroup.createChart( statisticHolder )",
		"    def chartView = chartGroup.getChartViewForChart( chart )",
		"    chartView.addSegment( 'Throughput', 'TPS', 'main' )",
		"\} else if( statisticHolder instanceof ProjectItem ) \{",
		"    chartGroup.type = 'com.eviware.loadui.api.statistics.model.chart.LineChartView'",
		"    def chart = chartGroup.createChart( statisticHolder )",
		"    def chartView = chartGroup.getChartViewForChart( chart )",
		"    chartView.addSegment( 'Requests', 'PER_SECOND', 'main' )",
		"\}"
	)
}

public def REQUEST_PERCENTILE = AnalysisToolbarItem {
	label: "Request Percentile"
	icon: Image { url: "{__ROOT__}images/png/request-percentile.png" }
	templateScript: StringUtils.multiline(
		"import com.eviware.loadui.api.model.ComponentItem",
		"import com.eviware.loadui.api.model.ProjectItem",
		"import com.eviware.loadui.api.component.categories.RunnerCategory",
		"",
		"if( statisticHolder instanceof ComponentItem && statisticHolder.behavior instanceof RunnerCategory ) \{",
		"    chartGroup.type = 'com.eviware.loadui.api.statistics.model.chart.LineChartView'",
		"    def chart = chartGroup.createChart( statisticHolder )",
		"    def chartView = chartGroup.getChartViewForChart( chart )",
		"    chartView.addSegment( 'Time Taken', 'PERCENTILE_25TH', 'main' )",
		"    chartView.addSegment( 'Time Taken', 'MEDIAN', 'main' )",
		"    chartView.addSegment( 'Time Taken', 'PERCENTILE_75TH', 'main' )",
		"    chartView.addSegment( 'Time Taken', 'PERCENTILE_90TH', 'main' )",
		"\} else if( statisticHolder instanceof ProjectItem ) \{",
		"    chartGroup.type = 'com.eviware.loadui.api.statistics.model.chart.LineChartView'",
		"    def chart = chartGroup.createChart( statisticHolder )",
		"    def chartView = chartGroup.getChartViewForChart( chart )",
		"    chartView.addSegment( 'Requests', 'PER_SECOND', 'main' )",
		"\}"
	)
}

public def BYTES_THROUGHPUT = AnalysisToolbarItem {
	label: "Bytes Throughput"
	icon: Image { url: "{__ROOT__}images/png/bytes-throughput.png" }
	templateScript: StringUtils.multiline(
		"import com.eviware.loadui.api.model.ComponentItem",
		"import com.eviware.loadui.api.model.ProjectItem",
		"import com.eviware.loadui.api.component.categories.RunnerCategory",
		"",
		"if( statisticHolder instanceof ComponentItem && statisticHolder.behavior instanceof RunnerCategory ) \{",
		"    chartGroup.type = 'com.eviware.loadui.api.statistics.model.chart.LineChartView'",
		"    def chart = chartGroup.createChart( statisticHolder )",
		"    def chartView = chartGroup.getChartViewForChart( chart )",
		"    chartView.addSegment( 'Throughput', 'BPS', 'main' )",
		"\} else if( statisticHolder instanceof ProjectItem ) \{",
		"    chartGroup.type = 'com.eviware.loadui.api.statistics.model.chart.LineChartView'",
		"    def chart = chartGroup.createChart( statisticHolder )",
		"    def chartView = chartGroup.getChartViewForChart( chart )",
		"    chartView.addSegment( 'Requests', 'PER_SECOND', 'main' )",
		"\}"
	)
}

public def REQUEST_ERRORS = AnalysisToolbarItem {
	label: "Request Errors"
	icon: Image { url: "{__ROOT__}images/png/request-errors.png" }
	templateScript: StringUtils.multiline(
		"import com.eviware.loadui.api.model.ComponentItem",
		"import com.eviware.loadui.api.model.ProjectItem",
		"import com.eviware.loadui.api.component.categories.RunnerCategory",
		"",
		"if( statisticHolder instanceof ComponentItem && statisticHolder.behavior instanceof RunnerCategory ) \{",
		"    chartGroup.type = 'com.eviware.loadui.api.statistics.model.chart.LineChartView'",
		"    def chart = chartGroup.createChart( statisticHolder )",
		"    def chartView = chartGroup.getChartViewForChart( chart )",
		"    chartView.addSegment( 'Failures', 'PER_SECOND', 'main' )",
		"\} else if( statisticHolder instanceof ProjectItem ) \{",
		"    chartGroup.type = 'com.eviware.loadui.api.statistics.model.chart.LineChartView'",
		"    def chart = chartGroup.createChart( statisticHolder )",
		"    def chartView = chartGroup.getChartViewForChart( chart )",
		"    chartView.addSegment( 'Requests', 'PER_SECOND', 'main' )",
		"    chartView.addSegment( 'Total Failures', 'PER_SECOND', 'main' )",
		"\}",
	)
}

public def ASSERTION_FAILURES = AnalysisToolbarItem {
	label: "Assertion Failures"
	icon: Image { url: "{__ROOT__}images/png/assertion-failures.png" }
	templateScript: StringUtils.multiline(
		"import com.eviware.loadui.api.model.ProjectItem",
		"",
		"def variable = statisticHolder.getStatisticVariable('Assertion Failures')",
		"if( variable?.statisticNames?.contains( 'PER_SECOND' ) ) \{",
		"    chartGroup.type = 'com.eviware.loadui.api.statistics.model.chart.LineChartView'",
		"    def chart = chartGroup.createChart( statisticHolder )",
		"    def chartView = chartGroup.getChartViewForChart( chart )",
		"    chartView.addSegment( 'Assertion Failures', 'PER_SECOND', 'main' )",
		"\}",
		"if( statisticHolder instanceof ProjectItem ) \{",
		"    chartGroup.type = 'com.eviware.loadui.api.statistics.model.chart.LineChartView'",
		"    def chart = chartGroup.createChart( statisticHolder )",
		"    def chartView = chartGroup.getChartViewForChart( chart )",
		"    chartView.addSegment( 'Requests', 'PER_SECOND', 'main' )",
		"\}",
	)
}

public def RUNNING_REQUESTS = AnalysisToolbarItem {
	label: "Running Requests"
	icon: Image { url: "{__ROOT__}images/png/running-requests.png" }
	templateScript: StringUtils.multiline(
		"import com.eviware.loadui.api.model.ComponentItem",
		"import com.eviware.loadui.api.component.categories.RunnerCategory",
		"",
		"if( statisticHolder instanceof ComponentItem && statisticHolder.behavior instanceof RunnerCategory ) \{",
		"    chartGroup.type = 'com.eviware.loadui.api.statistics.model.chart.LineChartView'",
		"    def chart = chartGroup.createChart( statisticHolder )",
		"    def chartView = chartGroup.getChartViewForChart( chart )",
		"    chartView.addSegment( 'Running', 'VALUE', 'main' )",
		"\}",
	)
}

public def ALL = [
	RESPONSE_TIMES, REQUEST_THROUGHPUT, REQUEST_PERCENTILE, BYTES_THROUGHPUT, REQUEST_ERRORS, ASSERTION_FAILURES, RUNNING_REQUESTS
];