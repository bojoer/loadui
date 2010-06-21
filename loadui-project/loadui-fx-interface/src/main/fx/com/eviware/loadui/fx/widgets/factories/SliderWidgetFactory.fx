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
package com.eviware.loadui.fx.widgets.factories;
/*
*SliderWidgetFactory.fx
*
*Created on Apr 20, 2010, 19:44:46 PM
*/
import com.eviware.loadui.fx.widgets.componet.SliderWidget;

import com.eviware.loadui.fx.ui.layout.PropertyLayoutComponentNode;

import com.eviware.loadui.api.layout.WidgetCreationException;
import com.eviware.loadui.api.layout.WidgetFactory;
import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.layout.PropertyLayoutComponent;
import com.eviware.loadui.api.events.PropertyEvent;

public class SliderWidgetFactory extends WidgetFactory {

    override public function getId(){
        "sliderWidget";
    }
    
    override function buildWidget( lc:LayoutComponent ) {
        if( lc instanceof PropertyLayoutComponent ) {
            def plc = lc as PropertyLayoutComponent;
            if( plc.getProperty().getType() == Integer.class )
            return PropertyLayoutComponentNode {
                layoutComponent: plc
                widget:SliderWidget { plc: plc }
            }
        }
        
        throw new WidgetCreationException("The SliderWidgetFacory widget requires a PropertyLayoutComponent with a Property of type Integer!");
    }
};
