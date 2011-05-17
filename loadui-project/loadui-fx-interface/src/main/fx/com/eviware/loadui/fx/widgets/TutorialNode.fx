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
/*
*TutorialNode.fx
*
*/

package com.eviware.loadui.fx.widgets;


import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.dialogs.Dialog;

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Stack;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.*;

import org.slf4j.LoggerFactory;

import java.net.*;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.TutorialNode" );

/**
 * Node to display in the TutorialList representing a Tutorial.
 */
public class TutorialNode extends CustomNode {
    
	public var url:String;
	public var label:String;
	public var text:String;
	
	override var onMousePressed = function(e:MouseEvent) {
		 openURL(url)
	}	
	
	override var onMouseEntered = function(e:MouseEvent) {
	    this.cursor = javafx.scene.Cursor.HAND;
	}
	
	override var onMouseExited = function(e:MouseEvent) {
		this.cursor = javafx.scene.Cursor.DEFAULT;
	}
	
	override function create() {
		
		var url:String = null;	
	
		if ( text.indexOf("<img")> 0 ) {
			var start:Integer = text.indexOf("src=\"", text.indexOf("<img"));
			var end:Integer = text.indexOf("\"", start+5);
		    url = text.substring(start + 5, end);
		}
		
		Stack {
			content: [
				ImageView {
				    image: Image {
						url: url	
						backgroundLoading: true
						placeholder: Image {
				      	url: "{__ROOT__}images/png/tutorial-holder.png"
				      }
				    }
				    fitWidth: 130
					fitHeight: 98					    
				    smooth: true
				    cache: true
				}
			]
		} 
		
	}
	
	override function toString() { label }
}
