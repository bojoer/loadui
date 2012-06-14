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
package com.eviware.loadui.fx.assertions;

import com.eviware.loadui.fx.util.ModelUtils;
import com.eviware.loadui.fx.util.ModelUtils.CollectionHolder;

import com.eviware.loadui.api.assertion.AssertionAddon;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.assertion.AssertionItem;

public class AssertionCollection {
	public-read var assertionItems:AssertionItem[];
	
	var testCaseCollection:CollectionHolder;
	var assertionItemCollections:CollectionHolder[] on replace {
		assertionItems = for( collection in assertionItemCollections ) collection.items as AssertionItem[];
	}
	
	public var project:ProjectItem on replace {
		testCaseCollection = CollectionHolder {
			owner: project
			key: ProjectItem.SCENES
			items: project.getChildren()
			onAdd: function( e ):Void {
				def newCollection = getAssertionCollection( e as CanvasItem );
				insert newCollection into assertionItemCollections;
				insert newCollection.items as AssertionItem[] into assertionItems;
			}
			onRemove: function( e ):Void {
				rebuildAssertionList();
			}
		}
		
		rebuildAssertionList();
	}
	
	function rebuildAssertionList() {
		assertionItemCollections = [
			getAssertionCollection( project ),
			for( testCase in testCaseCollection.items ) getAssertionCollection( testCase as CanvasItem )
		];
	}
	
	function getAssertionCollection( canvas:CanvasItem ):CollectionHolder {
		return CollectionHolder {
			owner: canvas
			key: AssertionAddon.ASSERTION_ITEMS
			items: canvas.getAddon( AssertionAddon.class ).getAssertions()
			onAdd: function( e ) { insert e as AssertionItem into assertionItems }
			onRemove: function( e ) { delete e as AssertionItem from assertionItems }
		}
	}
}