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

package com.eviware.loadui.api.charting.line;

import java.awt.Color;

import com.eviware.loadui.api.statistics.model.chart.LineChartView.LineSegment;

/**
 * A line in a LineChart, which can be styled.
 * 
 * @author dain.nilsson
 */
public interface LineSegmentModel
{
	public static final String SCALE = "scale";
	public static final String COLOR = "color";
	public static final String STROKE = "stroke";
	public static final String WIDTH = "width";

	public LineSegment getLineSegment();

	public int getScale();

	public void setScale( int scale );

	public Color getColor();

	public void setColor( Color color );

	public StrokeStyle getStrokeStyle();

	public void setStrokeStyle( StrokeStyle strokeStyle );

	public int getStrokeWidth();

	public void setStrokeWidth( int strokeWidth );
}