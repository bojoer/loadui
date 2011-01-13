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
package com.eviware.loadui.fx.statistics.chart.line;

import javafx.scene.layout.HBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.geometry.Insets;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.FxUtils.*;

import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.events.BaseEvent;

public def ZOOM_DEFAULT = "Seconds";
public def ZOOM_LEVEL = "com.eviware.loadui.fx.statistics.chart.line.ZoomPanel@zoomLevel";
public def ZOOM_LEVEL_ATTRIBUTE = "zoomLevel";

def allImage = Image { url: "{__ROOT__}images/png/all.png" };
def weekImage = Image { url: "{__ROOT__}images/png/week.png" };
def dayImage = Image { url: "{__ROOT__}images/png/day.png" };
def hoursImage = Image { url: "{__ROOT__}images/png/hours.png" };
def minutesImage = Image { url: "{__ROOT__}images/png/minutes.png" };
def secondsImage = Image { url: "{__ROOT__}images/png/seconds.png" };

def buttonInfo = LayoutInfo { hfill: true, hgrow: Priority.ALWAYS };

public class ZoomPanel extends HBox {
	public-init var chartGroup:ChartGroup;
	
	override var hpos = HPos.CENTER;
	override var vpos = VPos.CENTER;
	override var nodeVPos = VPos.BOTTOM;
	override var padding = Insets { top: 10, right: 25, bottom: 15, left: 25 };
	
	def toggleGroup = new ToggleGroup();
	def selectedLevel = bind toggleGroup.selectedToggle on replace {
		if( selectedLevel.value != null ) {
			chartGroup.setAttribute( ZOOM_LEVEL_ATTRIBUTE, "{selectedLevel.value}" );
			chartGroup.fireEvent( new BaseEvent( chartGroup, ZOOM_LEVEL ) );
		}
	}
	
	init {
		content = [
			Region { managed: false, width: bind width, height: bind height, styleClass: "zoom-panel" },
			buildButton( "All", allImage ),
			buildButton( "Weeks", weekImage ),
			buildButton( "Days", dayImage ),
			buildButton( "Hours", hoursImage ),
			buildButton( "Minutes", minutesImage ),
			buildButton( ZOOM_DEFAULT, secondsImage )
		];
	}
	
	function buildButton( text:String, image:Image ) {
		RadioButton {
			value: text
			text: text
			toggleGroup: toggleGroup
			selected: chartGroup.getAttribute( ZOOM_LEVEL_ATTRIBUTE, ZOOM_DEFAULT ) == text
			graphic: ImageView { image: image }
			layoutInfo: buttonInfo
			styleClass: "zoom-panel-button"
		}
	}
}