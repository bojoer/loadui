/*
*Deletable.fx
*
*Created on jul 23, 2010, 11:20:12 fm
*/

package com.eviware.loadui.fx.ui.node;

import com.eviware.loadui.fx.dialogs.DeleteDeletablesDialog;

public function deleteObjects( deletables:Deletable[] ):Void {
	if( sizeof deletables[d|d.confirmDelete] > 0 ) {
		DeleteDeletablesDialog { deletables: deletables };
	} else {
		for( deletable in deletables ) {
			deletable.doDelete();
		}
	}
}

/**
 * Mixin class for objects which can be deleted.
 */
public mixin class Deletable {
	
	public var confirmDelete = true;
	
	public function deleteObject():Void {
		if( confirmDelete ) {
			DeleteDeletablesDialog { deletables: [ this ] };
		} else {
			doDelete();
		}
	}
	
	public abstract function doDelete():Void;
}