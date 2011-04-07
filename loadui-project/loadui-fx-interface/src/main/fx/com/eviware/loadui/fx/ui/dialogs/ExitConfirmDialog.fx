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
/*
*ExitConfirm.fx
*
*Created on May 28, 2010, 14:55:53 PM
*/

package com.eviware.loadui.fx.ui.dialogs;

import javafx.scene.text.Text;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.ui.form.fields.*;
import com.eviware.loadui.fx.dialogs.*;
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.api.ui.WindowController;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.fx.statistics.StatisticsWindow;

import java.lang.RuntimeException;

public class ExitConfirmDialog {
	
	public-init var wc:WindowController;
    
    postinit {
    	  wc.bringToFront();
        
        def dialog:Dialog = Dialog {
            title: "Exit confirm!"
            content: [
            	Text { content: "Do you want to exit loadUI?" },
            ]
            okText: "Yes"
            cancelText: "No"
            onOk: function() {
					def dialog2:Dialog = Dialog {
						title: "Save Project"
						content: [
							Text { content: "Save the Project?" },
						]
						okText: "Yes"
						cancelText: "No"
						onOk: function() {
							MainWindow.instance.projectCanvas.generateMiniatures();
							def project = MainWindow.instance.projectCanvas.canvasItem as ProjectItem;
							project.save();
							dialog2.close();
							StatisticsWindow.getInstance().close();
							wc.forceClose();
						}
						onCancel: function() {
							dialog2.close();
							StatisticsWindow.getInstance().close();
							wc.forceClose();
						}
					}
					dialog.close();
            }
        }
    }
};
