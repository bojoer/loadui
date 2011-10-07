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
*Pagination.fx
*
*Created on feb 17, 2010, 12:56:43 em
*/

package com.eviware.loadui.fx.ui.pagination;

import javafx.scene.Node;
import javafx.util.Math;
import javafx.util.Sequences;

import java.util.Comparator;

public mixin class Pagination {

	/**
	 * In fluid mode, items are scrolled one by one, instead of page by page.
	 * This gives a fluid scroll behavior.
	 */
	protected var fluid = false;

	/**
	 * Optional Comparator to sort items by. If set, any time the items variable is changed, items will be sorted by this Comparator.
	 */
	public var comparator:Comparator;

	/**
	 * The number of items to show per page.
	 * Even if set to below 1, the component will always show at least one item per page.
	 */
	public var itemsPerPage:Integer on replace {
		actualItemsPerPage = Math.max( 1, itemsPerPage );
		refresh( 0 );
	}
	
	public-read var actualItemsPerPage:Integer;
	
	/**
	 * The nodes to display.
	 */
	public var items: Node[] on replace {
		if( comparator != null ) {
			def sorted = Sequences.sort( items, comparator ) as Node[];
			if( sorted != items ) {
				items = sorted;
			} else {
				refresh( 0 );
			}
		} else {
			refresh( 0 );
		}
	}
	
	/**
	 * The currently displayed page of Nodes, zero indexed.
	 */
	public var page:Integer = 0 on replace oldVal {
		refresh( page-oldVal );
	}
	
	/**
	 * The total number of pages.
	 */
	public-read var numPages:Integer = 1;
	
	/**
	 * The content displayed in the current page.
	 */
	public-read var displayedItems: Node[];
	
	public var onDisplayChange: function( oldContent:Node[], direction:Integer ):Void;
	
	var refreshing = false;
	function refresh( direction:Integer ) {
		if( refreshing )
			return;
		
		refreshing = true;
		numPages = if( fluid ) Math.max( sizeof items - actualItemsPerPage + 1, 1 ) else Math.max( Math.ceil( (sizeof items as Double) / actualItemsPerPage ) as Integer, 1 );
		if( page < 0 ) {
			page = 0;
		} else if( page >= numPages ) {
			page = numPages - 1;
		}
		
		def newContent = if( fluid ) items[page..page+actualItemsPerPage-1] else items[(actualItemsPerPage*page)..(actualItemsPerPage*(page+1)-1)];
		if( not Sequences.isEqualByContentIdentity( newContent, displayedItems ) ) {
			def oldContent = displayedItems;
			displayedItems = newContent;
			onDisplayChange( oldContent, direction );
		}

		refreshing = false;
	}
	
	init {
		actualItemsPerPage = Math.max( 1, itemsPerPage );
	}
}
