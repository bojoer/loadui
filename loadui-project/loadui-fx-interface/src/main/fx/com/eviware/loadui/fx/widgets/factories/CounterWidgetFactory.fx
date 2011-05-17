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
package com.eviware.loadui.fx.widgets.factories;

import java.awt.Dimension;

import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.layout.WidgetCreationException;
import com.eviware.loadui.api.layout.WidgetFactory;
import com.eviware.loadui.api.property.Property;

import java.util.ArrayList;

import com.eviware.loadui.fx.widgets.componet.CounterWidget;
import com.eviware.loadui.util.collections.ObservableList;

/**
 * @author robert
 */
//DEPRECATED
public class CounterWidgetFactory extends WidgetFactory {
    
    override public function getId(){
        "counterWidget";
    }
        
    override public function buildWidget( layoutComponent:LayoutComponent  ) {
        CounterWidget {
            ccounters: layoutComponent.get('counts') as ArrayList
            component: layoutComponent.get('counters') as ObservableList
            work: bind layoutComponent.get('onOff') as Property
        }
    }
}
