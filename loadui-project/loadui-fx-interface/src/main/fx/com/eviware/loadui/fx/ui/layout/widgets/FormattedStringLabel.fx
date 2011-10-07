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
*FormattedStringLabel.fx
*
*Created on apr 19, 2010, 16:07:53 em
*/

package com.eviware.loadui.fx.ui.layout.widgets;

import javafx.scene.Group;
import javafx.scene.layout.Resizable;
import javafx.scene.control.Label;
import javafx.geometry.BoundingBox;
import javafx.util.Math;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.util.layout.FormattedString;

import java.util.Observer;
import java.util.Observable;

public class FormattedStringLabel extends BaseNode, Resizable, Observer {
	
	def stringNode = Label {
		width: bind width
	}
	
	public-init protected var formattedString:FormattedString on replace {
		formattedString.addObserver( this );
		stringNode.text = formattedString.toString();
	}
	
	public-init protected var text:String = "";
	def labelNode = Label {
		text: bind text
		width: bind width
	}
	def labelOffset = labelNode.getPrefHeight( -1 );
	
	override function update( observable:Observable, arg:Object ) {
		FxUtils.runInFxThread( function():Void {
			stringNode.text = arg as String;
		} );
	}
	
	override var height on replace {
		stringNode.height = height - labelOffset;
	}
	
	override function create() {
		labelNode.height = labelOffset;
		stringNode.layoutY = labelOffset;
		
		Group {
			content: [ labelNode, stringNode ]
		}
	}
	
	override function getPrefHeight( width:Float ) {
		stringNode.getPrefHeight( width ) + labelOffset;
	}
	
	override function getPrefWidth( height:Float ) {
		Math.max( stringNode.getPrefWidth( height ), labelNode.getPrefWidth( height ) );
	}
}
