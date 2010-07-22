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
*ProjectNode.fx
*
*Created on feb 10, 2010, 11:47:11 fm
*/

package com.eviware.loadui.fx.widgets;

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.VPos;
import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.BaseNode.*;
import com.eviware.loadui.fx.ui.popup.Menu;
import com.eviware.loadui.fx.ui.popup.PopupMenu;
import com.eviware.loadui.fx.ui.popup.MenuItem;
import com.eviware.loadui.fx.ui.popup.ActionMenuItem;
import com.eviware.loadui.fx.ui.resources.TitlebarPanel;
import com.eviware.loadui.fx.ui.resources.MenuArrow;
import com.eviware.loadui.fx.dialogs.DeleteProjectDialog;
import com.eviware.loadui.fx.dialogs.CloneProjectDialog;
import com.eviware.loadui.fx.dialogs.CorruptProjectDialog;
import com.eviware.loadui.fx.ui.popup.SeparatorMenuItem;
import com.eviware.loadui.fx.widgets.canvas.ProjectCanvas;

import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import javafx.util.Math;

import java.util.EventObject;
import java.io.File;
import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import com.eviware.loadui.api.component.BehaviorProvider.ComponentCreationException;

import javax.imageio.ImageIO;
import javafx.ext.swing.SwingUtils;
import com.eviware.loadui.fx.util.ImageUtil.*;

import com.eviware.loadui.fx.async.ProjectRefSetEnabledTaskFX;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.ProjectNode" );

def projectGrid = Image { url:"{__ROOT__}images/project-grid.png" };

/**
 * Node to display in the ProjectList representing a ProjectRef.
 */
public class ProjectNode extends BaseNode, Draggable, EventHandler {
	
	var label:String;
	
	/**
	 * True if the ProjectRef is enabled, false if not.
	 */
	public-read var enabled:Boolean;
	
	/**
	 * The ProjectRef to represent.
	 */
	public-init var projectRef:ProjectRef on replace {
		projectRef.addEventListener( BaseEvent.class, this );
		enabled = projectRef.isEnabled();
		label = projectRef.getLabel() ;
	}
	
	def modelItem = bind lazy projectRef.getProject();
	
	postinit {
		if( not FX.isInitialized( projectRef ) )
			throw new RuntimeException( "projectRef must not be null!" );
		
		enabled = projectRef.isEnabled();
		label = projectRef.getLabel();
		
		addMouseHandler( MOUSE_CLICKED, function( e:MouseEvent ) {
			if( e.button == MouseButton.PRIMARY and e.clickCount == 2 ) {
				MainWindow.instance.waitingCursor.startWait("Openning Project");

				
			    	try {

						var task = ProjectRefSetEnabledTaskFX { ref:projectRef 
						onDone: function() {
							AppState.instance.setActiveCanvas( projectRef.getProject() );
							MainWindow.instance.waitingCursor.stopWait();	
						}
						
						}
						task.start();

			   	 	}
			    	catch( ex:IOException )
			    	{
			    	    
			    		MainWindow.instance.waitingCursor.stopWait();
			    		CorruptProjectDialog{ project:projectRef };
			    	}
			}
		} );
	}	

	
	var menu:PopupMenu;
	var menuContent:Node;
	
	def enabledMenu:MenuItem[] = [
		ActionMenuItem {
			text: ##[OPEN]"Open"
			action: function() {
				AppState.instance.setActiveCanvas( projectRef.getProject() );
			}
		}, ActionMenuItem {
			text: ##[DISABLE]"Disable"
			action: function() {
				if( MainWindow.instance.projectCanvas.canvasItem == projectRef.getProject() )
					MainWindow.instance.projectCanvas.canvasItem = null;
				projectRef.setEnabled( false );
			}
		}, ActionMenuItem {
			text: ##[SAVE]"Save"
			action: function() {
				projectRef.getProject().save();
			}
		}, ActionMenuItem {
			text: ##[DELETE]"Delete"
			action: function() { DeleteProjectDialog { projectRef: projectRef } }
		}
	];
	
	def disabledMenu:MenuItem[] = [
		//ActionMenuItem {
		//	text: ##[ENABLE]"Enable"
		//	action: function() {
		//		projectRef.setEnabled( true );
		//	}
		//
		ActionMenuItem {
			text: ##[OPEN]"Open"
			action: function() {
			    try {
					projectRef.setEnabled( true );
					AppState.instance.setActiveCanvas( projectRef.getProject() );
			     }
			    catch( e:IOException )
			    {
			    	CorruptProjectDialog{ project:projectRef };
			    }
			}
		} 
		SeparatorMenuItem{}
		ActionMenuItem {
			text: ##[DELETE]"Clone"
			action: function() { CloneProjectDialog { projectRef: projectRef } }
		}
		ActionMenuItem {
			text: ##[DELETE]"Delete"
			action: function() { DeleteProjectDialog { projectRef: projectRef } }
		}
	];
	
	var width: Number = 155;
	var height: Number = 100;
	
	var miniature: Image; 
	function refreshMiniature(){
		var base64: String = projectRef.getAttribute("miniature", "");
		if(base64.length() > 0){
			miniature = base64ToFXImage(base64);
		}
	}
	
	override function create() {
		refreshMiniature();
		TitlebarPanel {
			titlebarContent: BasicTitlebarMenuContent {
				width: 155
				hasLed: false
				label: bind "{label}"
				menuItems: bind if( enabled ) enabledMenu else disabledMenu
			}
			content: bind [
				Rectangle { fill: Color.web("#c9c9c9"), width: bind width, height: bind height },
				ImageView { image: projectGrid, x: 7, y: 7 },
				ImageView { image: miniature, x: 12, y: 12 }
			]
			opacity: bind if( dragging ) 0.8 else 1
		}
	}
	
	override function handleEvent( e:EventObject ) {
		def event = e as BaseEvent;
		if(event.getSource() == projectRef){
			if( event.getKey().equals( ProjectRef.LOADED ) ) {
				runInFxThread( function():Void { 
					enabled = true;
					projectRef.getProject().addEventListener( BaseEvent.class, this ); 
					label = projectRef.getLabel();
				});
			} else if( event.getKey().equals( ProjectRef.UNLOADED ) ) {
				runInFxThread( function():Void { 
					enabled = false;
					projectRef.getProject().removeEventListener( BaseEvent.class, this );
					refreshMiniature();
				});
			} else if( event.getKey().equals( ProjectRef.LABEL ) ) {
				runInFxThread( function():Void { label = projectRef.getLabel() } );
			}
		}
		else if(event.getSource() == projectRef.getProject()){
			if(event.getKey().equals(ModelItem.LABEL)) {
				runInFxThread( function():Void { 
					label = projectRef.getProject().getLabel();
				} );
			} 
		}
	}
	
	override function toString() { label }
	
}
