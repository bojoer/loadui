/*
*Deletable.fx
*
*Created on jul 23, 2010, 11:20:12 fm
*/

package com.eviware.loadui.fx.ui.node;

import com.eviware.loadui.fx.dialogs.DeleteDeletablesDialog;
import javafx.scene.Scene;
import javafx.scene.Node;

public function deleteObjects( deletables:Deletable[], onOk: function():Void ):Void {
	if( sizeof deletables[d|d.confirmDelete] > 0 ) {
		println("scene: {(deletables[0] as Node).scene}");
		DeleteDeletablesDialog { deletables: deletables, onOk: onOk, hostScene: (deletables[0] as Node).scene };
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
		if( not FX.isInitialized( confirmDialogScene ) )
			confirmDialogScene = (this as Node).scene;
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