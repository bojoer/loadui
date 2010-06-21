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
package com.eviware.loadui.fx.ui.tabs;

/**
 * @author robert
 */

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.fxd.FXDNode;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;

import com.eviware.loadui.fx.FxUtils.*;

public class TabButton extends CustomNode {
    
    public var text:String;
    public var action:function();
    
    public var selected:Boolean = false;
    var left:FXDNode;
    var middle:FXDNode;
    var right:FXDNode;
    var content:Group;
    var label:Text;
    
    var contentNormal = Group {
                    content: [
                        label = Text {
                            layoutX: bind middle.boundsInParent.minX + 5
                            layoutY: 12
                            content: text
                            font: Font.font("Arial", 9)
                            fill: Color.web("#4d4d4d")
                        },
                        left = FXDNode {
                            layoutX: 0
                            layoutY: 0
                            url: "{__ROOT__}images/lc_button_tab.fxz"
                        },
                        middle = FXDNode {
                            layoutY:0
                            layoutX:left.layoutBounds.width
                            url: "{__ROOT__}images/middle_button_tab.fxz"
                            scaleX: label.layoutBounds.width + 10
                            translateX: (label.layoutBounds.width + 10) / 2 - .5
                        },
                        right = FXDNode {
                            layoutY:0
                            layoutX:middle.boundsInParent.width + left.layoutBounds.width 
                            url: "{__ROOT__}images/rc_button_tab.fxz"
                        }
                    ]
                }
                
    var contentActive = Group {
                content: [
                    label = Text {
                        layoutX: bind middle.boundsInParent.minX + 5
                        layoutY: 12
                        content: text
                        font: Font.font("Arial", 9)
                        fill: Color.web("#4d4d4d")
                    },
                    left = FXDNode {
                        layoutX: 0
                        layoutY: 0
                        url: "{__ROOT__}images/lc_button_active_tab.fxz"
                    },
                    middle = FXDNode {
                        layoutY:0
                        layoutX:left.layoutBounds.width
                        url: "{__ROOT__}images/middle_button_active_tab.fxz"
                        scaleX: label.layoutBounds.width + 10
                        translateX: (label.layoutBounds.width + 10) / 2 - .5
                    },
                    right = FXDNode {
                        layoutY:0
                        layoutX:middle.boundsInParent.width + left.layoutBounds.width 
                        url: "{__ROOT__}images/rc_button_active_tab.fxz"
                    }
                ]
            }
            
    override public function create():Node {
        Group {
            content: bind if (selected) contentActive else contentNormal
        }
    }
    
    override public var onMousePressed = function(e) {
        //action();
        selected = true;
    }
    
    override public var onMouseReleased = function(e) {
        if( hover ) {
            action();
            selected = true;
        } else {
            selected = false;
        }
      }
    
}
