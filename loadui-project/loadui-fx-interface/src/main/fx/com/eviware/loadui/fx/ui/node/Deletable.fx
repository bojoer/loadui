/*
*Deletable.fx
*
*Created on jul 23, 2010, 11:20:12 fm
*/

package com.eviware.loadui.fx.ui.node;

import com.eviware.loadui.fx.dialogs.DeleteDeletablesDialog;
import javafx.scene.Scene;

public function deleteObjects( deletables:Deletable[], onOk: function():Void ):Void {
	if( sizeof deletables[d|d.confirmDelete] > 0 ) {
		DeleteDeletablesDialog { deletables: deletables, onOk: onOk };
	} else {
		for( deletable in deletables ) {
			deletable.doDelete();
		}
		onOk();
	}
}

public function deleteObjects( deletables:Deletable[] ):Void {
	deleteObjects( deletables, null );
}

/**
 * Mixin class for objects which can be deleted.
 */
public mixin class Deletable {
	
	public var confirmDelete = true;
	public var confirmDialogScene:Scene;
	
	public function deleteObject():Void {
		println("DeleteObject. confirm: {confirmDelete}, deletable: {this}, hostScene:[confirmDialogScene]");
		if( confirmDelete ) {
			DeleteDeletablesDialog {
				deletables: [ this ]
				hostScene: confirmDialogScene
			};
		} else {
			doDelete();
		}
	}
	
	public abstract function doDelete():Void;
}