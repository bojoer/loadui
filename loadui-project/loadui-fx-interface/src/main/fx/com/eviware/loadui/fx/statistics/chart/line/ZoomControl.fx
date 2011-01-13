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

import javafx.scene.layout.*;
import javafx.scene.CustomNode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polygon;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Stop;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.geometry.*;
import javafx.scene.transform.Rotate;

import com.eviware.loadui.fx.FxUtils.*;

public class ZoomControl extends CustomNode, Resizable {

	def buttonGroup:ToggleGroup = new ToggleGroup();

	override var width on replace { 
		requestLayout(); 
		getPPoints();
	}
    override var height on replace { 
    	requestLayout();
    	getPPoints();
    }
	
	var oldRec:Rectangle;
	var sellected:Toggle = bind buttonGroup.selectedToggle on replace {
		if( oldRec != null ) {
			oldRec.fill = Color.TRANSPARENT;
			oldRec.stroke = Color.TRANSPARENT;
		}
		oldRec = sellected.value as Rectangle;
		oldRec.fill = Color.web("#1c1c1c");
		oldRec.stroke = Color.web("#141414");
		scale = (buttonGroup.selectedToggle as ToggleButton).text;
	}
	
	// scale should be monitored to set time scale on graph
	public-read var scale:String = "";
	var ppoints;
	
	function getPPoints():Void {
		ppoints = [60,height - 50, 18,140, width-18,140, width-60, height-50];
	}
	
	init {
		height = 150;
		getPPoints();
		var rec:Rectangle;
	// it is in init since buttonToggle needs to be init before children
		children = [
				Stack {
					content: [
						Rectangle {
			                    x: 0, y: 0
			                    width: bind width, height: bind height
			                    fill: Color.web("#262626");
			            }, Polygon {
			            	points: bind ppoints
			            	fill: LinearGradient {
								startX: 0.0
								startY: 0.3
								endX: 0.0
								endY: 1.0
								stops: [
									Stop {
				                        color: Color.web('#262626')
				                        offset: 0.0
			                		},
					                Stop {
					                        color: Color.web('#363636')
					                        offset: 1.0
									}]
								}
			            }, HBox {
			            	nodeVPos: VPos.CENTER
			            	padding: Insets{ left:150 top:18 right:50 bottom:30}
			            	content:[
			            		VBox {
			            			nodeHPos: HPos.CENTER
			            			spacing: 10
			            			content: [
			            				Stack {
			            					content: [
			            						rec = Rectangle { arcHeight: 10, arcWidth:10, 
			            										  x:0, y:0, width:60, height:60, 
			            										  fill:Color.TRANSPARENT, strokeWidth:3 },
					            				ImageView {
													image: Image { url: "{__ROOT__}images/png/all.png" }
													onMouseClicked: function(e) {
														buttonGroup.toggles[0].selected = true;
													}
												}]
										}, ToggleButton {
											toggleGroup:buttonGroup
							            	text: "All"
							            	value: rec
							            }
			            			]	
			            		}, VBox {
			            			nodeHPos: HPos.CENTER
			            		    spacing: 10
			            			content: [
			            				Stack {
			            					content: [
			            						rec = Rectangle { arcHeight: 10, arcWidth:10, 
			            										  x:0, y:0, width:60, height:60, 
			            										  fill:Color.TRANSPARENT, strokeWidth:3 },
					            				ImageView {
													image: Image { url: "{__ROOT__}images/png/week.png" }
													onMouseClicked: function(e) {
														buttonGroup.toggles[1].selected = true;
													}
												}]
										}, ToggleButton {
											toggleGroup:buttonGroup
							            	text: "Weeks"
							            	value: rec
							            }
			            			]	
			            		}, VBox {
			            			nodeHPos: HPos.CENTER
			            		    spacing: 10
			            			content: [
			            				Stack {
			            					content: [
			            						rec = Rectangle { arcHeight: 10, arcWidth:10, 
			            										  x:0, y:0, width:60, height:60, 
			            										  fill:Color.TRANSPARENT, strokeWidth:3 },
					            				ImageView {
													image: Image { url: "{__ROOT__}images/png/day.png" }
													onMouseClicked: function(e) {
														buttonGroup.toggles[2].selected = true;
													}
												}]
										}, ToggleButton {
											toggleGroup:buttonGroup
							            	text: "Days"
							            	value: rec
							            }
			            			]	
			            		}, VBox {
			            			nodeHPos: HPos.CENTER
			            		    spacing: 10
			            			content: [
			            				Stack {
			            					content: [
			            						rec = Rectangle { arcHeight: 10, arcWidth:10, 
			            										  x:0, y:0, width:60, height:60, 
			            										  fill:Color.TRANSPARENT, strokeWidth:3 },
					            				ImageView {
													image: Image { url: "{__ROOT__}images/png/hours.png" }
													onMouseClicked: function(e) {
														buttonGroup.toggles[3].selected = true;
													}
												}]
										}, ToggleButton {
											toggleGroup:buttonGroup
							            	text: "Hours"
							            	value: rec
							            }
			            			]	
			            		}, VBox {
			            			nodeHPos: HPos.CENTER
			            		    spacing: 10
			            			content: [
			            				Stack {
			            					content: [
			            						rec = Rectangle { arcHeight: 10, arcWidth:10, 
			            										  x:0, y:0, width:60, height:60, 
			            										  fill:Color.TRANSPARENT, strokeWidth:3 },
					            				ImageView {
													image: Image { url: "{__ROOT__}images/png/minutes.png" }
													onMouseClicked: function(e) {
														buttonGroup.toggles[4].selected = true;
													}
												}]
										}, ToggleButton {
											toggleGroup:buttonGroup
							            	text: "Minutes"
							            	value: rec
							            }
			            			]	
			            		}, VBox {
			            			nodeHPos: HPos.CENTER
			            		    spacing: 10
			            			content: [
			            				Stack {
			            					content: [
			            						rec = Rectangle { arcHeight: 10, arcWidth:10, 
			            										  x:0, y:0, width:60, height:60, 
			            										  fill:Color.TRANSPARENT, strokeWidth:3 },
					            				ImageView {
													image: Image { url: "{__ROOT__}images/png/seconds.png" }
													onMouseClicked: function(e) {
														buttonGroup.toggles[5].selected = true;
													}
												}]
										}, ToggleButton {
											toggleGroup:buttonGroup
							            	text: "Seconds"
							            	value: rec
							            }
			            			]	
			            		}	
			            	]
			            }]
	            }
				];
		buttonGroup.toggles[5].selected = true;
	}
	
						
    override var layoutBounds = bind BoundingBox {
         minX: 0 
         minY: 0
         width: this.width
         height: this.height
    }

	override function getPrefWidth(height:Number):Number {
    	width
    }
    
    override function getPrefHeight(width:Number):Number {
		height
    }

}