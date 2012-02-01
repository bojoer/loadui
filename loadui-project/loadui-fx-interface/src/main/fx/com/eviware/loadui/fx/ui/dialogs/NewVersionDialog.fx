/*
*NewVersionDialog.fx
*
*Created on jan 19, 2012, 13:48:00 em
*/

package com.eviware.loadui.fx.ui.dialogs;

import javafx.ext.swing.SwingComponent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Stack;
import javafx.scene.text.Text;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.dialogs.*;
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.ui.WindowController;
import com.eviware.loadui.fx.statistics.StatisticsWindow;
import com.eviware.loadui.util.NewVersionChecker;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

public class NewVersionDialog {

	public-init var newVersion:NewVersionChecker.VersionInfo;
	def component = createReleaseNotesPane();
	
	// This is the way that you have to wrap Swing Components in JavaFx to make them auto-resize nicely.
	def stack:Stack = Stack {
		override var width on replace {
			component.setPreferredSize( new Dimension( getWidth(), getHeight() ) ); 
		}
		
		override var height on replace {
			component.setPreferredSize( new Dimension( getWidth(), getHeight() ) );
		}
		content: SwingComponent.wrap( component ) 
	}
	
	function getHeight()
	{
		return 380;
	}
	
	function getWidth()
	{
		return 500;
	}
	
   postinit {
   	def dialog:Dialog = Dialog {
	         title : "New version notice"
	         content: [
	         	Text { content: "There is a newer version of loadUI available.\n" },
	         	stack
	         ]
	         extraButtons: [
	         	Button {
						text: "Ignore This Update"
						action: function() {
							newVersion.skipThisVersion();
							dialog.close();
						}
					}]
	         okText: "Download latest version"
	         cancelText: "Remind Me Later"
	         onOk: function() {
					dialog.close();
					FxUtils.openURL( newVersion.downloadUrl );
	         }
				onCancel: function() {
					dialog.close();
	         }
         }
    }
    
    function createReleaseNotesPane()
    {
    	def text:JEditorPane = new JEditorPane();
		try {
			text.setPage( newVersion.releaseNotes );
			text.setEditable( false );
		} catch( e ) {
			text.setText( "Sorry! No Release notes currently available." );
		}
		return new JScrollPane( text );
    }

};
