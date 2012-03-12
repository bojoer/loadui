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
package com.eviware.loadui.fx.ui.dnd;

import javafx.scene.Node;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.Container;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.geometry.Insets;
import javafx.util.Sequences;

import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.Deletable;

/**
 * Acts as a HBox or VBox, but allows the user to reorder the items using drag and drop.
 *
 * @author dain.nilsson
 */
public class SortableBox extends BaseNode, Resizable {
	var box:Container;
	
	public-init var vertical = false on replace {
		box = if( vertical ) VBox {
			width: bind width
			height: bind height
			layoutInfo: bind layoutInfo
			fillWidth: fillWidth
			padding: padding
			spacing: spacing
			nodeHPos: nodeHPos
			nodeVPos: nodeVPos
			hpos: hpos
			vpos: vpos
		} else HBox {
			width: bind width
			height: bind height
			layoutInfo: bind layoutInfo
			fillHeight: fillHeight
			padding: padding
			spacing: spacing
			nodeHPos: nodeHPos
			nodeVPos: nodeVPos
			hpos: hpos
			vpos: vpos
		};
		box.content = buildContent();
	}
	
	public var padding:Insets on replace {
		if( vertical ) {
			(box as VBox).padding = padding;
		} else {
			(box as HBox).padding = padding;
		}
	}
	
	public var spacing = 0 on replace {
		if( vertical ) {
			(box as VBox).spacing = spacing;
		} else {
			(box as HBox).spacing = spacing;
		}
	}
	
	public var nodeHPos = HPos.LEFT on replace {
		if( vertical ) {
			(box as VBox).nodeHPos = nodeHPos;
		} else {
			(box as HBox).nodeHPos = nodeHPos;
		}
	}
	
	public var nodeVPos = VPos.TOP on replace {
		if( vertical ) {
			(box as VBox).nodeVPos = nodeVPos;
		} else {
			(box as HBox).nodeVPos = nodeVPos;
		}
	}
	
	public var hpos = HPos.LEFT on replace {
		if( vertical ) {
			(box as VBox).hpos = hpos;
		} else {
			(box as HBox).hpos = hpos;
		}
	}
	
	public var vpos = VPos.TOP on replace {
		if( vertical ) {
			(box as VBox).vpos = vpos;
		} else {
			(box as HBox).vpos = vpos;
		}
	}
	
	public var fillWidth = true on replace {
		if( vertical ) {
			(box as VBox).fillWidth = fillWidth;
		}
	}
	
	public var fillHeight = true on replace {
		if( not vertical ) {
			(box as HBox).fillHeight = fillHeight;
		}
	}
	
	public var content:Node[] on replace {
		box.content = buildContent();
	}
	
	public var onMoved: function( node:Node, fromIndex:Integer, toIndex:Integer ):Void;
	
	public var enforceBounds:Boolean = true;
	
