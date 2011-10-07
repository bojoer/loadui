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
*TutorialList.fx
*
*Created on feb 10, 2010, 09:32:42 fm
*/

package com.eviware.loadui.fx.widgets;

import javafx.util.Sequences;
import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.CustomNode;
import javafx.scene.layout.Resizable;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.data.feed.rss.RssTask;
import javafx.data.feed.rss.Item;
import javafx.data.feed.rss.Channel;
import com.javafx.preview.control.MenuItem;
import com.javafx.preview.control.PopupMenu;

import java.util.EventObject;
import java.util.Comparator;
import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.pagelist.PageList;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.model.WorkspaceItem;
import java.io.IOException;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.TutorialList" );

/**
 * A list of all the Tutorials available.
 */
public class TutorialList extends CustomNode, Resizable {

	/**
	 * A reference to the current Workspace.
	 */
	public-init var workspace: WorkspaceItem;
	
	var pagelist:PageList;

	override function getPrefHeight( width:Float ) {
		pagelist.getPrefHeight( width );
	}
	
	override function getPrefWidth( height:Float ) {
		pagelist.getPrefWidth( height );
	}
	
	postinit {
		if( workspace == null )
			throw new RuntimeException( "Workspace must not be null!" );
		
		RssTask {
	        location: "http://www.loadui.org/component/option,com_ninjarsssyndicator/feed_id,1/format,raw/lang,en/"
	        interval: 300s
	        
			onException: function(e) {
	         if( e instanceof IOException ) {
					log.warn( "An error occured when downloading the news RSS feed." );
				} else {
					log.error( "An error occured when parsing the news RSS feed.", e );
				}
			}
	        
	       onItem: function(item:Item):Void {
	           FX.deferAction( function() {
		           insert TutorialNode {
		               label: item.title;
		               text: item.description
		               url: item.link
		           } into pagelist.items;
	             } );
	       }
	       
	       onChannel: function(channel:Channel):Void {
	           delete pagelist.items;
	       }
	
		}.start();
		
	}
	
	
	override function create() {
		
		
		pagelist = PageList {
			label: ##[TUTORIALS]"TUTORIALS"
			height: bind height
			width: bind width
			items: [
	/*			TutorialNode {
					url:"http://www.loadui.org/Getting-Started-with-loadUI/your-first-load-test.html"
					label:"First LoadUI Test"
				},
				TutorialNode {
				    url:"http://www.loadui.org/loadUI-Demo-Movies.html"
				    label:"Demo Movies"
				} */
			]
		};
		
		Group {
		     content: [
		         pagelist
		         ]
		 }
		
	}
}
