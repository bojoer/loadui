/*
*Note.fx
*
*Created on aug 23, 2010, 10:30:16 fm
*/

package com.eviware.loadui.fx.widgets.canvas;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Stack;
import javafx.scene.layout.LayoutInfo;
import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.TextBox;
import javafx.scene.Node;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.util.Math;

import com.javafx.preview.control.MenuButton;
import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.Deletable;
import com.eviware.loadui.fx.ui.dnd.Movable;

import com.eviware.loadui.api.model.AttributeHolder;

import java.lang.StringBuilder;

public function loadNotes( canvas:Canvas ):Note[] {
	var notes:Note[] = [];
	def noteIds = canvas.canvasItem.getAttributes()[s|s.startsWith( "gui.note." )];
	for( id in noteIds ) {
		def noteData = canvas.canvasItem.getAttribute( id, null );
		if( noteData != null ) {
			def parts = noteData.split( ";", 5 );
			insert Note {
				id: id
				canvas: canvas
				noteHolder: canvas.canvasItem
				layoutX: Integer.parseInt( parts[0] )
				layoutY: Integer.parseInt( parts[1] )
				textWidth: Integer.parseInt( parts[2] )
				textHeight: Integer.parseInt( parts[3] )
				text: parts[4]
			} into notes;
		}
	}
	
	return notes;
}

public function createNote( canvas:Canvas, x:Integer, y:Integer ):Note {
	var index = 0;
	while( canvas.canvasItem.getAttribute( "gui.note.{index}", null ) != null ) {
		index++;
	}
	
	Note { id: "gui.note.{index}", canvas:canvas, noteHolder: canvas.canvasItem, layoutX: x, layoutY: y }
}

public class Note extends BaseNode, Movable, Selectable, Deletable {
	override var styleClass = "note";
	override var blocksMouse = true;
	
	public var textWidth:Integer = 200;
	public var textHeight:Integer = 150;
	
	public var text:String on replace {
		textBox.text = text;
	}
	
	public var canvas:Canvas;
	public var noteHolder:AttributeHolder;
	
	var startWidth:Integer;
	var startHeight:Integer;
	
	def baseRegion = Region {
		styleClass: "note-background"
		layoutInfo: LayoutInfo {
			hfill: true
			vfill: true
		}
	}
	
	def cornerRegion = Region {
		styleClass: "note-corner"
		layoutInfo: LayoutInfo {
			hpos: HPos.RIGHT
			vpos: VPos.BOTTOM
			width: 11
			height: 11
			vfill: false
			hfill: false
			margin: Insets { right: 3, bottom: 3 }
		}
	}
	
	def cornerResize = Rectangle {
		fill: Color.TRANSPARENT
		width: 15
		height: 15
		layoutInfo: LayoutInfo {
			hpos: HPos.RIGHT
			vpos: VPos.BOTTOM
		}
		blocksMouse: true
		cursor: Cursor.SE_RESIZE
		onMousePressed: function( e:MouseEvent ) {
			startWidth = textWidth;
			startHeight = textHeight;
		}
		onMouseDragged: function( e:MouseEvent ) {
			textWidth = Math.max( 100, startWidth + e.dragX as Integer );
			textHeight = Math.max( 75, startHeight + e.dragY as Integer );
		}
		onMouseReleased: function( e:MouseEvent ) {
			save();
		}
	}
	
	def textBox:TextBox = TextBox {
		multiline: true
		layoutInfo: LayoutInfo { width: bind textWidth, height: bind textHeight }
		text: text
	}
	
	def textBoxText = bind textBox.text on replace {
		text = textBoxText;
		save();
	}
	
	def body = VBox {
		padding: Insets { left: 11, right: 11, bottom: 11 }
		content: [
			Label { text: "NOTE", layoutInfo: LayoutInfo { height: 30, hfill: true } },
			HBox {
				content: [ MenuButton { text: "Menu" } ]
			}, textBox
		]
	}
	
	public function save():Void {
		noteHolder.setAttribute( id, "{layoutX as Integer};{layoutY as Integer};{textWidth};{textHeight};{text}" );
	}
	
	override function create():Node {
		Stack {
			content: [ baseRegion, cornerRegion, body, cornerResize ]
		}
	}
	
	override var onGrab = function():Void {
		toFront();
		requestFocus();
		if( mouseEvent.controlDown ) { if( selected ) deselect() else select() } else if( not selected ) selectOnly();
	}
	
	override var onMove = function():Void {
		save();
	}
	
	override function doDelete():Void {
		canvas.removeNote( this );
		noteHolder.removeAttribute( id );
	}
}