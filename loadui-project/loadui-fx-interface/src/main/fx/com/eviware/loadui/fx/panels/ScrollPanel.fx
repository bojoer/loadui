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
package com.eviware.loadui.fx.panels;

/**
 * @author robert
 */


import javafx.scene.control.ScrollBar;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.ClipView;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.CustomNode;
import javafx.scene.Node;

public class ScrollPanel extends CustomNode, Resizable {
    
    public var content:Node;
    
    var h = bind content.layoutBounds.width on replace {
        hScroll.max = if ( width < content.layoutBounds.width ) content.layoutBounds.width - width else 0;
        hScroll.visible = width < content.layoutBounds.width;
    }
    
    var w = bind content.layoutBounds.height on replace {
        vScroll.max = if ( height < content.layoutBounds.height ) content.layoutBounds.height - height else 0;
        vScroll.visible = height < content.layoutBounds.height;
    }
    
    var hScroll:ScrollBar;
    var vScroll:ScrollBar;
    
    override function getPrefHeight( width:Float ) {
        height;
    }
        
    override function getPrefWidth( height:Float ) {
        width;
    }
        
    override public function create():Node {
        hScroll = ScrollBar {
	            min: 0
	            max: if ( width < content.layoutBounds.width ) content.layoutBounds.width - width else 0
	            value: 0
	            vertical: false
	            layoutInfo: LayoutInfo {
	                            width: width 
	                        }
	            visible: width < content.layoutBounds.width
	        }
        vScroll = ScrollBar {
            min: 0
            max: if ( height < content.layoutBounds.height ) content.layoutBounds.height - height else 0
            value: 0
            vertical: true
            layoutInfo: LayoutInfo {
                            height: height
                        }
            visible: height < content.layoutBounds.height
        }
        // view 
        var scrollClipView:ClipView = ClipView {
                    clipX: bind hScroll.value
                    clipY: bind vScroll.value
                    node: content
                    pannable: false
                    layoutInfo: LayoutInfo {
                            width: width 
                            height: height
                        }
                } 
        // add vertical scroll bar        
        var hBox = HBox {
            content: [
                scrollClipView, vScroll
            ]
        }
        // add horizontal bar
          var vBox = VBox {
              content: [
                  hBox, hScroll
              ]
          }
        return vBox;
    }
}
