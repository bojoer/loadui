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
/*
*FeedDisplay.fx
*
*Created on may 11, 2010, 11:46:53 am
*/

package com.eviware.loadui.fx.widgets;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.VPos;
import org.slf4j.LoggerFactory;
import javafx.scene.Node;
import com.eviware.loadui.fx.ui.node.BaseNode;
import javafx.scene.Group;

import javafx.data.feed.rss.RssTask;
import javafx.scene.control.Label;
import javafx.scene.layout.Resizable;
import javafx.data.feed.rss.Item;
import javafx.data.feed.rss.Channel;

import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

import javafx.scene.control.ScrollView;
import javafx.scene.control.ScrollBarPolicy;
import javafx.scene.control.Hyperlink;
import com.eviware.loadui.fx.FxUtils;
import javafx.scene.text.Font;

import javafx.util.Math;
import java.io.IOException;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.FeedDisplay" );

/**
 * A component to display items from the Eviware feed
 */
public class FeedDisplay extends BaseNode, Resizable {
	def feedUrl:String = "http://loadUI.org/xml/loadui_news.xml";
	var items:NewsItem[];
	
	
	def feedTask:RssTask = RssTask{
			location: feedUrl
			interval: 60s
			
			onException: function(e) {
				if( e instanceof IOException ) {
					log.warn( "An error occured when downloading the news RSS feed." );
				} else {
					log.error( "An error occured when parsing the news RSS feed.", e );
				}
			}
			
		onItem: function(item:Item):Void {
			FX.deferAction( function() {
				insert NewsItem {
					title: item.title;
					text: item.description
					image: item.enclosure.url
					url: item.link
					date: item.pubDate
				} into items;
				} );
		}
		
		onChannel: function(channel:Channel):Void {
			delete items;
		}
	
	}
	
	def vbox:VBox = VBox {
			
			width: bind width
				content:  
					bind items
				
			}
	
	var mainFeed:Group = Group {
				content: [
					Rectangle {
						height: bind Math.max(vbox.height, height)
						width: bind 300
						fill: Color.web("#313030")
					},
					VBox {
						content: [
							vbox,
							HBox {
								spacing: 6
								nodeVPos: VPos.CENTER
								content: [
									javafx.scene.control.Label {
										text: "More news on:"
										font: Font.font("Amble", 12)
										style: "-fx-text-fill: #c7c7c7" 
									},
									Hyperlink {
										font: Font.font("Amble", 12)
										text: "www.eviware.com/loadui/news"
										style: "-fx-text-fill: #006b33"
										action: function() {
											FxUtils.openURL("http://www.eviware.com/loadui/news");
										}
									}
								]
							}
						]
					}
				]
			}
	
	override function create() {
		if( java.lang.System.getProperty("noFeed") == null )
			feedTask.start();
		var sc:ScrollView;
		var lb: Label;
		Group {
			content:[  
				Rectangle {
					height: bind height
					width: 320
					arcWidth: 24
					arcHeight: 24
					fill: Color.web("#313030")
				},
				VBox {
					layoutY: 20
					layoutX: 10
					content: [
						HBox {
							spacing: 100
							content: [
								lb = Label {
									text: "NEWS"
									font: Font.font("Amble", 12)
									textFill: Color.GREY
								},
								Hyperlink {
									font: Font.font("Amble", 12)
									text: "www.eviware.com/loadui/news"
									style: "-fx-text-fill: #006b33; -fx-border-color: #313030"
									visited: false
									action: function() {
										FxUtils.openURL("http://www.eviware.com/loadui/news");
									}
								}		
							]
						},
						Rectangle {
							width: bind width
							height: 10
							fill: Color.TRANSPARENT
						},
						Rectangle {
							width: bind width + 20
							height: bind 2
							fill: Color.web("#232323")
						},
						Rectangle {
							width: bind width
							height: 10
							fill: Color.TRANSPARENT
						},
						sc = ScrollView {
								height: bind height - 70
								styleClass:"feed-scroll-view"
								width: bind 300
								node: mainFeed
								hbarPolicy: ScrollBarPolicy.NEVER
						}
					]
				}
			]
		}
	}	
	
	override function getPrefWidth( height:Float ) { 
		vbox.getPrefWidth( height )
	}
		
	override function getPrefHeight( width:Float ) { 
		vbox.getPrefHeight( width )
	}
}
