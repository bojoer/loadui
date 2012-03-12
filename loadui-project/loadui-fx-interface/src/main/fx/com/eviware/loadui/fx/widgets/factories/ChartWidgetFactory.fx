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
package com.eviware.loadui.fx.widgets.factories;

import com.eviware.loadui.fx.widgets.componet.ChartWidget;

import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.layout.WidgetCreationException;
import com.eviware.loadui.api.layout.WidgetFactory;
import com.eviware.loadui.api.chart.ChartModel;

/**
 * @author predrag
 */
public class ChartWidgetFactory extends WidgetFactory {
    
	def PROPERTY_MODEL:String = "model";
    
	override public function getId(){
    	"chartWidget";
    }
    
    override public function buildWidget( layoutComponent:LayoutComponent  ) {
        if ( layoutComponent.has(PROPERTY_MODEL) ) {
            if ( layoutComponent.get(PROPERTY_MODEL) instanceof ChartModel ) {
            	ChartWidget {
                	chartModel: layoutComponent.get(PROPERTY_MODEL) as ChartModel 
                }
            } 
            else {
                throw new WidgetCreationException("[ChartWidget] DataModel is not ChartModel.");
            }
        } 
        else {
        	throw new WidgetCreationException("[ChartWidget] DataModel is null.");
        }
    }
    
    
}



















