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
import javafx.scene.layout.Priority;
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
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Label;
import javafx.fxd.FXDNode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.dnd.MovableNode;
import com.eviware.loadui.fx.ui.button.GlowButton;
import com.eviware.loadui.fx.ui.resources.TitlebarPanel;
import com.eviware.loadui.fx.ui.resources.DialogPanel;
import com.eviware.loadui.fx.ui.resources.Paints;
import com.eviware.loadui.fx.ui.menu.MenubarButton;

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
	public var content: Node[] on replace {
		dialogContent.content = content;
	}
	
	 /**
	 * Allows manual control over the dialogs X coordinate.
	 */ 
	public var x:Number on replace {
		panel.layoutX = x - panel.layoutBounds.width / 2;
	}
	
	 /**
	 * Allows manual control over the dialogs Y coordinate.
	 */ 
	public var y:Number on replace {
		panel.layoutY = y - panel.layoutBounds.height / 2;
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
	public-init var okText:String = ##[OK]"OK";
	
	public-init var noOk = false;
	
	public var onOk:function():Void;
	
	public-init var cancelText:String = ##[CANCEL]"Cancel";
	
	public-init var noCancel = false;
	
	public var onCancel:function():Void = close;
	
	public-init var extraButtons:Button[];
	
	var modalLayer:Node;
	var panel:Node;
	protected var dialogPanel:DialogPanel;
	
	var okButton: Button;
	var cancelButton: Button;

	protected var dialogContent:VBox;
	var handle:BaseNode;
	var titlebarContent:HBox;
	
	init {
		def scene = AppState.instance.scene;
		
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
			//for( button in dialogButtons ) button.styleClass = "dialog-button";
		}

		if( not noCancel ) {
			cancelButton = Button {
				text: cancelText
				action: function() { onCancel() }
				layoutInfo: nodeConstraints(new CC().tag( "cancel" ).width("60!"))
			}
			insert cancelButton into dialogButtons;
		}	
		if( not noOk ) {
			okButton = Button {
				text: okText
				action: function() { onOk() }
				layoutInfo: nodeConstraints(new CC().tag( "ok" ).width("60!"))
			}
			insert okButton into dialogButtons;
		}
		
		insert extraButtons into dialogButtons;
		
		if( not FX.isInitialized( x ) ) {
			x = scene.width / 2;
		}
		if( not FX.isInitialized( y ) ) {
			y = scene.height / 2;
		}
		
		var titlebarPanel:TitlebarPanel;
		panel = MovableNode {
			layoutX: x
			layoutY: y
			useOverlay: false
			containment: sceneBounds
			contentNode: dialogPanel = DialogPanel {
				body: VBox {
					padding: Insets { left: 10, right: 10, bottom: 15, top: 3 }
					layoutInfo: LayoutInfo { hfill: true }
					content: [
						handle = BaseNode {
							managed: false
							contentNode: Rectangle { width: bind dialogPanel.width, height: 30, fill: Color.TRANSPARENT }
						},
						titlebarContent = HBox {
							layoutInfo: LayoutInfo { margin: Insets { right: -5 } }
							nodeVPos: VPos.CENTER
							content: [
								Label {
									textFill: Color.rgb(0, 0, 0, 0.5)
									text: bind title
									font: Font { size: 10 }
									layoutInfo: LayoutInfo {
										hfill: true
										hgrow: Priority.ALWAYS
										height: 26
									}
								}
							]
						},
						dialogContent = VBox {
							padding: Insets { left: 10, top: 10, right: 10, bottom: 10 }
							content: content
						},
						XMigLayout {
							constraints: new LC().fillX()
							rows: new AC().noGrid()
							content: dialogButtons
						}
					]
				}
			}
			handle: handle
		}
		
		if( helpUrl != null ) {
			insert MenubarButton {
				shape: "M2.46,10.21 C2.46,9.69 2.49,9.23 2.54,8.83 C2.59,8.43 2.69,8.07 2.82,7.75 C2.95,7.43 3.12,7.15 3.34,6.89 C3.55,6.63 3.82,6.39 4.15,6.15 C4.44,5.93 4.70,5.73 4.92,5.55 C5.14,5.36 5.32,5.18 5.47,4.99 C5.62,4.81 5.73,4.62 5.80,4.43 C5.87,4.25 5.91,4.03 5.91,3.80 C5.91,3.57 5.86,3.37 5.77,3.18 C5.67,2.99 5.54,2.82 5.36,2.68 C5.19,2.55 4.98,2.44 4.73,2.36 C4.48,2.29 4.21,2.25 3.90,2.25 C3.57,2.25 3.26,2.28 2.96,2.32 C2.67,2.37 2.39,2.44 2.12,2.52 C1.84,2.60 1.58,2.69 1.33,2.80 C1.08,2.90 0.83,3.01 0.58,3.13 L-0.00,1.18 C0.22,1.05 0.49,0.91 0.79,0.77 C1.10,0.63 1.45,0.50 1.83,0.39 C2.22,0.28 2.64,0.19 3.09,0.11 C3.55,0.04 4.04,0.00 4.56,0.00 C5.21,0.00 5.80,0.08 6.33,0.25 C6.86,0.42 7.32,0.65 7.70,0.96 C8.08,1.27 8.38,1.64 8.59,2.08 C8.80,2.52 8.90,3.02 8.90,3.57 C8.90,4.07 8.82,4.52 8.66,4.91 C8.50,5.30 8.29,5.66 8.03,5.99 C7.77,6.31 7.49,6.61 7.17,6.88 C6.85,7.15 6.53,7.41 6.21,7.67 C6.04,7.83 5.90,7.98 5.77,8.13 C5.64,8.28 5.53,8.46 5.45,8.65 C5.37,8.84 5.30,9.06 5.26,9.31 C5.22,9.56 5.20,9.86 5.20,10.21 Z M2.48,11.85 L5.25,11.85 L5.25,14.00 L2.48,14.00 Z"
				tooltip: Tooltip { text:"Help" }
				action: function() { openURL( helpUrl ); }
			} into titlebarContent.content;
			insert FXDNode {
				scaleY: .6
				url: "{__ROOT__}images/delimiter.fxz"
			} into titlebarContent.content;
		}
		
		
		if( closable ) {
			insert MenubarButton {
				shape: "M14.00,2.00 L12.00,0.00 7.00,5.00 2.00,0.00 0.00,2.00 5.00,7.00 0.00,12.00 2.00,14.00 7.00,9.00 12.00,14.00 14.00,12.00 9.00,7.00 Z"
				tooltip: Tooltip { text:"Close" }
				action: close
			} into titlebarContent.content;
		}
		
		if( showPostInit )
		show();
	}
	
	/**
	 * Displays the Dialog.
	 */ 
	public function show() {
		insert modalLayer into AppState.overlay;
		insert panel into AppState.overlay;
		
		if( okButton != null )
			okButton.requestFocus();
		//AppState.overlay.layout();
		panel.layoutY = y - panel.layoutBounds.height / 2;
		panel.layoutX = x - panel.layoutBounds.width / 2;
	}
	
	 /**
	 * Closes the Dialog.
	 */ 
	public function close():Void {
		delete panel from AppState.overlay;
		delete modalLayer from AppState.overlay;
		onClose();
	}
}