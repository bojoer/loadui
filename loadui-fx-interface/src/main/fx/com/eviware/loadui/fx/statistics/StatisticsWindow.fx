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
package com.eviware.loadui.fx.statistics;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.stage.StageStyle;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

import com.eviware.loadui.api.model.ProjectItem;

var instance:StatisticsWindow;

public function getInstance():StatisticsWindow {
  if (instance == null) {
    instance = StatisticsWindow {};
  }
  return instance;
}

public class StatisticsWindow {

	public var stage:Stage;
	public var project:ProjectItem;
	var closed:Boolean = true;
	var scene:Scene;
	
	public function show() {
		
    	if ( closed )
    		stage = Stage {
    			height: 600
    			width: 600
		    	title: "Statistics"
		    	scene: scene = Scene {
				        content: [
				        	Rectangle {
				        		x:1
				        		y:1
				        		width: bind scene.width -2
				        		height: 21
				        		fill: Color.web("#b1b1b1")
				        		stroke: Color.web("#000000")
				        		strokeWidth: 1
				        	}, Text {
				        		x: 20
				        		y: 15
				        		content: "Stats"
				        		font: Font { size: 10 }
				        	}, Rectangle {
				        		x: 1
				        		y: 22
				        		width: bind scene.width -2
				        		height: 90
				        		fill: Color.web("#989898")
				        		stroke: Color.web("#000000")
				        		strokeWidth: 1
				        	}, Text {
				        		x: 20
				        		y: 55
				        		content: "Stats: project {project.getLabel()}"
				        		font: Font { size: 30 }
				        		fill: Color.web("#ffffff")
				        	}
				        ]
				        fill: Color.web("#373737")
	    			   }
	    		onClose: function() {
	    		 	closed = true;
	//    				throw new com.eviware.loadui.util.hacks.PreventClosingStageException(); // this a hack to keep stage open
	  				}
	    		
	    		};
    	closed = false;
	}
	
	public function close() {
		stage.close()
	}
	
}
	

