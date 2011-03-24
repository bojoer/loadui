/* 
 * Copyright 2011 eviware software ab
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
import javafx.scene.layout.Stack;
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
import javafx.scene.control.Separator;
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
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.reporting.ReportingManager;

import java.lang.IllegalArgumentException;

import javax.swing.table.TableModel;

import java.lang.Exception;
import java.text.SimpleDateFormat;

public class SummaryReport extends Stack {
	def sdf: SimpleDateFormat = new SimpleDateFormat("d MMM yyyy hh:mm a", Locale.ENGLISH);
	
	def reportingManager:ReportingManager = BeanInjector.getBean( ReportingManager.class );
	
	var navigator:VBox;
	var main:VBox;
	var headerStack:Stack;
	var vScroller:VScroller;
	
	override var layoutInfo = LayoutInfo { width: bind scene.width, height: bind scene.height };
	override var styleClass = "summary-report";

	/**
	 * Whether to make the dialog modal or not.
	 */ 
	public var modal = true;
	
	
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
	public-init var showPostInit = false;
	
	public-init var summary:Summary;
	
	 /**
	 * Defines the Paint for the background.
	 */ 
	public var backgroundFill: Paint = Color.web("#F9F2B7");
	public var strokeFill: Paint = Color.web("#c4c4c4");
	
	public var leftBackgroundFill: Paint = Color.web("#F9F2B7");
	public var indexFill: Paint = Color.web("#F2E25E");
	
	var modalLayer: Node;
	
	var summarySections: VBox;
	var tables: Node[] = [];
	
	public-init var select: String  = "";
	
	//current selected chapter
	var chapter: Chapter on replace {
		if(chapter != null){
			title = chapter.getTitle();
			
			//var sdf: SimpleDateFormat = new SimpleDateFormat("d MMM yyyy hh:mm a", Locale.ENGLISH);
			date = sdf.format(chapter.getDate()).toLowerCase();
			if ( chapter.getDescription() == null or chapter.getDescription().trim().length() == 0 )
		    	descr = "N/A"
		    else
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
		summaryHeader = SummaryHeader{ layoutInfo: LayoutInfo { hgrow: Priority.ALWAYS, hfill: true } };
		
		summarySections = VBox {
			layoutInfo: LayoutInfo {
				hgrow: Priority.ALWAYS vgrow: Priority.NEVER
				hfill: true vfill: false
			}
			spacing: 0
			content: []
		}
		
		content = [
			Rectangle {
				width: bind scene.width
				height: bind scene.height
				opacity: 0.3
				blocksMouse: true
			}, HBox {
				layoutInfo: LayoutInfo { width: 800, maxWidth: 800, margin: Insets { top: 30, bottom: 30 } }
				content: [
					navigator = VBox {
						layoutInfo: LayoutInfo { height: 460, maxHeight: 460, margin: Insets { top: 30 } }
						spacing: 10
						content: [
							Rectangle {
								width: bind navigator.width + 14
								height: bind navigator.height
								managed: false
								arcWidth: 14
								arcHeight: 14
								fill: bind leftBackgroundFill
								stroke: bind strokeFill
								strokeWidth: 1.0
								effect: DropShadow { width: 40.0 height: 40.0 }
							}, Label {
								styleClass: "summary-index-label"
								text: "Summary Index"
								layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS, height: 22 }
							}, vScroller = VScroller {
								layoutInfo: LayoutInfo { width: 125, height: 440 }
								itemSpacing: 0
								arrowVSpace: 36
								topArrowActive: "{__ROOT__}images/summary-index-top-arrow-active.fxz";
								topArrowInactive: "{__ROOT__}images/summary-index-top-arrow-inactive.fxz";
								bottomArrowActive: "{__ROOT__}images/summary-index-bottom-arrow-active.fxz";
								bottomArrowInactive: "{__ROOT__}images/summary-index-bottom-arrow-inactive.fxz";
								items: getChapters()
							}
						]
					}, main = VBox {
						layoutInfo: LayoutInfo { width: 675, vfill: true }
						padding: Insets { left: 30, top: 15, right: 15, bottom: 15 }
						spacing: 12
						content: [
							Rectangle {
								width: bind main.width
								height: bind main.height
								managed: false
								fill: bind leftBackgroundFill
								arcWidth: 24
								arcHeight: 24
								stroke: bind strokeFill
								strokeWidth: 1.0
								effect: DropShadow { width: 40.0 height: 40.0 }
							}, HBox {
								nodeVPos: VPos.CENTER
								spacing: 10
								content: [
									Label { layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS } },
									Label { text: bind date },
									Button {
										text: "Print"
										action:function() {
										    AppState.byName("MAIN").blockingTask( function():Void {
		    									reportingManager.createReport( summary );
		    								}, null, "Generating Printable Report..." );
										}
									}, GlowButton {
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
							}, HBox {
								layoutInfo: LayoutInfo { margin: Insets { top: -15 } }
								styleClass: "summary-header"
								content: [
									Label { text: "Summary for " }
									Label { text: bind title, textFill: Color.rgb( 0x66, 0x66, 0x66 ) }
								]
							}, Separator {
							}, headerStack = Stack {
								layoutInfo: LayoutInfo { hfill: true hgrow: Priority.ALWAYS, height: 56 }
								padding: Insets { left: 15 }
								nodeHPos: HPos.LEFT
								content: [
									Rectangle {
										height: bind headerStack.height
										width: bind headerStack.width
										fill: Color.web("#d9d39f");
										arcWidth: 14
										arcHeight: 14
										managed: false
									}, summaryHeader.get()
								]
							}, Separator {
							}, ScrollView {
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
		];
		
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
		insert this into AppState.byName("MAIN").overlay.content;
	}
	
	 /**
	 * Closes the Dialog.
	 */ 
	public function close():Void {
		delete this from AppState.byName("MAIN").overlay.content;
	}
	

} 



