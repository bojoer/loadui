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

/*
*StatisticsMenu.fx
*
*Created on jun 1, 2010, 10:51:10 fm
*/

package com.eviware.loadui.fx.statistics.topmenu;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Separator;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;

import com.javafx.preview.control.MenuButton;
import com.javafx.preview.control.MenuItem;
import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.ui.dialogs.*;
import com.eviware.loadui.fx.dialogs.*;
import com.eviware.loadui.fx.ui.menu.button.*;
import com.eviware.loadui.fx.widgets.TrashHole;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.resources.Paints;
import com.eviware.loadui.fx.ui.resources.MenuArrow;
import com.eviware.loadui.fx.ui.menu.MenubarButton;
import com.eviware.loadui.fx.statistics.StatisticsWindow;
import com.eviware.loadui.fx.statistics.chart.ChartPage;
import com.eviware.loadui.fx.statistics.topmenu.ExecutionSelector;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.reporting.ReportingManager;
import com.eviware.loadui.util.BeanInjector;

import java.lang.Exception;

import org.slf4j.LoggerFactory;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.ui.menu.StatisticsMenu" );

/**
 * The top menu in the statistics panel.
 *
 * @author henrik.olsson
 */
public class StatisticsMenu extends VBox {
	
	def reportingManager:ReportingManager = BeanInjector.getBean( ReportingManager.class );
	var executionSelector:ExecutionSelector;
	
	var menuButton:MenuButton;
	
	public var project:ProjectItem on replace {
		tabContainer.statisticPages = project.getStatisticPages();
	}
	
	var tabContainer:TabContainer;
	
	public def menuButtonFont: Font = Font { size:18 };
	//public def menuButtonLabel: String = "Analysis";
	
//	var mainExecution:Execution = bind StatisticsWindow.execution on replace {
//      runInFxThread( function():Void {
//      	if( StatisticsWindow.execution == null or StatisticsWindow.execution == StatisticsWindow.currentExecution )
//      		menuButton.text = bind leftExecution
//      	else
//      		menuButton.text = StatisticsWindow.execution.getLabel()
//      	} );
//   }
	
	public var onPageSelect:function( page:ChartPage ):Void;
	
	override var spacing = 2;
	
