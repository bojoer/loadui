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
*ToolbarExpander.fx
*
*Created on mar 15, 2010, 10:51:30 fm
*/

package com.eviware.loadui.fx.ui.toolbar;

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Panel;
import javafx.scene.text.Text;
import javafx.scene.text.TextOrigin;
import javafx.scene.effect.Glow;
import javafx.fxd.FXDNode;
import javafx.geometry.Point2D;
import javafx.util.Math;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.pagination.Pagination;
import com.eviware.loadui.fx.AppState;

//import org.jfxtras.animation.wipe.XWipePanel;
import com.eviware.loadui.fx.ui.XWipePanel;
import org.jfxtras.animation.wipe.SlideWipe;
import org.jfxtras.scene.shape.MultiRoundRectangle;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.menu.button.MenuBarButton;

/**
 * Displays all the ToolbarItems inside a ToobarItemGroup when it is expanded.
 * If not all items fit on the screen at once, they are paginated, and buttons for paging are displayed.
 *
 * @author dain.nilsson
 */
public class ToolbarExpander extends CustomNode, Pagination {

   var closeBtnIconUrl: String = "images/close_btn.fxz";
	/**
	 * Background fill of the toolbar expander. Use background-fill property 
	 * in CSS to define background fill.
	 */
	public var backgroundFill: Paint = Color.rgb( 0x44, 0x44, 0x44, .8 );
	
	/**
	 * Border fill of the toolbar expander. Use border-fill property
	 * in CSS to define border fill.
	 */
	public var borderFill: Paint = Color.rgb( 0x26, 0x26, 0x26 );
	
	/**
	 * Title text fill of the toolbar expander. Use text-fill property 
	 * in CSS to define fill of expander title. 
	 */
	public var textFill: Paint = Color.rgb( 0x9c, 0x9c, 0x9c );
	
	/**
	 * Background fill of the horizontal bar. Use hr-background-fill property
	 * in CSS to define horizontal bar background fill.
	 */
	public var hrBackgroundFill: Paint = Color.rgb( 0x78, 0x78, 0x78 );
	
	/**
	 * Url for active left arrow. Use left-arrow-active-url property
	 * in CSS to define url of active left arrow.
	 */
	public var leftArrowActiveUrl: String = "images/cb_group_expanded_leftarrow_active.fxz";

	/**
	 * Url for inactive left arrow. Use left-arrow-inactive-url property
	 * in CSS to define url of inactive left arrow.
	 */
	public var leftArrowInactiveUrl: String = "images/cb_group_expanded_leftarrow_inactive.fxz";
	
	/**
	 * Background fill of left arrow container. Use left-arrow-background-fill property
	 * in CSS to define background fill for left arrow container.
	 */
	public var leftArrowBackgroundFill: Paint = Color.TRANSPARENT;

	/**
	 * Url for active right arrow. Use right-arrow-active-url property
	 * in CSS to define url of active right arrow.
	 */
	public var rightArrowActiveUrl: String = "images/cb_group_expanded_rightarrow_active.fxz";
	
	/**
	 * Url for inactive right arrow. Use right-arrow-inactive-url property
	 * in CSS to define url of inactive right arrow.
	 */
	public var rightArrowInactiveUrl: String = "images/cb_group_expanded_rightarrow_inactive.fxz";
	
	/**
	 * Background fill of right arrow container. Use right-arrow-background-fill property
	 * in CSS to define background fill for right arrow container.
	 */
	public var rightArrowBackgroundFill: Paint = Color.TRANSPARENT;

	/**
	 * A Group where this ToolbarExpander should be placed when shown.
	 */
	public-init var expandedHolder:Group;
	
	def glow = Glow { level: .5 };
	
	function buildContent():Group {
		var offset = 0;
		Group {
			content: for( node in displayedContent ) {
				node.layoutX = offset;
				offset += 100;
				node
			}
		}
	}
	
	def modalLayer = Rectangle {
		width: bind scene.width
		height: bind scene.height
		fill: Color.TRANSPARENT
		onMousePressed: function( e:MouseEvent ) {
			if( not contains( sceneToLocal( Point2D { x: e.sceneX, y: e.sceneY } ) ) )
				group = null;
		}
	}
	
	def slideWipe = SlideWipe {
		time: 200ms
	}

