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
 /*
*Dialog.fx
*
*Created on feb 9, 2010, 15:47:15 em
*/ 

package com.eviware.loadui.fx.ui.dialogs;

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.scene.layout.Container;
import javafx.scene.layout.Stack;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.geometry.BoundingBox;
import javafx.scene.control.Button;
import javafx.fxd.FXDNode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

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
import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.tabs.*;
import com.eviware.loadui.fx.ui.form.Form;

import java.lang.IllegalArgumentException;

/**
 * A Group which is used as an overlay to place nodes which are being dragged into to avoid z - index issues.
 * The Group should be positioned at 0, 0 in the scene.
*/
public var overlay: Group = null;

/**
 * A Dialog is a panel which is displayed on top of other content in a movable window.
 * It can be modal, blocking all other clicks to the application.
 * 
 * @author dain.nilsson
*/
public class Dialog extends FocusChangeListener{
	 /**
	 * Whether to make the dialog modal or not.
	 */ 
	public var modal = true;
	
	 /**
	 * The contents to display in the dialog.
	 */ 
	public var content: Node[];
	
	 /**
	 * Allows manual control over the dialogs X coordinate.
	 */ 
	public var x:Number on replace {
		panel.layoutX = x;
	}
	
	 /**
	 * Allows manual control over the dialogs Y coordinate.
	 */ 
	public var y:Number on replace {
		panel.layoutY = y;
	}
	
	 /**
	 * Called when the dialog is closed.
	 */ 
	public var onClose: function():Void;
	
	 /**
	 * Set to false to prevent the dialog from automatically showing after being created.
	 */ 
	public-init var showPostInit = true;
	
	 /**
	 * True if the dialog should have an X in the corner to close it. 
	 */ 
	public var closable = true;
	
	 /**
	 * The title of the dialog
	 */ 
	public var title: String;
	
	 /**
	 * Defines help Url, if not set help button is not visible. 
	 */ 
	public var helpUrl: String;
	
	 /**
	 * Defines the Paint for the background.
	 */ 
	public var backgroundFill: Paint = Color.web("#EDEDED");
	
	public var stripeFill: Paint = Color.web("#B2B2B2", .2);
	public var stripeVisible:Boolean = false;
	
	public-init var okText:String = ##[OK]"OK";
	
	public-init var noOk = false;
	
	public var onOk:function():Void;
	
	public-init var cancelText:String = ##[CANCEL]"Cancel";
	
	public-init var noCancel = false;
	
	public var onCancel:function():Void = close;
	
	public-init var width: Integer = -1;
	
	public-init var height: Integer = -1;
	
	public-init var extraButtons:ActionButton[];
	
	var modalLayer:Node;
	var panel:Node;
	
	var okButton: ActionButton;
	var cancelButton: ActionButton;

	def target: XMigLayout = XMigLayout {
		layoutX: 20
		layoutY: 20
		layoutInfo: LayoutInfo { hfill: true, vfill: true }
		constraints: new LC().fill().wrapAfter(1).insets( "0" )
		rows: new AC().gap( "20px" ).noGrid()
		content: [
			VBox {
				layoutInfo: LayoutInfo { hfill: true, vfill: true } 	
				spacing: 10, 
				content: bind content
			}
		]
	}

	var buttons: Container;
	var titleText: Text;
	
	postinit {
		if ( sizeof buttons.content == 1 )
		buttons.layoutX = panel.layoutBounds.width - 45 
		else 
		buttons.layoutX = panel.layoutBounds.width - 87;
	}
	