	init {
		var menuContent:Node;
		content = [
			Region {
				width: bind width, 
				height: bind height, 
				managed: false, 
				styleClass: "statistics-topMenu-background"
			}, ImageView {
				image: Image {
					url: "{__ROOT__}images/png/toolbar-background.png"
					width: width
				}
				fitWidth: bind width
				managed: false
				layoutY: -20
			}, Rectangle {
				width: 78
				height: 73
				fill: Color.rgb( 0, 0, 0, 0.1 )
				managed: false
			}, 
			
			HBox {
				content: [
					Label {
						layoutInfo: LayoutInfo {
							width: 88
						}
					}, VBox {
						content: [
							Label {
								text: "Results"
								textFill: Color.web("#666666")
								font: Font { name: "Arial", size: 10 }
								layoutInfo: LayoutInfo {
									height: 20
									margin: Insets { left: 3 }
								}
							}, HBox {
									layoutInfo: LayoutInfo {
										hgrow: Priority.ALWAYS
										vgrow: Priority.NEVER
										hfill: true
										vfill: false
										height: 45
										margin: Insets { left: -3 }
									}
									spacing: 3
									nodeVPos: VPos.CENTER
									content: [
										menuButton = MenuButton {
											styleClass: bind if( menuButton.showing ) "menu-button-showing" else "menu-button"
											layoutInfo: LayoutInfo { hshrink: Priority.SOMETIMES, minWidth: 100 }
											text: bind executionSelector.leftLabel.text
											font: menuButtonFont
											items: [
												MenuItem {
													text: "Close"
													action: function() {
														StatisticsWindow.currentChartPage.updateIcon();
														AppState.byName("STATISTICS").transitionTo( StatisticsWindow.STATISTICS_MANAGE, AppState.ZOOM_WIPE );
													}
												}
											]
										}, SeparatorButton {
												height: bind height,
												layoutInfo: LayoutInfo { margin: Insets { left: 4, top: 2 } }
										}, executionSelector = ExecutionSelector {
												layoutInfo: LayoutInfo { margin: Insets { left: 4, top: 2 } }
										}, Label {
											layoutInfo: LayoutInfo {
												hgrow: Priority.ALWAYS
												hfill: true 
											}
										}, SeparatorButton {
												height: bind height
										}, TrashHole {
												showTooltip: false
										}, SeparatorButton {
												height: bind height
										}, MenubarButton {
											shape: "M0,0 L0,12 10,12, 10,0 0,0 M4,13 L4,16 14,16 14,4 11,4 11,13 4,13"
											action: function() {
												StatisticsReportPrintDialog {}
											}
											disable: bind StatisticsWindow.execution == null
										}, MenubarButton {
											shape: "M2.46,10.21 C2.46,9.69 2.49,9.23 2.54,8.83 C2.59,8.43 2.69,8.07 2.82,7.75 C2.95,7.43 3.12,7.15 3.34,6.89 C3.55,6.63 3.82,6.39 4.15,6.15 C4.44,5.93 4.70,5.73 4.92,5.55 C5.14,5.36 5.32,5.18 5.47,4.99 C5.62,4.81 5.73,4.62 5.80,4.43 C5.87,4.25 5.91,4.03 5.91,3.80 C5.91,3.57 5.86,3.37 5.77,3.18 C5.67,2.99 5.54,2.82 5.36,2.68 C5.19,2.55 4.98,2.44 4.73,2.36 C4.48,2.29 4.21,2.25 3.90,2.25 C3.57,2.25 3.26,2.28 2.96,2.32 C2.67,2.37 2.39,2.44 2.12,2.52 C1.84,2.60 1.58,2.69 1.33,2.80 C1.08,2.90 0.83,3.01 0.58,3.13 L-0.00,1.18 C0.22,1.05 0.49,0.91 0.79,0.77 C1.10,0.63 1.45,0.50 1.83,0.39 C2.22,0.28 2.64,0.19 3.09,0.11 C3.55,0.04 4.04,0.00 4.56,0.00 C5.21,0.00 5.80,0.08 6.33,0.25 C6.86,0.42 7.32,0.65 7.70,0.96 C8.08,1.27 8.38,1.64 8.59,2.08 C8.80,2.52 8.90,3.02 8.90,3.57 C8.90,4.07 8.82,4.52 8.66,4.91 C8.50,5.30 8.29,5.66 8.03,5.99 C7.77,6.31 7.49,6.61 7.17,6.88 C6.85,7.15 6.53,7.41 6.21,7.67 C6.04,7.83 5.90,7.98 5.77,8.13 C5.64,8.28 5.53,8.46 5.45,8.65 C5.37,8.84 5.30,9.06 5.26,9.31 C5.22,9.56 5.20,9.86 5.20,10.21 Z M2.48,11.85 L5.25,11.85 L5.25,14.00 L2.48,14.00 Z"
											action: function():Void {
												openURL("http://www.loadui.org/Getting-results/the-statistics-window.html")
												}
										}, MenubarButton {
											shape: "M14.00,2.00 L12.00,0.00 7.00,5.00 2.00,0.00 0.00,2.00 5.00,7.00 0.00,12.00 2.00,14.00 7.00,9.00 12.00,14.00 14.00,12.00 9.00,7.00 Z"
											action: function():Void {
												StatisticsWindow.currentChartPage.updateIcon();
												AppState.byName("STATISTICS").transitionTo( StatisticsWindow.STATISTICS_MANAGE, AppState.ZOOM_WIPE );
											}
										}, Label {
											layoutInfo: LayoutInfo {
											width: 10 }
										}
									]
							}
						]
					}
				]
			},
			tabContainer = TabContainer {
				spacing: 36
				layoutInfo: LayoutInfo { height: 43, margin: Insets{top: 8} }
				statisticPages: project.getStatisticPages()
				onSelect: function(sp:StatisticPage):Void {
					onPageSelect( ChartPage { statisticPage: sp } );
				}
			},
			Region {
				width: bind width
				height: bind 5
				styleClass: "statistics-topMenu-bottomShadow"
			}
		];
	}
}
