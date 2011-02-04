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
package com.eviware.loadui.fx.widgets.componet;

import java.awt.Dimension;

import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.layout.WidgetCreationException;
import com.eviware.loadui.api.layout.WidgetFactory;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.counter.*;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Panel;
import javafx.scene.layout.Stack;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.control.TextBox;
import javafx.scene.CustomNode;
import javafx.scene.Node;
import javafx.fxd.FXDNode;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import com.eviware.loadui.api.property.Property;

import com.eviware.loadui.util.collections.ObservableList;
import java.util.Observer;
import java.util.Observable;
import java.util.ArrayList;

//import com.eviware.loadui.fx.FxUtils;

/**
* @author robert
*/


public class CounterWidget extends Panel, Observer {
    
    public var component:ObservableList;
    public var work:Property;
    public var ccounters:ArrayList;
    
    var counters:Node[] = [];
    var resetCounters = [0,0,0,0,0,0,0,0,0,0];
    override var content = bind counters;
    var numVisible = 1 on replace {
        onLayout();
    };
    
    init {
        component.addObserver(this);
        counters = for( cnt in [0..9] ) {[
            Stack {
                content: [
                FXDNode {
                    url: "{com.eviware.loadui.fx.FxUtils.__ROOT__}images/splitter_small_screen.fxz"
                },
                Text {
                    content: cnt.toString()
                    font: Font.font("Arial", 12)
                    fill: Color.web("#00FF00")
                }
                ]
                visible: cnt == 0 // just first visible
            },
            javafx.scene.shape.Line {
                startX: 20  startY: 0
                endX: 20  endY: 24
                stroke:Color.web("#8c8c8c")
                visible: false
            } ]
        }
        delete counters[sizeof counters -1];
        
		insert Rectangle {
			width: bind width
			height: bind height
        	managed: false
        	fill: Color.TRANSPARENT
		} before counters[0];
		
		update(null, null);
    }
    
    override function update(observable: Observable, arg: Object) {
        FX.deferAction(
        function(): Void {
            if ( work.getValue() as Boolean ) {
                var newVisible = 0;
                for( cnt in [0..20][p| counters[p as Integer] instanceof Stack] ) {
                        var field:Stack = counters[cnt as Integer] as Stack;
                        if ( (ccounters.get(cnt/2) as Counter).get() < resetCounters[cnt/2] )
                        	resetCounters[cnt/2] = 0;
                        var valueToShow =  (ccounters.get(cnt/2) as Counter).get() - resetCounters[cnt/2] ;
                        (field.content[1] as Text).content = (valueToShow  mod 1000).toString();
                        if ( (component.get(cnt/2) as Integer) > -1 ) {
                            field.visible = true;
                            if( cnt > 0 )
                                counters[(cnt-1) as Integer].visible = true;
                            newVisible ++;
                            } else {
                            resetCounters[cnt/2] = (ccounters.get(cnt/2) as Counter).get();
                            field.visible = false;
                            if( cnt > 0 )
                            counters[(cnt-1) as Integer].visible = false;
                        }
                }
                if( numVisible != newVisible )
                numVisible = newVisible;
            }
        });
    }
    
    override var prefWidth = function(height:Number):Number {
        450
    }
    
    override var prefHeight = function(width:Number):Number {
        30
    }
    
    override var minWidth = function():Float {
        450
    }
    
    override var maxWidth = function():Float {
        450
    }
    
    override var onLayout = function():Void {
        resizeContent(); // will set all content to preferred sizes
        // space to divide = total size - cnt.size * nVisible - nVisible*separator.size
        var xStep:Integer = ((450 - 36.5*numVisible - numVisible + 1) / (2*numVisible)) as Integer;
        translateX = xStep;
        var next:Integer = xStep;
        for (node in getManaged(content)[p| p.visible == true ]) {
            if ( node instanceof Stack ) {
                //       println(" counter ");
                //       println("next {next} -- xStep {xStep} --- {node.boundsInParent.width}");
                positionNode(node, next, 0);
                next = (next + xStep + node.boundsInParent.width) as Integer;
            }
            else {
                //       println(" separator " );
                //		println("next {next} -- xStep {xStep}");
                positionNode(node, next, 0);
                next = next + 1 + xStep;
            }
        }
    }
}
