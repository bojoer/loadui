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
*DeleteProjectDialog.fx
*
*Created on feb 10, 2010, 15:11:25 em
*/

package com.eviware.loadui.fx.dialogs;

import javafx.scene.Scene;
import javafx.scene.text.Text;
import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.widgets.ModelItemHolder;

import com.eviware.loadui.api.model.BaseItem;
import com.eviware.loadui.api.traits.Labeled;

import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.dialogs.DeleteModelItemDialog" );

/**
 * Dialog allowing the user to confirm deletion of a ModelItem.
 * The ModelItem is specified by giving either a ModelItem or a ModelItemHolder instance.
 *
 * @author dain.nilsson
 */
public class DeleteModelItemDialog {
	/**
	 * The ModelItem to delete.
	 */
	public-init var modelItem:BaseItem;
	
	/**
	 * The ModelItemHolder to delete.
	 */
	public-init var modelItemHolder:ModelItemHolder;
	
	public var onOk: function(): Void;
	
	public var hostScene:Scene;
	
	postinit {
		if( not ( FX.isInitialized( modelItem ) or FX.isInitialized( modelItemHolder ) ) )
			throw new RuntimeException( "Neither modelItem nor modelItemHolder are set!" );
		
		def typeName = if( FX.isInitialized( modelItemHolder ) ) modelItemHolder.getTypeName() else modelItem.getClass().getSimpleName();
		if( not FX.isInitialized( modelItem ) ) modelItem = modelItemHolder.modelItem;
		
		def label = if( modelItem instanceof Labeled ) (modelItem as Labeled).getLabel() else "{modelItem}";
		
		def dialog:Dialog = Dialog {
			scene: if (hostScene == null) AppState.byName("MAIN").scene else hostScene
			title: "Delete {label}"
			content: [
				Text { content: "Are you sure you want to delete '{label}'?" },
			]
			okText: "Delete"
			onOk: function() {
				modelItem.delete();
				dialog.close();
				onOk();
			}
		}
	}
}
