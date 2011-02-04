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
import javafx.scene.text.TextAlignment;

public class SummaryHeader extends CustomNode {

	public var width: Number;
	
	var hbox: HBox;
	
	var group: Group;
	
	public var values: KeyValue[] on replace {
		delete hbox.content;
		for(i in [0..values.size()-1]){
			addValue(values.get(i).key, values.get(i).value, false, getAlignment(i));
		} 
	}
	
	override function create(): Node {
		group = Group{
			content: [
				Rectangle {
					x: 0
					y: 12
					fill: bind Color.web("#D9D39F")
					width: bind width
					height: 80 - 24
					arcWidth: 14
					arcHeight: 14
					stroke: Color.TRANSPARENT
					strokeWidth: 0.0
				}
				hbox = HBox {
					padding: Insets { top: 0 right: 18 bottom: 0 left: 18}
					layoutInfo: LayoutInfo {
						hgrow: Priority.NEVER vgrow: Priority.NEVER
						hfill: false vfill: false
						height: 80
					}
					spacing: 40
					content: []
				}
			]
		}
	}
	
	function addValue(key: String, value: String, bold: Boolean, hpos: HPos): Void {
		insert 
			Grid {
				vgap: 4
				rows: [
				    GridRow{
				    	cells: [
					    	Label {
								layoutInfo: LayoutInfo {
									hgrow: Priority.SOMETIMES vgrow: Priority.ALWAYS
									hfill: true vfill: true	
									minWidth: 50 
							    }
								text: bind key.toUpperCase()
								textFill: Color.web("#000000")
								font: if(bold) Font.font("Arial", FontWeight.BOLD, 10) else Font { name:"Arial" size: 10 }
								vpos: VPos.BOTTOM
								hpos: hpos
							}
						]
					},
				    GridRow{
				    	cells: [
					    	Label {
								layoutInfo: LayoutInfo {
									hgrow: Priority.SOMETIMES vgrow: Priority.ALWAYS
									hfill: true vfill: true	
									minWidth: 50 
							    }
								text: bind value
								textFill: Color.web("#000000")
								font: if(bold) Font.font("Arial", FontWeight.BOLD, 10) else Font { name:"Arial" size: 10 } 
								vpos: VPos.TOP
								hpos: hpos
							}
						]
					}
			    ]
			}
		into hbox.content;
	}
	
	function getAlignementByType(value: String): HPos {
		if(value.matches("[0-9]* *\\,*[0-9]*\\.*[0-9]*")){
			return HPos.RIGHT;
		}
		else{
			return HPos.LEFT;
		}		
	}

	function getAlignment(index: Number): HPos {
		if(index > 0){
			return HPos.RIGHT;
		}
		else{
			return HPos.LEFT;
		}		
	}
	
	public function get(): Group {
		group;
	}
}
