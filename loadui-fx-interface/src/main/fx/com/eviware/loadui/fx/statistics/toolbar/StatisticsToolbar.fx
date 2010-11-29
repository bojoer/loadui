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
package com.eviware.loadui.fx.statistics.toolbar;

import com.eviware.loadui.fx.ui.toolbar.Toolbar;

import com.eviware.loadui.fx.statistics.toolbar.items.*;
import com.eviware.loadui.fx.statistics.StatisticsWindow;

import java.util.Collection;

import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;

public class StatisticsToolbar extends Toolbar {
    
    public var project: ProjectItem on replace {
     	if(project != null){
        var components: Collection = project.getComponents();
        for(c in components){
            if((c as ComponentItem).getStatisticVariableNames().size()>0){
                addItem(ComponentToolbarItem {
						component: c as ComponentItem
                });
            }
        }
     	}   
    }
    
    override var toolbarTitle = "Statictics";
    
    override var groupOrder = StatisticsGroupOrder{};
    
    override var itemOrder = StatisticsItemOrder{};
}