	init {
		def scene = overlay.scene;
		
		def sceneBounds = BoundingBox {
			width: scene.width;
			height: scene.height;
		}
		
		modalLayer = Rectangle {
			width: bind scene.width
			height: bind scene.height
			fill: Color.BLACK
			opacity: 0.3
			visible: bind modal
			blocksMouse: bind modal
		}
		
		var dialogButtons: ActionButton[] = [];

		if( not noCancel ) {
			cancelButton = ActionButton {
				translateX: - 19
				translateY: bind if(stripeVisible == true) - 43 else - 38
				text: cancelText
				action: function() { onCancel() }
				width: 59
				layoutInfo: nodeConstraints(new CC().tag( "cancel" ))
				focusChangeListener: this
				prevFocus: if(not noOk) okButton else lastFocusable()
				nextFocus: firstFocusable()
			}
			insert cancelButton into dialogButtons;
		}	
		if( not noOk ) {
			okButton = ActionButton {
				translateX: - 19
				translateY: bind if(stripeVisible == true) - 43 else - 38
				text: okText
				width: 59
				action: function() { onOk() }
				layoutInfo: nodeConstraints(new CC().tag( "ok" ))
				focusChangeListener: this
				prevFocus: lastFocusable()
				nextFocus: if(not noCancel) cancelButton else firstFocusable()
			}
			insert okButton into dialogButtons;
		}
		
		insert extraButtons into dialogButtons;
		
		var titlebarPanel:TitlebarPanel;
		panel = MovableNode {
		    layoutX: x
		    layoutY: y
			useOverlay: false
			containment: sceneBounds
			
			contentNode: Group {
				content: [
					titlebarPanel = TitlebarPanel {
						backgroundFill: backgroundFill
						content: [
							XMigLayout {
								constraints: new LC().fill().wrapAfter(1).insets( "0" )
								rows: new AC().gap( "0px" ).noGrid()
								content: [
									Group {
										content: [
											Rectangle {
												x:0
												y:0
												fill: Color.TRANSPARENT
												width: bind if(width > - 1) width else target.boundsInLocal.width + 10
												height: bind if(height > - 1) height else target.boundsInLocal.height + 10
											}, 
											Rectangle {
												x:0
												y:0
												height: 40
												width: bind if(width > - 1) width else target.boundsInLocal.width + 10
												fill: bind stripeFill
												visible: stripeVisible
											}, 
											target
										]
									}
									dialogButtons
								]
							}							
						]
						titlebarContent: [
							titleText = Text {
								x: 20
								y: 20
								fill: Color.rgb(0, 0, 0, 0.5)
								content: bind title
								font: Font {
									size: 10
								}
							}, 
							buttons = HBox {
								 spacing: 9 
							}
						]
					}
				]
			}
			handle: titlebarPanel.titlebar
		}
		
		if( helpUrl != null ) {
			insert GlowButton {
				contentNode: FXDNode {
					translateY: 2
					url: "{__ROOT__}images/help_btn.fxz"
				}
				tooltip: "Help"
				action: function() { openURL( helpUrl ); }
			//	width: bind 28
				height: bind 30
			} into buttons.content;
			insert FXDNode {
				translateY: 2
				scaleY: .6
				url: "{__ROOT__}images/delimiter.fxz"
			} into buttons.content;
		}
		
		
		if( closable ) {
			insert GlowButton {
				contentNode: FXDNode {
					translateY: 2
					url: "{__ROOT__}images/close_btn.fxz"
				}
				tooltip: "Close"
				action: close
				//width: bind 28
				height: bind 30
			} into buttons.content;
		}
		
		if( not FX.isInitialized( x ) ) {
			x = ( scene.width - panel.boundsInLocal.width ) / 2;
		}
		if( not FX.isInitialized( y ) ) {
			y = ( scene.height - panel.boundsInLocal.height ) / 2;
		}
		
		if( showPostInit )
		show();
	}
	
	 /**
	 * Displays the Dialog.
	 */ 
	public function show() {
		insert modalLayer into overlay.content;
		insert panel into overlay.content;
		
		if( okButton != null )
		okButton.requestFocus();
	}
	
	 /**
	 * Closes the Dialog.
	 */ 
	public function close():Void {
		delete panel from overlay.content;
		delete modalLayer from overlay.content;
		onClose();
	}
	
	override function focusGained(node: Node){
		if(node == okButton){
			okButton.prevFocus = lastFocusable();
			if(not noCancel){
				okButton.nextFocus = cancelButton;
			}
			else{
				okButton.nextFocus = firstFocusable();
			}
		}
		else if(node == cancelButton){
			cancelButton.nextFocus = firstFocusable();
			if(not noOk){
				cancelButton.prevFocus = okButton;
			}
			else{
				cancelButton.prevFocus = lastFocusable();
			}		
		} 
	}
	
	function firstFocusable(): Node {
		for(n in content){
			def node: Node = n as Node;
			if(node instanceof TabPanel){
				var tabContent: Node = (node as TabPanel).selected.content;
				if(tabContent instanceof Form){
					for(item in (tabContent as Form).formContent){
						if(item.fields.size()>0){
							return item.fields.get(0) as Node; 
						}
					}
				}
			}
			else if(node.visible){
				if(node instanceof Form){
					return findFirstInForm(node as Form);
				}
			}
		}
		if(not noOk){
			return okButton;
		}
		else{
			return null;
		}
	}
	
	function lastFocusable(): Node {
		for(i in [content.size()-1..0 step -1]){
			def node: Node = content.get(i) as Node;
			if(node instanceof TabPanel){
				var tabContent: Node = (node as TabPanel).selected.content;
				if(tabContent instanceof Form){
					for(j in [(tabContent as Form).formContent.size()-1..0 step -1]){
						def item = (tabContent as Form).formContent.get(j);
						if(item.fields.size() > 0){
							return item.fields.get(item.fields.size() - 1) as Node; 
						}
					}
				}
			}
			else if(node.visible){
				if(node instanceof Form){
					return findLastInForm(node as Form);
				}
			}
		}
		if(not noCancel){
			return cancelButton;
		}
		else{
			return null;
		}
	}
	
	function findFirstInForm(form: Form): Node {
		for(item in form.formContent){
			if(item.fields.size()>0){
				return item.fields.get(0) as Node; 
			}
		}
		return null;
	}
	
	function findLastInForm(form: Form): Node {
		for(j in [form.formContent.size()-1..0 step -1]){
			def item = form.formContent.get(j);
			if(item.fields.size() > 0){
				return item.fields.get(item.fields.size() - 1) as Node; 
			}
		}
		return null;
	}
	
