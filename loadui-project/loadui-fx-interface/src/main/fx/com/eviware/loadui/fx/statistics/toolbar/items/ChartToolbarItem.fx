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
package com.eviware.loadui.fx.statistics.toolbar.items;

import com.eviware.loadui.fx.statistics.toolbar.StatisticsToolbarItem;
import com.eviware.loadui.fx.FxUtils.*;

import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.fx.AppState;

import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.MainWindow;
import javafx.scene.text.Text;

def iconImage = Image { url: "{__ROOT__}images/png/line-chart.png" };

public class ChartToolbarItem extends StatisticsToolbarItem {
	
	public-init var type:String;
	
	override var icon = iconImage;
	
	override var category = "GRAPHS";
	
}
