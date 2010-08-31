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
/*
*TutorialNode.fx
*
*Created on feb 10, 2010, 11:47:11 fm
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
import org.slf4j.LoggerFactory;
import javafx.scene.media.*;


public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.TutorialNode" );

/**
 * Node to display in the TutorialList representing a Tutorial.
 */
public class TutorialNode extends CustomNode {
    
	public var url:String;
	public var label:String;
	public var isMedia:Boolean = false;
	
	override var onMouseClicked = function(e:MouseEvent) {
	    if ( isMedia ) {
			var media = Media{
		    	source:url
		    }
		    	 
	    	var player = MediaPlayer{
	    		media:media,
	    		autoPlay:true
	    	}
		    def dialog:Dialog = Dialog {
    	        noCancel: true
    	        noOk: true
    	        onClose: function():Void {
    	            dialog.close();
    	        }
    	        title: label
    	        content: 
				    MediaView {
				    	mediaPlayer:player
				    }
    	    }
			
	    }
		else 
		    openURL(url)
	}	
	
	override function create() {
		
		Stack {
			content: [
				javafx.scene.shape.Rectangle {
				    width: 115
				    height: 98
				}
			]
		} 
		
	}
	
	override function toString() { label }
}
