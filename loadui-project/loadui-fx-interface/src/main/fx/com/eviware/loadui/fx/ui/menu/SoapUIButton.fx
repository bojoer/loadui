 /* 
 * Copyright 2011 eviware software ab
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

package com.eviware.loadui.fx.ui.menu;

import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image; 
import javafx.scene.text.*;

import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.popup.*;
import com.eviware.loadui.fx.ui.dialogs.*;
import com.eviware.loadui.fx.ui.menu.button.*;

import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.util.soapui.SoapUIStarter;
import javafx.scene.input.MouseEvent;

import java.io.File;
import javafx.scene.control.Tooltip;

public var instance:SoapUIButton;

public class SoapUIButton extends Group {
		
	  var tooltip:Tooltip;
     public var image:ImageView;
     
	  override var content = bind [image, tooltip ];
	   
     init {
      instance = this;
      
      tooltip = Tooltip{
		    text: "Launch soapUI"
		}
      
		image = ImageView {
		      blocksMouse: true
		      onMouseEntered: function (e:MouseEvent): Void {
                 tooltip.activate();
        		}
	         onMouseExited: function (e:MouseEvent): Void {
	            tooltip.deactivate();
	         }
				image: Image { url: "{__ROOT__}images/png/soapui-logo-small.png" }
				opacity: if ( MainWindow.instance.workspace.getProperty(WorkspaceItem.SOAPUI_PATH_PROPERTY).getValue() == null ) 0.5 else 1
				onMouseReleased: function(e) {
					def dialog:Dialog = Dialog {
				    	title: "Launch soapUI"
				    	content: Text {
				    		content: "Launch soapUI?"
				    	}
				    	okText: "Ok"
				    	onOk: function() {
				    		var workspace: WorkspaceItem = MainWindow.instance.workspace;
				    		def soapUIHome = workspace.getProperty(WorkspaceItem.SOAPUI_PATH_PROPERTY);
				    		if( soapUIHome.getValue() == null ) {
				    			def warning:Dialog = Dialog {
				    				title: "Warning!"
							    	content: Text {
							    		content: "You need to set a soapUI home first! Do it now or later?"
							    	}
							    	okText: "Now"
							    	onOk: function() {
							    		warning.close();
							    	    WorkspaceWrenchDialog{}.show();
							    	}
							    	cancelText: "Later"
							    }
				    		} else {
				    			SoapUIStarter.start(( soapUIHome.getValue() as File).getAbsolutePath());
				    		}
			    		    dialog.close();
				    	}
				    }
				}
			}
	 }
};