	function buildContent():Node[] {
		for( child in content ) {
			var draggable:ElementNode;
			var offset = 0.0;
			
			def frame:DraggableFrame = DraggableFrame {
				layoutInfo: child.layoutInfo
				draggable: draggable = if( child instanceof Deletable ) DeletableElementNode {
					confirmDialogScene: bind scene
					layoutInfo: child.layoutInfo
					width: bind frame.width
					height: bind frame.height
					revert: false
					contentNode: child
					containment: bind if (enforceBounds) localToScene( layoutBounds ) else null
					onDragging: function():Void {
						def index = Sequences.indexByIdentity( box.content, frame );
						var pos:Number;
						var nextPos:Number;
						var prevPos:Number;
						if( vertical ) {
							pos = draggable.translateY;
							prevPos = if( index > 0 ) -(spacing + box.content[index-1].layoutBounds.height / 2) else Integer.MIN_VALUE;
							nextPos = if( index < sizeof content-1 ) spacing + box.content[index+1].layoutBounds.height / 2 else Integer.MAX_VALUE;
						} else {
							pos = draggable.translateX;
							prevPos = if( index > 0 ) -(spacing + box.content[index-1].layoutBounds.width / 2) else Integer.MIN_VALUE;
							nextPos = if( index < sizeof content-1 ) spacing + box.content[index+1].layoutBounds.width / 2 else Integer.MAX_VALUE;
						}
						
						if( pos + offset < prevPos or pos + offset > nextPos ) {
							def moveIndex = if( pos + offset < prevPos ) index-1 else index+1;
							def delta = if( vertical ) {
								if( moveIndex > index ) {
									box.content[moveIndex].layoutY - frame.height + box.content[moveIndex].layoutBounds.height - frame.layoutY;
								} else {
									box.content[moveIndex].layoutY - frame.layoutY;
								}
							} else {
								if( moveIndex > index ) {
									box.content[moveIndex].layoutX - frame.width + box.content[moveIndex].layoutBounds.width - frame.layoutX;
								} else {
									box.content[moveIndex].layoutX - frame.layoutX;
								}
							}
							
							delete box.content[index];
							insert frame before box.content[moveIndex];
							offset -= delta;
						}
					}
					onRelease: function():Void {
						def index = Sequences.indexByIdentity( box.content, frame );
						def oldIndex = Sequences.indexByIdentity( content, child );
						if( oldIndex != index ) {
							var newContent = content;
							delete child from newContent;
							insert child before newContent[index];
							content = newContent;
							offset = 0.0;
							onMoved( child, oldIndex, index );
						}
					}
				} else ElementNode {
					layoutInfo: child.layoutInfo
					width: bind frame.width
					height: bind frame.height
					revert: false
					contentNode: child
					containment: bind if (enforceBounds) localToScene( layoutBounds ) else null
					onDragging: function():Void {
						def index = Sequences.indexByIdentity( box.content, frame );
						var pos:Number;
						var nextPos:Number;
						var prevPos:Number;
						if( vertical ) {
							pos = draggable.translateY;
							prevPos = if( index > 0 ) -(spacing + box.content[index-1].layoutBounds.height / 2) else Integer.MIN_VALUE;
							nextPos = if( index < sizeof content-1 ) spacing + box.content[index+1].layoutBounds.height / 2 else Integer.MAX_VALUE;
						} else {
							pos = draggable.translateX;
							prevPos = if( index > 0 ) -(spacing + box.content[index-1].layoutBounds.width / 2) else Integer.MIN_VALUE;
							nextPos = if( index < sizeof content-1 ) spacing + box.content[index+1].layoutBounds.width / 2 else Integer.MAX_VALUE;
						}
						
						if( pos + offset < prevPos or pos + offset > nextPos ) {
							def moveIndex = if( pos + offset < prevPos ) index-1 else index+1;
							def delta = if( vertical ) {
								if( moveIndex > index ) {
									box.content[moveIndex].layoutY - frame.height + box.content[moveIndex].layoutBounds.height - frame.layoutY;
								} else {
									box.content[moveIndex].layoutY - frame.layoutY;
								}
							} else {
								if( moveIndex > index ) {
									box.content[moveIndex].layoutX - frame.width + box.content[moveIndex].layoutBounds.width - frame.layoutX;
								} else {
									box.content[moveIndex].layoutX - frame.layoutX;
								}
							}
							
							delete box.content[index];
							insert frame before box.content[moveIndex];
							offset -= delta;
						}
					}
					onRelease: function():Void {
						def index = Sequences.indexByIdentity( box.content, frame );
						def oldIndex = Sequences.indexByIdentity( content, child );
						if( oldIndex != index ) {
							var newContent = content;
							delete child from newContent;
							insert child before newContent[index];
							content = newContent;
							offset = 0.0;
							onMoved( child, oldIndex, index );
						}
					}
				}
			}
		}
	}
	
	
	override function create():Node {
		box
	}
	
	override function getPrefHeight( width:Number ):Number {
		box.getPrefHeight( width )
	}
	
	override function getPrefWidth( height:Number ):Number {
		box.getPrefWidth( height )
	}
}

class ElementNode extends BaseNode, Draggable, Resizable {
	override var width on replace { (contentNode as Resizable).width = width }
	override var height on replace { (contentNode as Resizable).height = height }
	
	override function getPrefHeight( width:Number ):Number {
		(contentNode as Resizable).getPrefHeight( width )
	}
	
	override function getPrefWidth( height:Number ):Number {
		(contentNode as Resizable).getPrefWidth( height )
	}
	
	override function doLayout():Void {
	}
}

class DeletableElementNode extends ElementNode, Deletable {
	override function doDelete():Void {
		(contentNode as Deletable).doDelete();
	}
}