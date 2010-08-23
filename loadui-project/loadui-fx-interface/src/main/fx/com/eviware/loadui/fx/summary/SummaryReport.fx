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
import com.eviware.loadui.fx.StylesheetAware;

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
import java.util.Locale;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;

import java.lang.IllegalArgumentException;

import javax.swing.table.TableModel;

import java.lang.Exception;
import java.text.SimpleDateFormat;

public class SummaryReport extends StylesheetAware {

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
	public var x: Number on replace {
		panel.layoutX = x;
	}
	
	/**
	 * Allows manual control over the dialogs Y coordinate.
	 */ 
	public var y: Number on replace {
		panel.layoutY = y;
	}
	
    /**
	 * Set to false to prevent the dialog from automatically showing after being created.
	 */ 
	public-init var showPostInit = true;
	
	public-init var summary:Summary;
	
	 /**
	 * Defines the Paint for the background.
	 */ 
	public var backgroundFill: Paint = Color.web("#F9F2B7");
	public var strokeFill: Paint = Color.web("#c4c4c4");
	
	public var leftBackgroundFill: Paint = Color.web("#F9F2B7");
	public var indexFill: Paint = Color.web("#F2E25E");
	
	public-init var width: Number = 670;
	
	public-init var height: Number = 900;
	
	var modalLayer: Node;
	
	var summarySections: VBox;
	var tables: Node[] = [];
	
	public-init var select: String  = "";
	
	//current selected chapter
	var chapter: Chapter on replace {
		if(chapter != null){
			title = chapter.getTitle();
			
			var sdf: SimpleDateFormat = new SimpleDateFormat("d MMM yyyy hh:mm a", Locale.ENGLISH);
			date = sdf.format(chapter.getDate()).toLowerCase();
			descr = chapter.getDescription();
			
			delete summarySections.content;
			
			var sections = chapter.getSections();
			for(s in sections){
				var section = s as Section;
				if(not isSectionEmpty(section)){
					var values: KeyValue[];
					var keys: Iterator = section.getValues().keySet().iterator();
					while(keys.hasNext()){
						var key: String = keys.next() as String;
						insert
							KeyValue{
								key: key
								value: section.getValues().get(key)
							}
						into values; 
					}
					if(section.getTitle().equals(chapter.getTitle())){
						summaryHeader.values = values;
					}
					else{
						var summarySection: SummarySection = SummarySection{title: section.getTitle()}
						summarySection.values = values;
						insert summarySection.get() into summarySections.content;
						
						var tables: Map = section.getTables();
						var tKeys: Iterator = tables.keySet().iterator();
						
						for( key in tables.keySet() ) {
							def table = tables.get(key) as TableModel;
							if( table.getRowCount() > 0 ) {
								insert createSpacer(30) into summarySections.content;
								insert SummaryTable { title: key as String, table: table } into summarySections.content;
							}
						}
						
						insert createSpacer(50) into summarySections.content;
						insert createWideLine(30) into summarySections.content;
						insert createSpacer(10) into summarySections.content;
					}
				}
			}
		
			var descSection: SummaryText = SummaryText{
				title: "Description" 
				text: descr
			}
			insert descSection into summarySections.content;
			insert createSpacer(50) into summarySections.content;
		}
	}
	
	function isSectionEmpty(section: Section): Boolean {
		if(section.getValues().size() > 0){
			return false;
		}
		
		var tables: Map = section.getTables();
		var tKeys: Iterator = tables.keySet().iterator();
		while(tKeys.hasNext()){
			if((tables.get(tKeys.next()) as TableModel).getRowCount() > 0){
				return false;
			}		
		}
		return true;
	}
	
	var panel: Node;
	var summaryHeader: SummaryHeader;
	var centerPanel: Node;
	var sceneBounds: BoundingBox;
	var scene = AppState.instance.scene;
	
	public var title: String = "";
	public var date: String = "";
	public var descr: String = "";
	
	var w: Number = bind scene.width on replace {
		sceneBounds = BoundingBox {
			width: scene.width
			height: scene.height
		}
		x = ( scene.width - centerPanel.boundsInLocal.width ) / 2;
	}
	
	var h: Number = bind scene.height on replace {
		sceneBounds = BoundingBox {
			width: scene.width
			height: scene.height
		}
		//y = ( scene.height - centerPanel.boundsInLocal.height ) / 2;
		y = 30;
		height = scene.height - 60;
	}
	
