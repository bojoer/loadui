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
*ExitConfirmDialogWorkspace.fx
*
*Created on May 31, 2010, 16:55:31 PM
*/

package com.eviware.loadui.fx.ui.dialogs;

import javafx.scene.text.Text;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.ui.form.fields.*;
import com.eviware.loadui.fx.dialogs.*;
import com.eviware.loadui.fx.WindowControllerImpl;
import com.eviware.loadui.fx.statistics.StatisticsWindow;

public class ExitConfirmDialogWorkspace {
    
    public-init var wc:WindowControllerImpl;
    
    postinit {
    	
    	  wc.stage.iconified = false;
		  wc.stage.toFront();
        
        def dialog:Dialog = Dialog {
            title: "Exit confirm!"
            content: [
            Text { content: "Are you sure you want to exit loadUI?" },
            ]
            okText: "Yes"
            cancelText: "No"
            onOk: function() {
                dialog.close();
                StatisticsWindow.getInstance().close();
                wc.forceClose();
            }
        }
    }
};