	/**
	 * The currently expanded ToolbarItemGroup.
	 */
	public var group:ToolbarItemGroup on replace oldGroup {
		delete modalLayer from AppState.getOverlay( scene ).content;
		delete this from expandedHolder.content;
		
		if( oldGroup != null ) {
			for( frame in content )
				(frame as ToolbarItemFrame).item = null;
			content = null;
			oldGroup.collapse();
		}
		
		if( group != null ) {
			content = for( item in group.items ) 
				ToolbarItemFrame { item:item };
			layoutY = group.layoutY - 12;
			insert this into expandedHolder.content;
			insert modalLayer into AppState.getOverlay( scene ).content;
		}
	}
	
	override var itemsPerPage = bind Math.min( sizeof content, ((scene.width as Integer) - 100) / 100);
	
	var wipePanel:XWipePanel;
	var oldGroup:Group;
	def displayedGroup:Group = bind lazy wipePanel.content[0] as Group;
	
	var wiping = false;
	override var onDisplayChange = function( oldContent:Node[], direction:Integer ):Void {
		def wipeDir = if( direction > 0 ) SlideWipe.RIGHT_TO_LEFT
			else if( direction < 0 ) SlideWipe.LEFT_TO_RIGHT
			else -1;
		if( wipeDir != -1 ) {
			wiping = true;
			slideWipe.direction = wipeDir;
			oldGroup = displayedGroup;
			wipePanel.next( buildContent() );
		} else {
			displayedGroup.content = null;
			wipePanel.content = buildContent();
		}
	}

	def leftArrow:Node = Group {
		layoutX: -12
		onMouseClicked: function( e:MouseEvent ) {
			if( e.button == MouseButton.PRIMARY and page > 0 and not wiping )
				page--;
		}
		visible: bind numPages > 1
		content: [
			Rectangle {
				width: 25
				layoutX: -5
				fill: bind leftArrowBackgroundFill
				height: Toolbar.GROUP_HEIGHT
			}, FXDNode {
				layoutY: 40
				url: bind "{__ROOT__}{leftArrowActiveUrl}"
				visible: bind page > 0
				effect: bind if(leftArrow.hover) glow else null
			}, FXDNode {
				layoutY: 40
				url: bind "{__ROOT__}{leftArrowInactiveUrl}"
				visible: bind page == 0
			}
		]
	}
	
	def rightArrow:Node = Group {
		layoutX: bind 100 * actualItemsPerPage + 20
		onMouseClicked: function( e:MouseEvent ) {
			if( e.button == MouseButton.PRIMARY and page < numPages - 1 and not wiping )
				page++;
		}
		visible: bind numPages > 1
		content: [
			Rectangle {
				width: 25
				layoutX: -5
				fill: bind rightArrowBackgroundFill
				height: Toolbar.GROUP_HEIGHT
			}, FXDNode {
				layoutY: 40
				url: bind "{__ROOT__}{rightArrowActiveUrl}"
				visible: bind page < numPages - 1
				effect: bind if(rightArrow.hover) glow else null
			}, FXDNode {
				layoutY: 40
				url: bind "{__ROOT__}{rightArrowInactiveUrl}"
				visible: bind page == numPages - 1
			}
		]
	}
	
	override function create() {
		Group {
			content: [
				MultiRoundRectangle {
					x: -1
					width: bind if(numPages == 1) 100 * actualItemsPerPage + 14 else 100 * actualItemsPerPage + 75
					height: Toolbar.GROUP_HEIGHT - 1
					fill: bind backgroundFill
					blocksMouse: true
					topRightHeight: 5
					topRightWidth: 5
					bottomRightHeight: 5
					bottomRightWidth: 5
					stroke: bind borderFill
				}, Text {
					x: 13
					y: 12
					content: bind group.category
					textOrigin: TextOrigin.TOP
					fill: bind textFill
				}, Group {
					layoutX: bind if( numPages == 1 ) 0 else 25
					content: [
						leftArrow, wipePanel = XWipePanel {
							content: buildContent()
							wipe: slideWipe
							width: bind 100 * actualItemsPerPage
							height: Toolbar.GROUP_HEIGHT
							action: function() {
								if( oldGroup != null )
									oldGroup.content = null;
								wiping = false;
							}
						}, Rectangle {
							layoutX: 13
							layoutY: 77
							fill: bind hrBackgroundFill
							height: 1
							width: bind 100 * actualItemsPerPage
						}, rightArrow, 
						MenuBarButton {
								blocksMouse: true
						      layoutY: 10
						      layoutX: bind 100 * actualItemsPerPage - 20
				            graphicUrl: bind "{__ROOT__}{closeBtnIconUrl}"
				            onMouseClicked: function(e:MouseEvent) {
									group = null;
				        		}
							}
					]
				}
			]
		}
	}
}
