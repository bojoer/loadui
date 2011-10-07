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

import com.eviware.loadui.api.summary.Summary;
import com.eviware.loadui.api.summary.Chapter;
import com.eviware.loadui.api.summary.Section;

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
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.geometry.BoundingBox;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.fxd.FXDNode;
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
import javafx.scene.text.TextAlignment;

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

import javafx.scene.layout.ClipView;
import javafx.scene.control.ScrollBar;

import javafx.scene.control.ScrollView;
import javafx.scene.control.ScrollBarPolicy;

import java.util.Map;
import java.util.Iterator;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;

import java.lang.IllegalArgumentException;

import javax.swing.table.TableModel;

import java.lang.Exception;
import java.text.SimpleDateFormat;

import javafx.util.Math;
import javafx.util.Sequences;
import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.Container;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Tile;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Label;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.VBox;
import javafx.fxd.FXDNode;
import javafx.scene.effect.Glow;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.pagination.Pagination;

//import org.jfxtras.animation.wipe.XWipePanel;
import com.eviware.loadui.fx.ui.XWipePanel;
import org.jfxtras.animation.wipe.SlideWipe;

public class VScroller extends CustomNode, Resizable, Pagination {
	
	var itemHeight: Number = bind Math.max(tiles[0].tileHeight, 10);
	
	public-init var itemSpacing = 0;
	
	override var onDisplayChange = function( oldContent:Node[], direction:Integer ):Void {
		def wipeDir = if( direction < 0 ) SlideWipe.TOP_TO_BOTTOM
			else if( direction > 0 ) SlideWipe.BOTTOM_TO_TOP
			else -1;

		if( wipeDir != -1 ) {
			oldContainer = displayedContainer;
			slideWipe.direction = wipeDir;
			wiping = true;
			wipe.next( buildContainer() );
		} else {
			displayedContainer.content = null;
			wipe.content = buildContainer();
		}
	}
	
	/**
	 * The width that is given to the actual contents.
	 */
	public-read def contentHeight = bind height - 2 * arrowVSpace on replace {
		fixItemsPerPage();
	}
	
	override var items on replace {
		fixItemsPerPage();
	}
	
	function fixItemsPerPage():Void {
		var i = 1;
		while( ( i + 1 ) * ( itemHeight + itemSpacing ) < contentHeight + itemSpacing )
			i++;
		itemsPerPage = i;
	}
	
	def displayedGroup = bind wipe.content[0] as Group;
	def displayedContainer = bind displayedGroup.content[0] as Container;
	var wipe: XWipePanel;
	var slideWipe: SlideWipe;
	
	public var arrowVSpace: Number = 50;
	
	var upArrow:Node;
	var bottomArrow:Node;
	
	var wiping = false;

	var oldContainer:Container = null;
	
	var tiles:Tile[];
	function buildContainer():Group {
		Group {
			layoutY: bind arrowVSpace
			content: tiles = [
				Tile {
					height: bind contentHeight + itemSpacing
					vgap: bind itemSpacing
					nodeVPos: VPos.CENTER
					nodeHPos: HPos.LEFT
					content: displayedItems
					vertical: true
				}
			]
		}
	}
	
	public var topArrowActive: String = "{__ROOT__}images/leftarrow_active.fxz";
	public var topArrowInactive: String = "{__ROOT__}images/leftarrow_inactive.fxz";
	public var bottomArrowActive: String = "{__ROOT__}images/rightarrow_active.fxz";
	public var bottomArrowInactive: String = "{__ROOT__}images/rightarrow_inactive.fxz";
	
	override function create():Node {
		def glow = Glow { level: .5 };
		
		Group {
			content: [
				Group {
					content: [
						upArrow = Group {
							layoutX: bind ( width - upArrow.layoutBounds.width ) / 2
							layoutY: bind ( arrowVSpace - upArrow.layoutBounds.height ) / 2
							content: [
								FXDNode {
									url: bind topArrowActive
									visible: bind page > 0
									effect: bind if( upArrow.hover ) glow else null
								}, FXDNode {
									url: bind topArrowInactive
									visible: bind page <= 0
								}
							]
							blocksMouse: true
							onMousePressed: function( e:MouseEvent ) { if( e.primaryButtonDown and not wiping and page > 0 ) page-- }
						} 
						bottomArrow = Group {
							layoutX: bind ( width - upArrow.layoutBounds.width ) / 2
							layoutY: bind height - arrowVSpace + ( arrowVSpace - upArrow.layoutBounds.height ) / 2
							content: [
								FXDNode {
									url: bind bottomArrowActive
									visible: bind page < numPages - 1
									effect: bind if( bottomArrow.hover ) glow else null
								}, FXDNode {
									url: bind bottomArrowInactive
									visible: bind page >= numPages - 1
								}
							]
							blocksMouse: true
							onMousePressed: function( e:MouseEvent ) { if( e.primaryButtonDown and not wiping and page < (numPages-1) ) page++ }
						}
					]
				}
				wipe = XWipePanel {
					height: bind contentHeight
					width: bind width
					layoutX: 0
					layoutY: 0
					wipe: slideWipe = SlideWipe {
						time: 200ms
					}
					action: function() {
						if( oldContainer != null ) {
							oldContainer.content = null;
							oldContainer = null;
						}
						wiping = false;
					}
					content: buildContainer()
				}
			]
		}
	}
	
	override function getPrefHeight( width: Float ) {
		displayedContainer.getPrefHeight(width)
	}
	
	override function getPrefWidth( height: Float ) {
		displayedContainer.getPrefWidth(height)
	}
	
}
