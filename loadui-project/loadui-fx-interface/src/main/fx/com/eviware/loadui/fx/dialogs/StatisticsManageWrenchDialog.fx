/*
*StatisticsManageWrenchDialog.fx
*
*Created on feb 14, 2011, 10:28:16 fm
*/

package com.eviware.loadui.fx.dialogs;

import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Panel;
import javafx.scene.layout.LayoutInfo;

import org.slf4j.LoggerFactory;
public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.ui.dialogs.StatisticsManageWrenchDialog" );

import com.eviware.loadui.fx.ui.tabs.*;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.form.fields.*;

import org.jfxtras.scene.layout.XMigLayout;
import org.jfxtras.scene.layout.XMigLayout.*;
import net.miginfocom.layout.*;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.statistics.StatisticsWindow;
import java.io.File;

/**
 * A File FormField.
 *
 * @author henrik.olsson
 */
public class StatisticsManageWrenchDialog  {
	
	public var title:String = "Workspace";
	var workspace: WorkspaceItem = MainWindow.instance.workspace;
	var formT1: Form;
	var dialogRef: Dialog;
	def currentProject:ProjectItem =  StatisticsWindow.getInstance().project;
	
	function ok():Void {
					workspace.getProperty(WorkspaceItem.STATISTIC_RESULTS_PATH).setValue(formT1.getField("resultsPath").value as File);
					currentProject.getProperty(ProjectItem.STATISTIC_NUMBER_OF_AUTOSAVES).setValue(formT1.getField("numberOfAutosaves").value as Long);
					dialogRef.close();
	         }
	         
	public function show() {

		def resultsPath:File = workspace.getProperty(WorkspaceItem.STATISTIC_RESULTS_PATH).getValue() as File;
		def numberOfAutosaves:Long = currentProject.getProperty(ProjectItem.STATISTIC_NUMBER_OF_AUTOSAVES).getValue() as Long;
		
		dialogRef = TabDialog {
			scene: StatisticsWindow.getInstance().scene
         title: title
         subtitle: "Settings"
         helpUrl: "http://www.loadui.org/TODO" 
         tabs: [
				Tab {
         			label: "General", content: formT1 = Form {
						formContent: [
							FileInputField { id: "resultsPath", label: "Results path (affects all projects)", description: "Where to store Results in the file system. This affects all projects.", value: resultsPath, selectMode: FileInputField.DIRECTORIES_ONLY},
							LongInputField { id: "numberOfAutosaves", label: "Number of results to autosave", description: "Number of Results to save automatically", value: numberOfAutosaves },
						]
					}
				}
			]
         onOk: ok
		}
	}
}