	var paddingRight: Number = 17;
	var paddingLeft: Number = 30;
	var paddingTop: Number = 10;
	
	public var leftPanelHeaderHeight: Number = 22;
	public var leftPanelYOffset: Number = 30;
	public var leftPanelHeight: Number = 460;
	public var leftPanelWidth: Number = 125;
	
	init {
		
		sceneBounds = BoundingBox {
			width: scene.width
			height: scene.height
		}
		
		modalLayer = Rectangle {
			width: bind scene.width
			height: bind scene.height
			fill: Color.BLACK
			opacity: 0.3
			visible: bind modal
			blocksMouse: bind modal
		}
		
		var header: HBox = HBox {
			layoutInfo: LayoutInfo {
				hgrow: Priority.ALWAYS vgrow: Priority.NEVER
				hfill: true vfill: false
				height: 55
			}
			nodeVPos: VPos.CENTER
			spacing: 0
			content: [
				Label {
					layoutInfo: LayoutInfo {
						hgrow: Priority.NEVER
						hfill: false	
				   }
					text: "Summary for "
					textFill: Color.web("#000000")
					textWrap: false
					font: Font { name:"Arial" size: 18 }
					vpos: VPos.CENTER
				}
				Label {
					layoutInfo: LayoutInfo {
						hgrow: Priority.ALWAYS
						hfill: true
				    }
					text: bind "{title}"
					textFill: Color.web("#666666")
					textWrap: false
					font: Font { name:"Arial" size: 18 }
					vpos: VPos.CENTER
				}
			]
		}	
		
		summaryHeader = SummaryHeader{width: bind width - paddingLeft - paddingRight};
		
		summarySections = VBox {
			layoutInfo: LayoutInfo {
				hgrow: Priority.ALWAYS vgrow: Priority.NEVER
				hfill: true vfill: false
			}
			spacing: 0
			content: []
		}
		
		var headerButtons: HBox;
		
		centerPanel = Group {
			content: [
				Rectangle {
					x:0
					y:0
					fill: bind backgroundFill
					width: bind width
					height: bind height
					arcWidth: 24
					arcHeight: 24
					stroke: bind strokeFill
					strokeWidth: 1.0
					effect: DropShadow { width: 40.0 height: 40.0 }
				}
				headerButtons = HBox {
					layoutX: bind width - 45 - headerButtons.layoutBounds.width + 28
					layoutY: 15
					spacing: 10
					content: [
						Label {
							layoutY: 3
							text: bind date
							textFill: Color.web("#000000")
							font: Font { name:"Arial" size: 10 }
						}
					    Button {
					        text: "Print"
					        onMouseReleased:function(event) {
					            com.eviware.loadui.util.reporting.JasperReportManager.getInstance().createReport(summary);
					        }
					    }
						GlowButton {
							contentNode: FXDNode {
								translateY: 2
								url: "{__ROOT__}images/close_btn.fxz"
							}
							tooltip: "Close"
							action: close
							width: 28
							height: 30
						}
					]
				}
				VBox {
					layoutInfo: LayoutInfo {
						height: bind height
						width: bind width	
					}
					padding: Insets { top: paddingTop right: paddingRight bottom: 17 left: paddingLeft}
					spacing: 0
					content: [
						header,
						createWideLine(),
						summaryHeader.get(),
						createWideLine(),
						createSpacer(15),
						ScrollView {
							styleClass: "summary-scroll-view"
							layoutInfo: LayoutInfo {
								hgrow: Priority.ALWAYS vgrow: Priority.ALWAYS
								hfill: true vfill: true
							}
				     	    node: summarySections
				     	    hbarPolicy: ScrollBarPolicy.NEVER
				     	    vbarPolicy: ScrollBarPolicy.ALWAYS //AS_NEEDED
			     	    }
					]
				}
			]
		}
		
		panel = MovableNode {
			useOverlay: false
			containment: bind sceneBounds
			contentNode: Group {
				content: [
					Group {
						content: [
							Rectangle {
								x: bind -leftPanelWidth
								y: bind leftPanelYOffset
								fill: bind leftBackgroundFill
								width: 150
								height: bind leftPanelHeight
								arcWidth: 14
								arcHeight: 14
								stroke: bind strokeFill
								strokeWidth: 1.0
								effect: DropShadow { width: 40.0 height: 40.0 }
							}
							Rectangle {
								x: bind -leftPanelWidth
								y: bind leftPanelYOffset
								fill: bind indexFill
								width: 150
								height: 35
								arcWidth: 14
								arcHeight: 14
								stroke: bind strokeFill
								strokeWidth: 1.0
							}
							Rectangle {
								x: bind -leftPanelWidth
								y: bind leftPanelYOffset + leftPanelHeaderHeight
								fill: bind leftBackgroundFill
								width: 150
								height: 35
							}
							Line {
								stroke: Color.rgb(0, 0, 0, 0.2)
								strokeWidth: 1.0
								startX: bind -leftPanelWidth
								endX: 0
								startY: bind leftPanelYOffset + leftPanelHeaderHeight - 1
								endY: bind leftPanelYOffset + leftPanelHeaderHeight - 1
							}
							Line {
								stroke: Color.rgb(0, 0, 0, 0.2)
								strokeWidth: 1.0
								startX: bind -leftPanelWidth
								endX: 0
								startY: bind leftPanelYOffset + leftPanelHeaderHeight - 1 + 36
								endY: bind leftPanelYOffset + leftPanelHeaderHeight - 1 + 36
							}
							Label {
								layoutX: bind -leftPanelWidth + 13
								layoutY: bind leftPanelYOffset
								layoutInfo: LayoutInfo{
									height: leftPanelHeaderHeight
								}
								text: "Summary Index"
								textFill: Color.rgb(51, 51, 51, 0.6)
								font: Font.font("Arial", FontWeight.BOLD, 10)
								vpos: VPos.CENTER
								hpos: HPos.LEFT
							}
							VScroller {
								layoutX: bind -leftPanelWidth
								layoutY: bind leftPanelYOffset + leftPanelHeaderHeight
								height: bind leftPanelHeight - leftPanelHeaderHeight
								width: bind leftPanelWidth
								itemSpacing: 0
								arrowVSpace: 36
								topArrowActive: "{__ROOT__}images/summary-index-top-arrow-active.fxz";
								topArrowInactive: "{__ROOT__}images/summary-index-top-arrow-inactive.fxz";
								bottomArrowActive: "{__ROOT__}images/summary-index-bottom-arrow-active.fxz";
								bottomArrowInactive: "{__ROOT__}images/summary-index-bottom-arrow-inactive.fxz";
								content: getChapters()
							}
							Rectangle {
								x: bind -leftPanelWidth + 1
								y: bind leftPanelYOffset + 1
								fill: Color.TRANSPARENT
								width: 150 - 2
								height: bind leftPanelHeight - 2
								arcWidth: 13
								arcHeight: 13
								stroke: Color.rgb(255, 255, 255, 0.25)
								strokeWidth: 2.0
							}
							centerPanel
						]
					}
				]
			}
		}
		
		x = ( scene.width - centerPanel.boundsInLocal.width ) / 2;
		//y = ( scene.height - centerPanel.boundsInLocal.height ) / 2;
		y = 30;
		
		setDefault();
		
		if(showPostInit){
			show();
		}
	}
	
