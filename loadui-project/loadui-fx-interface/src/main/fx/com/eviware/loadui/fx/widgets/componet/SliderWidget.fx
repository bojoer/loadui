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
package com.eviware.loadui.fx.widgets.componet;
/*
*SliderWidget.fx
*
*Created on Apr 21, 2010, 13:43:14 PM
*/
import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.fx.ui.layout.Widget;
import javafx.scene.input.MouseEvent;
import javafx.scene.CustomNode;
import javafx.scene.Node;
import com.eviware.loadui.fx.ui.popup.TooltipHolder;
import javafx.scene.layout.Resizable;
import com.eviware.loadui.fx.ui.dnd.SliderNode;
import com.eviware.loadui.fx.ui.node.BaseNode;
import javafx.scene.input.MouseEvent;
import com.eviware.loadui.fx.FxUtils;
import javafx.fxd.FXDNode;
import javafx.scene.Group;
import javafx.scene.layout.VBox;
import com.eviware.loadui.fx.ui.dnd.Movable;
import javafx.geometry.BoundingBox;
import javafx.util.Math;
import javafx.scene.Cursor;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SliderWidget extends Widget, BaseNode, Resizable, TooltipHolder{

    public var numOptions:Integer = 10;
    
    var boldText:Integer;
    
    def slider = FXDNode {
        url:"{com.eviware.loadui.fx.FxUtils.__ROOT__}images/slider_back.fxz"
    }
    
    public var selectedIndex:Integer = ((plc.getProperty().getValue() as Integer) -1) on replace {
        sliderHandle.layoutX = notchSize*selectedIndex;
    }
    
    def notchSize = bind if(numOptions > 1) (slider.layoutBounds.width - sliderHandle.layoutBounds.width)/(numOptions - 1 ) else 1 on replace {
        sliderHandle.layoutX = notchSize*selectedIndex;
    }
    
    override var blocksMouse = true;
    
    init {
        addMouseHandler( MOUSE_PRIMARY_CLICKED, function( e:MouseEvent ):Void {
            def pos = if( e.x < 0 ) 0 else if( e.x >= slider.layoutBounds.width ) slider.layoutBounds.width -1 else e.x;
            selectedIndex = pos/(slider.layoutBounds.width/numOptions) as Integer;
        } );
    }
    
    override var value on replace {
        //state = if( value instanceof Integer ) value as Integer else 1;
        plc.getProperty().setValue( value );
        sliderHandle.layoutX = notchSize*(( value as Integer ) - 1 );
        boldText = (value as Integer) - 1;
    }
    
    var state = bind (selectedIndex + 1) on replace {
        value = state;
        boldText = selectedIndex;
    }
    
    def sliderHandle = SliderNodeHandle {
        contentNode: FXDNode {
            url: "{com.eviware.loadui.fx.FxUtils.__ROOT__}images/slider.fxz"
        }
        cursor: if(cursor != null) cursor else Cursor.DEFAULT
    }
    
    var digit:Text;
    
    def textUnder = Group {
        layoutY: slider.layoutBounds.maxY + 10
        content: for ( i in [0..9])
        digit = Text {
            layoutX: if ( i == 0 or i == 9 ) i*notchSize + notchSize/2 - digit.layoutBounds.width/2 -8 else i*notchSize + notchSize/2 - digit.layoutBounds.width/2 - 5
            content: (i + 1).toString()
            font: bind if (i == boldText) Font.font("Amble", FontWeight.ULTRA_BOLD, 11) else Font.font("Amble", 10);
        }
        
        
    }
    
    override public function create(){
        VBox {
            content: [
            Text {
                content: "Number Of Outputs"
                font: Font.font("Amble", 12)
            },
            Group {
                content: [
                slider,
                sliderHandle,
                textUnder
                ]
            }
            ]
        }
    }
    
    override function getPrefWidth( height:Float ) {
        250
    }
    
    override function getPrefHeight( width:Float ) {
        40
    }
}

public class SliderNodeHandle extends BaseNode, Movable {
    override var onGrab = function() {
        containment = localToScene( BoundingBox {
            minX: layoutBounds.minX - layoutX
            minY: layoutBounds.minY - layoutY
            height: layoutBounds.height
            width: slider.layoutBounds.width
        } );
    }
    
    override var onMove = function() {
        selectedIndex = Math.round(layoutX/notchSize);
        layoutX = notchSize* selectedIndex;
    }
}
