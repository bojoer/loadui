/* 
 * Copyright 2011 SmartBear Software
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
package com.eviware.loadui.fx.legacy;

import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.FxUtils;

import javafx.scene.paint.Color;

public function createInstance() { SingleOutputWireEnforcer {} };

public class SingleOutputWireEnforcer {
	def task = TestExecutionVerifier {};
	def listener = CollectionListener {};
	
	def canvas = bind MainWindow.instance.canvas on replace {
		FX.deferAction( function():Void { validate() } );
	}
	def projectCanvas = bind MainWindow.instance.projectCanvas;
	def project = bind projectCanvas.projectItem on replace oldProject {
		if( oldProject != null ) {
			for( canvasItem in [ oldProject, for( scene in oldProject.getScenes() ) scene ] ) {
				canvasItem.removeEventListener( CollectionEvent.class, listener );
			}
		}
		if( project != null ) {
			for( canvasItem in [ project, for( scene in project.getScenes() ) scene ] ) {
				canvasItem.addEventListener( CollectionEvent.class, listener );
			}
			FX.deferAction( function():Void { validate() } );
		}
	}
	
	init {
		BeanInjector.getBean( TestRunner.class ).registerTask( task, Phase.PRE_START );
	}
	
	function validate():Boolean {
		var valid = true;
		
		if( project != null ) {
			for( canvasItem in [ project, for( scene in project.getScenes() ) scene ] ) {
				for( component in canvasItem.getComponents() ) {
					for( outputTerminal in component.getTerminals()[x|x instanceof OutputTerminal] ) {
						def invalid = outputTerminal.getConnections().size() > 1;
						valid = valid and not invalid;
						for( connection in outputTerminal.getConnections() ) {
							def connNode = canvas.lookupConnectionNode( connection );
							if( invalid ) {
								connNode.baseCableColor = Color.RED
							} else if( connNode.baseCableColor == Color.RED ) {
								connNode.baseCableColor = Color.GRAY
							}
						}
					}
				}
			}
		}
		
		return valid;
	}
}

class TestExecutionVerifier extends TestExecutionTask {
	override function invoke( execution, phase ) {
		if( not validate() ) execution.abort( "Your test contains multiple wires connected to the same output terminal! Since loadUI 2.0 this is not allowed. Please, fix this before running the test." );
	}
}

class CollectionListener extends WeakEventHandler {
	override function handleEvent( e ) {
		def cEvent = e as CollectionEvent;
		if( ProjectItem.SCENES.equals( cEvent.getKey() ) ) {
			def scene = cEvent.getElement() as SceneItem;
			if( cEvent.getEvent() == CollectionEvent.Event.ADDED ) {
				scene.addEventListener( CollectionEvent.class, this );
			} else {
				scene.removeEventListener( CollectionEvent.class, this );
			}
		} else if( ProjectItem.CONNECTIONS.equals( cEvent.getKey() ) ) {
			FxUtils.runInFxThread( function():Void { FX.deferAction( function():Void { validate() } ) } );
		}
	}
}