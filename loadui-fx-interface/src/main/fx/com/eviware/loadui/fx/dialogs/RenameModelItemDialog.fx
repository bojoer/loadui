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
*DeleteProjectDialog.fx
*
*Created on feb 10, 2010, 15:11:25 em
*/

package com.eviware.loadui.fx.dialogs;

import javafx.scene.layout.LayoutInfo;
import javafx.scene.text.Text;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.form.fields.*;
import com.eviware.loadui.fx.widgets.ModelItemHolder;

import com.eviware.loadui.api.model.ModelItem;

import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.dialogs.DeleteModelItemDialog" );

/**
 * Dialog allowing the user to rename a ModelItem.
 * The ModelItem is specified by giving either a ModelItem or a ModelItemHolder instance.
 *
 * @author dain.nilsson
 */
public class RenameModelItemDialog {
	/**
	 * The ModelItem to delete.
	 */
	public-init var modelItem:ModelItem;
	
	/**
	 * The ModelItemHolder to delete.
	 */
	public-init var modelItemHolder:ModelItemHolder;
	var form:Form;
	var txt:TextField;
	var dialog:Dialog;
			
	function ok():Void {
		txt.commit();
		modelItem.setLabel( form.getValue("name") as String );
		dialog.close();
	}
	
	postinit {
		if( not ( FX.isInitialized( modelItem ) or FX.isInitialized( modelItemHolder ) ) )
			throw new RuntimeException( "Neither modelItem nor modelItemHolder are set!" );
		
		def typeName = if( FX.isInitialized( modelItemHolder ) ) modelItemHolder.getTypeName() else modelItem.getClass().getSimpleName();
		if( not FX.isInitialized( modelItem ) ) modelItem = modelItemHolder.modelItem;
		
		dialog = Dialog {
			title: "Rename {modelItem.getLabel()}"
			content: form = Form {
				formContent: txt = TextField { id:"name", label: "Name", value: modelItem.getLabel(), action:ok }
			}
			onOk: ok
		}
	}
}