	function setDefault(): Void {
		var chapters: Map = summary.getChapters();
		chapter = chapters.get(select) as Chapter;
	}
	
	function getChapters(): Node[] {
		var nodes: Node[] = [];
		
		var testCaseGroup = SummaryButtonGroup{}
		var chapters: Map = summary.getChapters();
		
		var keys: Iterator = chapters.keySet().iterator();
		while(keys.hasNext()){
			var chapterName: String = keys.next() as String;
			insert
				SummaryButton{
					text: chapterName
					group: testCaseGroup
					selected: chapterName.equals(select)
					action: function(){
						chapter = chapters.get(chapterName) as Chapter;
					}
				}
			into nodes; 
		}
		nodes;
	}
	
	function createWideLine(): Line {
		createWideLine(0);
	}
	function createWideLine(additionalPadding: Number): Line {
		Line {
			stroke: Color.rgb(0, 0, 0, 0.2)
			strokeWidth: 4.0
			endX: bind width - paddingRight - paddingLeft - 3 - additionalPadding
		}
	}
	
	function createSpacer(height: Number): Rectangle {
		Rectangle {
			width: 1
			height: height
			fill: Color.TRANSPARENT
		}
	}
	
	 /**
	 * Displays the Dialog.
	 */ 
	public function show() {
		insert modalLayer into AppState.overlay;
		insert panel into AppState.overlay;
	}
	
	 /**
	 * Closes the Dialog.
	 */ 
	public function close():Void {
		delete panel from AppState.overlay;
		delete modalLayer from AppState.overlay;
	}
	

} 



