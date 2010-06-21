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
*CloneProjectDialog.fx
*
*Created on 10, 2010, 15:11:25 em
*/

package com.eviware.loadui.fx.dialogs;

import javafx.scene.text.Text;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.HPos;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.fields.*;
import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.ui.form.FormField;

import javax.swing.JFileChooser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.api.model.ProjectRef;
import java.lang.StringBuffer;
import java.lang.Character;
import java.io.BufferedWriter;

import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.dialogs.CloneProjectDialog" );

public class CloneProjectDialog {
	/**
	 * The ProjectRef to clone.
	 *
	 * @author predrag
	 */
	public-init var projectRef:ProjectRef;
	
	var form:Form;
	
	postinit {
		if( not FX.isInitialized( projectRef ) )
			throw new RuntimeException( "projectRef must not be null!" );
		
		def dialog:Dialog = Dialog {
			title: "Clone project: {projectRef.getLabel()}"
			content: [
				form = Form {
					width: bind 260
					formContent: [
						TextField { id: "name", label: "Name of cloned project", description: "Name of cloned project" } as FormField,
						FileInputField { id: "file", label: "File of cloned project", description: "File of cloned project" } as FormField,
						CheckBoxField { id: "open", label: "Open cloned project for editing", value: true, translateY: 20 }
					]
				}
			]
			okText: "Clone"
			onOk: function() {
				var clone: ProjectRef = cloneProject(projectRef);
				if(form.getField('open').value as Boolean){
					clone.setEnabled(true);
					AppState.instance.setActiveCanvas( clone.getProject() );
				}
				dialog.close();
			}
			
			width : 300
			height : 150
		}
		
		setDefaults(projectRef);
	}
	
	function setDefaults(pRef: ProjectRef): Void {
		var f: File = pRef.getProjectFile();
		
		var name: String = f.getName();
		var path: String = f.getAbsolutePath();
		path = path.replaceAll(name, "");
    			
    	var c: Integer = 1;
    	var cloneFile: File;
    	while((cloneFile = new File("{path}copy-{c}-of-{name}")).exists()){
    		c++;
    	}
	
		form.getField('name').value = "Copy {c} of {pRef.getLabel()}";
		form.getField('file').value = cloneFile;
		
	}
	
	function cloneProject(pRef: ProjectRef): ProjectRef {
		var f: File = pRef.getProjectFile();
    	var cloneFile: File = form.getField('file').value as File;
    	
    	var input: FileReader = new FileReader(f);
        var out: FileWriter = new FileWriter(cloneFile);
        
        var sb: StringBuffer = new StringBuffer();
        
        var line: Integer;
        while ((line = input.read()) != -1) {
        	sb.append(Character.toChars(line));
        }
        input.close();
        
        var content: String = sb.toString();
        content = content.replaceFirst("label=\"{pRef.getLabel()}\"", "label=\"{form.getField('name').value}\"");
		
		var bw: BufferedWriter = new BufferedWriter(out);
		bw.write(content, 0, content.length());
		
		bw.close();
        out.close();
				
		var clone: ProjectRef = MainWindow.instance.workspace.importProject(cloneFile, true);
		clone.setEnabled(false);
		
		return clone;
    }
}
