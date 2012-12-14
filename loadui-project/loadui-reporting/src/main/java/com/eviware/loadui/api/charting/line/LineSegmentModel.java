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

import com.eviware.loadui.api.statistics.model.chart.line.LineSegment;

/**
 * A line in a LineChart, which can be styled.
 * 
 * @author dain.nilsson
 */
public interface LineSegmentModel extends SegmentModel.MutableStrokeWidth, SegmentModel.MutableStrokeStyle
{
	public static final String SCALE = "scale";

	@Override
	public LineSegment getSegment();

	public int getScale();

	public void setScale( int scale );
}