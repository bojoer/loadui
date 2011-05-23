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
package com.eviware.loadui.fx.summary;

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.scene.layout.Container;
import javafx.scene.layout.Stack;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.geometry.BoundingBox;
import javafx.geometry.Insets;
import javafx.scene.effect.DropShadow;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Label;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.FontWeight;
import com.javafx.preview.layout.Grid;
import com.javafx.preview.layout.GridRow;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.dnd.MovableNode;
import com.eviware.loadui.fx.ui.button.GlowButton;
import com.eviware.loadui.fx.ui.resources.TitlebarPanel;
import com.eviware.loadui.fx.ui.resources.Paints;

import org.jfxtras.scene.layout.XMigLayout;
import org.jfxtras.scene.layout.XMigLayout.*;
import net.miginfocom.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.shape.Line;
import com.javafx.preview.layout.GridLayoutInfo;
import javafx.scene.CustomNode;

public class SummaryButton extends CustomNode {

	public var text: String;
	
	public var selected: Boolean = false;
	
	public var action: function();
	
	public var group: SummaryButtonGroup on replace oldGroup {
    	if(oldGroup != null){
    		oldGroup.remove(this);
    	}
    	if(group != null){
    		group.add(this);
    	}
    }
	
	var width: Number = 130;
	
	var height: Number = 26;
	
	override function create(): Node {
		Group {
			content: [
				Rectangle{
					width: width
					height: height
					fill: bind if(selected) Color.web("#dcd6a7") else Color.web("#F9F2B7")
				}
				Line {
					stroke: bind if(selected) Color.rgb(102, 102, 102, 0.2) else Color.TRANSPARENT
					strokeWidth: 1.0
					endX: width
					startY: 1
					endY: 1
				}
				Line {
					stroke: bind if(selected) Color.rgb(0, 0, 0, 0.2) else Color.TRANSPARENT
					strokeWidth: 1.0
					endX: width
					startY: 2
					endY: 2
				}
				Line {
					stroke: Color.rgb(0, 0, 0, 0.2)
					strokeWidth: 1.0
					endX: width
					startY: bind if(selected) height - 2 else height - 1 
					endY: bind if(selected) height - 2 else height - 1
				}
				Line {
					stroke: bind if(selected) Color.rgb(255, 255, 255, 0.5) else Color.TRANSPARENT
					strokeWidth: 1.0
					endX: width
					startY: height - 1
					endY: height - 1
				}
				Label {
					layoutX: 13
					layoutInfo: LayoutInfo{
						width: width
						height: height	
					}
					text: bind text
					textFill: Color.web("#000000")
					font: bind if(selected) Font.font( "Amble", FontWeight.BOLD, 10) else Font { size: 10 }
					vpos: VPos.CENTER 
					hpos: HPos.LEFT
				}
			]
		}
	}

    var oldSelected: Boolean = selected;
    
    override public var onMousePressed = function(e) {
    	oldSelected = selected;
        selected = true;
    }
    
    override public var onMouseReleased = function(e) {
        if( hover ) {
        	requestFocus();
            action();
            if(group != null){
    			group.unselect();
    		}
            selected = true;
        } 
        else {
            selected = oldSelected;
        }
    }
}
