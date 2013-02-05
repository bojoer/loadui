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
package com.eviware.loadui.fx.widgets.componet;   

import com.eviware.loadui.api.chart.ChartSerie;
import com.eviware.loadui.api.chart.ChartModel;

import com.jidesoft.chart.Chart;
import com.jidesoft.chart.annotation.AutoPositionedLabel;
import com.jidesoft.chart.axis.Axis;
import com.jidesoft.chart.axis.TimeAxis;
import com.jidesoft.chart.model.ChartPoint;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.style.ChartStyle;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.range.NumericRange;
import com.jidesoft.range.Range;
import com.jidesoft.range.TimeRange;
import com.jidesoft.range.AbstractRange;
import com.jidesoft.chart.Legend;

import com.jidesoft.chart.PointShape;
import com.jidesoft.chart.event.PointSelection;
import com.jidesoft.chart.model.*;
import com.jidesoft.plaf.LookAndFeelFactory;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import javax.swing.*;
import java.awt.*;

import java.util.HashMap;
import java.util.Iterator;

//@Deprecated   
public class ChartMouseListener extends MouseMotionListener {
	
	public-init var chartModel: ChartModel;
    public-init var chart: Chart;
    public-init var models: HashMap;
	
    public override function mouseDragged(e: MouseEvent): Void {}

    public override function mouseMoved(e: MouseEvent): Void {
        rollover(e);
    }

    function rollover(e: MouseEvent): Void {
        def p: Point = e.getPoint();
        
        var model: DefaultChartModel;
        
        var tooltip: String = "";
        
        var keys: Iterator = models.keySet().iterator();
		while(keys.hasNext()){
			var cs: ChartSerie = chartModel.getSerie(keys.next() as String);
			if(cs != null and cs.isEnabled()){
				model = models.get(cs.getName()) as DefaultChartModel;
				def selected: Chartable = chart.nearestPoint(p, model).getSelected();
				tooltip += String.format("%s: %.1f ", cs.getName(), selected.getY().position());
			}
		}
		
		chart.setToolTipText(tooltip);
	
        chart.repaint();
    }
}