	public function requestDefaultFocus(){
		if(not noOk and okButton != null){
			okButton.requestFocus();
		}
		else if(not noCancel and cancelButton != null){
			cancelButton.requestFocus();
		}
	}
}

public class ActionButton extends CustomNode {
	
	public var text: String;
	public var action: function();
	public var width: Integer = -1;
	
	public var selected: Boolean = false;
	var left: ImageView;
	var middle: ImageView;
	var right: ImageView;
	var content: Group;
	var label: Text;
	
	public var prevFocus: Node;
	public var nextFocus: Node;
	
	public var focusChangeListener: FocusChangeListener;
	
	init{
		focusTraversable = true;
	}
	
	var contentNormal = Group {
		content: [
			left = ImageView {
				layoutX: 0
				layoutY: 0
				image: Image {
					url: "{__ROOT__}images/png/dialog-button-left.png"
				}
			}, 
			middle = ImageView {
				layoutY:0
				layoutX: left.layoutBounds.width
				scaleX: bind if(width > - 1) width - 12 else label.layoutBounds.width + 14
				translateX: bind if(width > - 1) (width - 12) / 2 else (label.layoutBounds.width + 14) / 2 - .5
				image: Image {
					url: "{__ROOT__}images/png/dialog-button-middle-1px.png"
				}
			}, 
			right = ImageView {
				layoutY: 0
				layoutX: bind middle.boundsInParent.width + left.layoutBounds.width - 1
				image: Image {
					url: "{__ROOT__}images/png/dialog-button-right.png"
				}
			}, 
			label = Text {
				layoutX: bind if(width > - 1) (width - label.layoutBounds.width) / 2 + 1 else middle.boundsInParent.minX + 8
				layoutY: 14
				content: text
				font: Font.font("Arial", 9)
				fill: Color.web("#707070")
			}
			Rectangle{
				layoutX: 1
				layoutY: 1
				width: bind width - 2
				height: bind middle.layoutBounds.height - 2
				fill: Color.TRANSPARENT
				strokeWidth: 2.0
				stroke: Color.web("#1d9dfc")
				arcHeight: 12
				arcWidth: 12
				visible: bind focused
			}
		]
	}
	
	var contentActive = Group {
		content: [
			left = ImageView {
				layoutX: 0
				layoutY: 0
				image: Image {
					url: "{__ROOT__}images/png/dialog-button-active-left.png"
				}
			}, 
			middle = ImageView {
				layoutY:0
				layoutX: left.layoutBounds.width
				scaleX: bind if(width > - 1) width - 12 else label.layoutBounds.width + 14
				translateX: bind if(width > - 1) (width - 12) / 2 else (label.layoutBounds.width + 14) / 2 - .5
				image: Image {
					url: "{__ROOT__}images/png/dialog-button-active-middle-1px.png"
				}
			}, 
			right = ImageView {
				layoutY:0
				layoutX: bind middle.boundsInParent.width + left.layoutBounds.width - 1
				image: Image {
					url: "{__ROOT__}images/png/dialog-button-active-right.png"
				}
			}, 
			label = Text {
				layoutX: bind if(width > - 1) (width - label.layoutBounds.width) / 2 + 1 else middle.boundsInParent.minX + 8
				layoutY: 14
				content: text
				font: Font.font("Arial", 9)
				fill: Color.web("#707070")
			}
			Rectangle{
				layoutX: 1
				layoutY: 1
				width: bind width - 2
				height: bind middle.layoutBounds.height - 2
				fill: Color.TRANSPARENT
				strokeWidth: 2.0
				stroke: Color.web("#1d9dfc")
				arcHeight: 12
				arcWidth: 12
				visible: bind focused
			}
		]
	}
	
	override public function create():Node {
		Group {
			content: bind if (selected) contentActive else contentNormal
		}
	}
	
	override public var onMousePressed = function(e) {
		 //action(); 
		selected = true;
		requestFocus();
	}
	
	override public var onMouseReleased = function(e) {
		if( hover ) {
			action();
			//selected = true;
		} 
		else {
			//selected = false;
		}
		selected = false;
	}
	
	override public var onKeyPressed = function(e: KeyEvent) {
		if( e.code == KeyCode.VK_SPACE) {
			selected = true;
		}
		else if(not e.shiftDown and e.code == KeyCode.VK_TAB) {
			if(nextFocus != null){
				nextFocus.requestFocus();
			}
		}
		else if( e.shiftDown and e.code == KeyCode.VK_TAB) {
			if(prevFocus != null){
				prevFocus.requestFocus();
			}
		}  
	}
	
	override public var onKeyReleased = function(e: KeyEvent) {
		if( e.code == KeyCode.VK_ENTER) {
			action();
		}
	}
	
	var f: Boolean = bind focused on replace {
		if(focusChangeListener != null){
			if(focused){
				focusChangeListener.focusGained(this);
			}
			else{
				focusChangeListener.focusLost(this);
			}
		}
	}
}


