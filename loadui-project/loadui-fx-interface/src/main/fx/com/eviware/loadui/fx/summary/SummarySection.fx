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
import javafx.geometry.HPos;
import javafx.geometry.VPos;
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

public class SummarySection extends CustomNode {

	public var title: String;
	
	var grid: Grid;
	
	var vbox: VBox;
	
	public var values: KeyValue[] on replace {
		delete grid.rows;
		
		addHeader();
		
		for(v in values){
			addValue(v.key, v.value);
		}
	}
	
	override function create(): Node {
		grid = Grid {
			layoutInfo: LayoutInfo {
				hgrow: Priority.ALWAYS vgrow: Priority.NEVER
				hfill: true vfill: false
			}
			vgap: 8
			rows: []
		}
		
		vbox = VBox{
			layoutInfo: LayoutInfo {
				hgrow: Priority.ALWAYS vgrow: Priority.NEVER
				hfill: true vfill: false
			}
			spacing: 0
			content: [
				grid
			]
		}
	}
	
	public function addHeader(): Void {
		insert GridRow{
			hfill: true 
			cells: [
				Label {
					layoutInfo: GridLayoutInfo {hspan: 2}
					text: bind title
					textFill: Color.web("#4d4d4d")
					font: Font.font("Amble", FontWeight.BOLD, 16)
					vpos: VPos.CENTER 
				}
			]
		}
		into grid.rows;
		if(values.size() > 0){
			//insert GridRow{
			//	hfill: true 
			//	cells: [
			//		Label {
			//			layoutInfo: GridLayoutInfo {hspan: 2 height: 20 width: 300}
			//			text: bind title
			//			textFill: Color.web("#4d4d4d")
			//			font: Font { name:"Arial" size: 11 }
			//			vpos: VPos.CENTER 
			//		}
			//	]
			//}
			//into grid.rows;
			insert GridRow{ 
				hfill: true
				cells: [
					Line {
						layoutInfo: GridLayoutInfo {hspan: 2}
						stroke: Color.rgb(0, 0, 0, 0.2)
						strokeWidth: 1.0
						endX: bind grid.width
					}
				]
			}
			into grid.rows;
		}
	}
	
	public function addValue(key: String, value: String): Void {
		insert 
			GridRow{ 
				cells: [
					Label {
						layoutInfo: LayoutInfo {
							hgrow: Priority.NEVER vgrow: Priority.ALWAYS
							hfill: false vfill: true
							width: 270
					    }
						text: bind key
						textFill: Color.web("#000000")
						font: Font { size: 10 }
						vpos: VPos.CENTER 
					}
					Label {
						layoutInfo: LayoutInfo {
							hgrow: Priority.NEVER vgrow: Priority.ALWAYS
							hfill: false vfill: true	 
							hpos: HPos.RIGHT
					    }
						text: bind value
						textFill: Color.web("#000000")
						font: Font { size: 10 }
						vpos: VPos.CENTER 
						hpos: HPos.RIGHT 
					}
				]
			}
		into grid.rows;
	}
	
	public function get(): VBox {
		vbox;
	}
}
