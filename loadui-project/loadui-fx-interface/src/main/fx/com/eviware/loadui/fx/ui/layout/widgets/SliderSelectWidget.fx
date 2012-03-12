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
/*
*SliderSelectWidget.fx
*
*Created on apr 15, 2010, 12:55:04 em
*/

package com.eviware.loadui.fx.ui.layout.widgets;

import javafx.scene.Group;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.geometry.BoundingBox;

import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.popup.TooltipHolder;
import com.eviware.loadui.fx.ui.dnd.SliderNode;
import com.eviware.loadui.fx.ui.layout.LabelProvider;
import com.eviware.loadui.fx.ui.layout.Widget;
import com.eviware.loadui.fx.ui.layout.widgets.support.SelectSupport;

/**
 * Select Widget which allows the user to choose between a set of predefined values.
 *
 * @author dain.nilsson
 */
public class SliderSelectWidget extends BaseNode, Widget, TooltipHolder, SelectSupport {

	public var backgroundStroke:Paint = Color.rgb( 0xe5, 0xe5, 0xe5 );
	public var backgroundFill:Paint = LinearGradient {
		endY: 0
		stops: [
			Stop { offset: 0, color: Color.rgb( 0x91, 0x92, 0x95 ) },
			Stop { offset: 1, color: Color.rgb( 0xd4, 0xd4, 0xd4 ) }
		]
	}
	public var handleStroke:Paint = Color.rgb( 0x65, 0x65, 0x65 );
	public var handleFill:Paint = LinearGradient {
		endX: 0
		stops: [
			Stop { offset: 0, color: Color.rgb( 0xdd, 0xdd, 0xdd ) },
			Stop { offset: 1, color: Color.rgb( 0xc0, 0xc0, 0xc0 ) }
		]
	}
	public var lineStroke1:Paint = Color.rgb( 0x58, 0x58, 0x58 );
	public var lineStroke2:Paint = Color.WHITE;

	public-init var labelProvider:LabelProvider = OptionsLabelProvider {};

	override var blocksMouse = true;
	
	def selectorHeight = bind height - labelNode.height;
	
	var initSelected:Integer;
	var tmpCnt = 0;
	
	def labels = VBox {
		height: bind selectorHeight
		width: bind width - 20
		layoutX: 18
		content: for( o in options ) labelProvider.labelFor( o );
	}
	
	override var options on replace {
		labels.content = for( o in options ) labelProvider.labelFor( o );
	}
	
	def labelNode = Label {
		width: bind width
		text: bind label
	}
	
	def switchBase = Group {
		content: [
			Rectangle {
				width: 14
				height: bind selectorHeight
				fill: backgroundStroke
			}, Rectangle {
				width: 13
				height: bind selectorHeight - 1
				fill: backgroundFill
			} 
		]
	}
	
	def switchHandle:SliderNode = SliderNode {
		height: bind selectorHeight - 2
		width: 12
		numOptions: bind sizeof options
		contentNode: Group {
			content:[
				Rectangle {
					x: 1
					y: 1
					width: 10
					height: 10
					fill: bind handleFill
					stroke: bind handleStroke 
					arcWidth: 4
					arcHeight: 4
				}, Line {
					startX: 3
					endX: 9
					layoutY: 6
					stroke: bind lineStroke1
				}, Line {
					startX: 3
					endX: 9
					layoutY: 7
					stroke: bind lineStroke2
				}
			]
		}
	}
	
	override var plc on replace {
		setPlc( plc );
		value = plc.getProperty().getValue();
	}
	
	override var value on replace {
		plc.getProperty().setValue( value );
		
		var index = 0;
		
		for( option in options ) {
			if(option == value)
				break;
			index++;
		}
		initSelected = if(sizeof options == 0) 0 else index mod sizeof options;
		/*
		 * for some reason this is not working for the first time( when this node is initializing )
		 * value for switchHandle.selectedIndex is always 0. So, this hack is to fix this.
		 * robert
		 */
		switchHandle.selectedIndex = initSelected;//if(sizeof options == 0) 0 else index mod sizeof options;
	}
	
	def selectedIndex = bind switchHandle.selectedIndex on replace {
	    if ( tmpCnt > 0) {
			value = options[selectedIndex];
	    } else {
	        switchHandle.selectedIndex = initSelected;
	        value = options[initSelected];
	        
	        tmpCnt++;
	    }
	}
	
	override var layoutBounds = bind lazy BoundingBox {
		minX: labelNode.layoutBounds.minX
		minY: labelNode.layoutBounds.minY
		width: width
		height: height
	}
	
	override function create() {
		labelNode.height = labelNode.getPrefHeight( -1 );
		
		Group {
			content: [
				labelNode,
				Group {
					layoutY: bind labelNode.height
					content: [
						switchBase,
						switchHandle,
						labels
					]
				}
			]
		}
	}
	
	override function getPrefWidth( height:Float ) {
		labels.getPrefWidth( -1 ) + 20
	}
	
	override function getPrefHeight( width:Float ) {
		sizeof options * 12 + labelNode.height
	}
}

class OptionsLabelProvider extends LabelProvider {
	override function labelFor( o:Object ):Label {
		Label {
			text: optionsProvider.labelFor( o )
			font: Font { size: 9 }
		}
	}
}
