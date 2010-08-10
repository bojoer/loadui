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
import javafx.scene.control.Button;
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
public class Dialog {
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
	
	public-init var extraButtons:Button[];
	
	var modalLayer:Node;
	var panel:Node;
	
	var okButton: Button;
	var cancelButton: Button;

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
		
		var dialogButtons: Node[] = [] on replace {
			for( button in dialogButtons ) button.styleClass = "dialog-button";			
		}

		if( not noCancel ) {
			cancelButton = Button {
				translateX: - 19
				translateY: bind if(stripeVisible == true) - 43 else - 38
				text: cancelText
				action: function() { onCancel() }
				width: 59
				layoutInfo: nodeConstraints(new CC().tag( "cancel" ).width("60!"))
			}
			insert cancelButton into dialogButtons;
		}	
		if( not noOk ) {
			okButton = Button {
				translateX: - 19
				translateY: bind if(stripeVisible == true) - 43 else - 38
				text: okText
				action: function() { onOk() }
				layoutInfo: nodeConstraints(new CC().tag( "ok" ).width("60!"))
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
	
	def dummy = Group {};
	
	 /**
	 * Displays the Dialog.
	 */ 
	public function show() {
		insert modalLayer into overlay.content;
		insert panel into overlay.content;
		insert dummy into overlay.content;
		
		if( okButton != null )
			okButton.requestFocus();
	}
	
	 /**
	 * Closes the Dialog.
	 */ 
	public function close():Void {
		delete panel from overlay.content;
		delete modalLayer from overlay.content;
		delete dummy from overlay.content;
		onClose();
	}
}