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
package com.eviware.loadui.fx.widgets.componet;

import java.awt.Dimension;

import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.layout.WidgetCreationException;
import com.eviware.loadui.api.layout.WidgetFactory;
import com.eviware.loadui.api.property.Property;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import com.eviware.loadui.fx.FxUtils.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.util.Sequences;
import javafx.geometry.BoundingBox;

import java.util.ArrayList;
import com.eviware.loadui.fx.ui.layout.widgets.SelectorWidgetSkin;
import javafx.scene.text.Font;


/**
 * @author robert
 */

public class SelectorWidget extends VBox {
    
    var labels:ArrayList = new ArrayList();
    var images:ArrayList = new ArrayList();
    
    var selected:String = "none";
    var default:String;
    def rbtng:ToggleGroup = ToggleGroup{}
    public-init var component:LayoutComponent;
    var showLabels:Boolean = true;
    var label = "Choose one:";
    var topLabel:Label;
    
    override var layoutBounds = bind lazy BoundingBox {
        	minX: topLabel.layoutBounds.minX
        	minY: topLabel.layoutBounds.minY
    		width: width
    		height: height
    	}
    
    init {
         if ( component.has("label") ) {
             label = (component.get("label") as String);
        }
        
        if ( component.has("labels") ) {
            labels = (component.get("labels") as ArrayList);
        }
        
        if ( component.has("images") ) {
            images = (component.get("images") as ArrayList);
        }
        
        if ( component.has("showLabels") ) {
            showLabels = component.get("showLabels") as Boolean;
        }
        
        if ( component.has("selected") ) {
            selected = (component.get("selected") as Property).getValue() as String;
        }
        
        if ( component.has("default") ) {
            default = component.get("default") as String;
        }
        	
          spacing = 4;
          content = [ topLabel = Label {
                        text: label
                  	}, 
                  	for( label in labels ) {
                      	RadioButton {
                           text: label as String
                           
                           graphic: ImageView {
                               image:  Image {
                               		url: if (indexof label < images.size()) "{__ROOT__}images/options/{images.get(indexof label)}" else null
                               }
                           }
                           selected: default == (label as String)
                           toggleGroup: rbtng
                           onMouseClicked: function(e) { update() }
                          skin:SelectorWidgetSkin{
                               showLabels:showLabels
                           }
                      }
                  	},
                      Button {
                          width: bind 60
                          height: bind 13
                          action: function() {
                              if (Sequences.indexOf(rbtng.toggles, rbtng.selectedToggle) == rbtng.toggles.size() - 1) {
                                  rbtng.selectedToggle = rbtng.toggles[0];
                              } else {
                                  rbtng.selectedToggle = rbtng.toggles[Sequences.indexOf(rbtng.toggles, rbtng.selectedToggle)+ 1];
                              }
                          }
                      }
                  ] 
    }
    
    function update() {
        if ( component != null ) {
            (component.get("selected") as Property).setValue((rbtng.selectedToggle as RadioButton).text);
        }
    }
    
}
