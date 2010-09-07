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

import javax.swing.table.TableModel;

import javafx.scene.control.Tooltip;

public class SummaryTable extends Grid {
	public-init var title:String;
	public-init var table:TableModel;
	
	override var vgap = 8;
	override var hgap = 8;
	override var layoutInfo = LayoutInfo {
		hgrow: Priority.ALWAYS vgrow: Priority.NEVER
		hfill: false vfill: false
		//minWidth: 40 * header.size()	 
	}
	
	init {
		def colCount = table.getColumnCount();
		def rowCount = table.getRowCount();

   	rows = [
			GridRow {
				hgrow: Priority.ALWAYS vgrow: Priority.NEVER
				hfill: true vfill: false 
				cells: [
					Label {
						layoutInfo: GridLayoutInfo {
							hspan: colCount
							height: 20
							hfill: true
						}
						text: title.toUpperCase()
						textFill: Color.web("#4d4d4d")
						font: Font { name:"Arial" size: 11 }
						vpos: VPos.CENTER 
					}
				]
			}, GridRow {
				hgrow: Priority.ALWAYS vgrow: Priority.NEVER
				hfill: true vfill: false
				cells: for( i in [0..<colCount] ) Label {
					layoutInfo: GridLayoutInfo {
						hgrow: Priority.ALWAYS vgrow: Priority.ALWAYS
						hfill: true vfill: true
						height: 15
						minWidth: ((table.getColumnName(i).toString()).length() + 1) * 7
					}
					tooltip: Tooltip { text: (table.getColumnName(i) as String).toUpperCase() }
					text: (table.getColumnName(i).toString()).toUpperCase()
					textFill: Color.web("#000000")
					font: Font.font("Arial", FontWeight.BOLD, 10)
					vpos: VPos.CENTER 
					hpos: if(i == 0) HPos.LEFT else HPos.RIGHT
				}
			}, GridRow {
				hgrow: Priority.ALWAYS vgrow: Priority.NEVER
				hfill: true vfill: false 
				cells: [
					Rectangle {
						layoutInfo: GridLayoutInfo {hspan: colCount}
						fill: Color.rgb(0, 0, 0, 0.2)
						height: 1
						width: bind boundsInLocal.width
					}
				]
			}, for( i in [0..<rowCount] ) {
				GridRow {
					hgrow: Priority.ALWAYS vgrow: Priority.NEVER
					hfill: true vfill: false
					cells: for( j in [0..<table.getColumnCount()] ) {
						Label {
							layoutInfo: LayoutInfo {
								hgrow: Priority.ALWAYS vgrow: Priority.ALWAYS
								hfill: true vfill: true
								minWidth: if ( j == 0 ) 130 else ((table.getValueAt(i, j).toString()).length() +1)* 5//if ( table.getColumnCount() > 7) 60 else 100 
							}
							tooltip: Tooltip { text:table.getValueAt(i, j).toString() }
							text: { if( table.getValueAt(i, j).toString().length() == 0  )
										"N/A"
									else
										table.getValueAt(i, j).toString() 
							}
							textFill: Color.web("#000000")
							font: if(j == 0) Font.font("Arial", FontWeight.BOLD, 10) else Font { name:"Arial" size: 10 }
							vpos: VPos.BOTTOM
							hpos: if(j == 0) HPos.LEFT else HPos.RIGHT
						}
					}
				}
			}
		];
	}
}