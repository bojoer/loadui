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
package com.eviware.loadui.api.chart;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ChartModel {

	public static final int STYLE_BAR = 0;

	public static final int STYLE_LINE = 1;

	private int style = STYLE_LINE;

	private CustomAbstractRange xRange;

	private CustomAbstractRange yRange;
	
	private CustomAbstractRange y2Range;

	private String title = "";

	private int width = 200;

	private int height = 100;

	private List<ChartListener> chartListenerList;

	private ArrayList<ChartSerie> series;
	
	private int legendColumns = -1;
	
	public ChartModel(CustomAbstractRange xRange, CustomAbstractRange yRange,
			int width, int height) {
		this.xRange = xRange;
		this.yRange = yRange;
		this.width = width;
		this.height = height;
	}
	
	public ChartModel(CustomAbstractRange xRange, CustomAbstractRange yRange, CustomAbstractRange y2Range,
			int width, int height) {
		this.xRange = xRange;
		this.yRange = yRange;
		this.y2Range = y2Range;
		this.width = width;
		this.height = height;
	}

	public ChartModel(CustomAbstractRange xRange, CustomAbstractRange yRange) {
		this.xRange = xRange;
		this.yRange = yRange;
	}

	public void addPoint(int serieIndex, double x, double y) {
		Point p = new Point(x, y);
		if (series != null && serieIndex >= 0 && serieIndex < series.size() && series.get(serieIndex).isEnabled()) {
			firePointAddedToModel(series.get(serieIndex), p);
		}
	}

	public void clearSerie(String serieName) {
		for (int i = 0; i < series.size(); i++) {
			if(series.get(i).getName().equals(serieName)){
				fireSerieCleared(series.get(i));
				break;
			}
		}
	}

	public void clear() {
		for (int i = 0; i < series.size(); i++) {
			fireSerieCleared(series.get(i));
		}
		fireChartCleared();
	}
	
	public void enableSerie(String serieName, boolean enable) {
		for (int i = 0; i < series.size(); i++) {
			if(series.get(i).getName().equals(serieName)){
				series.get(i).setEnabled(enable);
				fireSerieEnabled(series.get(i));
				break;
			}
		}
	}

	public int getStyle() {
		return style;
	}

	public void setStyle(int style) {
		this.style = style;
	}

	public CustomAbstractRange getXRange() {
		return xRange;
	}

	public CustomAbstractRange getYRange() {
		return yRange;
	}
	
	public CustomAbstractRange getY2Range() {
		return y2Range;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public ArrayList<ChartSerie> getSeries() {
		return series;
	}

	public void addSerie(String name, boolean enabled, boolean defaultAxis) {
		if (series == null) {
			series = new ArrayList<ChartSerie>();
		}
		ChartSerie cs = new ChartSerie(name, enabled, defaultAxis);
		series.add(cs);
		cs.setIndex(series.size() - 1);
	}

	public int getSerieIndex(String serieName) {
		for (int i = 0; i < series.size(); i++) {
			if(series.get(i).getName().equals(serieName)){
				return i;
			}
		}
		return 0;
	}

	public ChartSerie getSerie(String serieName){
		for (int i = 0; i < series.size(); i++) {
			if(series.get(i).getName().equals(serieName)){
				return series.get(i);
			}
		}
		return null;
	}
	
	public void addChartListener(ChartListener chartListener) {
		if(chartListenerList == null){
			chartListenerList = new ArrayList<ChartListener>();
		}
		this.chartListenerList.add(chartListener);
	}

	private void firePointAddedToModel(ChartSerie cs, Point p) {
		if (chartListenerList != null) {
			for (Iterator<ChartListener> iterator = chartListenerList.iterator(); iterator.hasNext();) {
				iterator.next().pointAddedToModel(cs, p);
			}
		}
	}

	private void fireSerieCleared(ChartSerie cs) {
		if (chartListenerList != null) {
			for (Iterator<ChartListener> iterator = chartListenerList.iterator(); iterator.hasNext();) {
				iterator.next().serieCleared(cs);
			}
		}
	}
	
	private void fireChartCleared() {
		if (chartListenerList != null) {
			for (Iterator<ChartListener> iterator = chartListenerList.iterator(); iterator.hasNext();) {
				iterator.next().chartCleared();
			}
		}
	}

	private void fireSerieEnabled(ChartSerie cs) {
		if (chartListenerList != null) {
			for (Iterator<ChartListener> iterator = chartListenerList.iterator(); iterator.hasNext();) {
				iterator.next().serieEnabled(cs);
			}
		}
	}

	public int getLegendColumns() {
		if(legendColumns == -1){
			return getSeries().size();
		}
		else{
			return legendColumns;
		}
	}

	public void setLegendColumns(int legendColumns) {
		this.legendColumns = legendColumns;
	}

}
