/*
*SystemPropertiesDialog.fx
*
*Created on sep 8, 2010, 14:38:42 em
*/

package com.eviware.loadui.fx.ui.dialogs;

import javafx.scene.layout.LayoutInfo;
import javafx.scene.control.TextBox;

import com.eviware.loadui.fx.ui.dialogs.Dialog;

import java.lang.System;
import java.lang.StringBuilder;
import java.util.Collections;
import java.util.ArrayList;

public class SystemPropertiesDialog {
	postinit {
		def propertyNames = new ArrayList( System.getProperties().keySet() );
		Collections.sort( propertyNames );
		def stringBuilder = new StringBuilder();
		for( name in propertyNames )
			stringBuilder.append( "{%-30s name} {System.getProperty(name as String)}\r\n" );
		
		def dialog:Dialog = Dialog {
			title: "System Properties"
			content: TextBox {
				text: stringBuilder.toString()
				multiline: true
				editable: false
				layoutInfo: LayoutInfo { height: 300, width: 400 }
				selectOnFocus: true
			}
			noCancel: true
			onOk: function() { dialog.close(); }
		}
	}
}