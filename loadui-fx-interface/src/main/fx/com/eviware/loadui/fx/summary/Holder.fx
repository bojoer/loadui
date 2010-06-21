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
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class Holder extends CustomNode {

	public var fillColor: Color = Color.RED;
	
	public var strokeColor: Color = Color.GREEN;
	
	public var node: Node;
	
	var group: Group;
	
	override function create(): Node {
		group = Group{
			layoutInfo: node.layoutInfo
			content: [
				Rectangle {
					x: bind node.layoutX
					y: bind node.layoutY
					fill: bind fillColor
					width: bind node.layoutBounds.width
					height: bind node.layoutBounds.height
					stroke: strokeColor
					strokeWidth: 1.0
				}			
				node
			]
		}
	}
	
	public function get(): Group {
		group;
	}
}
