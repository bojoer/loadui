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
package com.eviware.loadui.fx.eventlog;

import javafx.scene.Node;
import javafx.scene.layout.Stack;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.ext.swing.SwingComponent;

import java.awt.Dimension;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import org.jdesktop.swingx.JXTable;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.statistics.StatisticsWindow;
import com.eviware.loadui.api.ui.inspector.Inspector;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEventManager;

public function createInstance( testEventManager:TestEventManager ):EventLogInspector {
	EventLogInspector { testEventManager: testEventManager }
}

public class EventLogInspector extends Inspector {
	def table = new JTable() on replace {
		table.setIntercellSpacing( new Dimension( 10, 0 ) );
		table.setAutoCreateColumnsFromModel( true );
		
		def header = table.getTableHeader();
		header.setReorderingAllowed( false );
		header.setResizingAllowed( false );
	}
	
	def scrollPane = new JScrollPane( table ) on replace {
		scrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
		scrollPane.setWheelScrollingEnabled( true );
	}
	
	def stack = Stack {
		override var width on replace {
			scrollPane.setPreferredSize( new Dimension( width, height ) );
		}
		
		override var height on replace {
			scrollPane.setPreferredSize( new Dimension( width, height ) );
		}
		content: SwingComponent.wrap( scrollPane )
	}
	
	def container = VBox {
		content: [
			Label { text: "No filtering applied" },
			HBox {
				content: [
					//TODO: Filtering goes here!
					stack
				]
			}
		]
	}
	
	def eventObserver = EventObserver {}
	
	public-init var testEventManager: TestEventManager on replace {
		testEventManager.registerObserver( eventObserver );
	}

	var model:EventLogTableModel;
	def execution = bind StatisticsWindow.execution on replace {
		table.setModel( model = EventLogTableModel.create( execution ) );
		def columnModel = table.getColumnModel();
		columnModel.getColumn( 0 ).setMaxWidth( 150 );
		columnModel.getColumn( 0 ).setPreferredWidth( 150 );
		columnModel.getColumn( 1 ).setMaxWidth( 150 );
		columnModel.getColumn( 1 ).setPreferredWidth( 150 );
		columnModel.getColumn( 2 ).setMaxWidth( 150 );
		columnModel.getColumn( 2 ).setPreferredWidth( 150 );
	}
	
	override function onShow() {
	}
	
	override function onHide() {
	}
	
	override function getPanel() {
		container
	}

	override function getName() {
		"Event Log"
	}
	
	override function getHelpUrl() {
		"http://www.loadui.org/";
	}
}

class EventObserver extends TestEventManager.TestEventObserver {
	override function onTestEvent( eventEntry:TestEvent.Entry ) {
		if( execution == StatisticsWindow.currentExecution ) {
			FxUtils.runInFxThread( function():Void {
				def scrollModel = scrollPane.getVerticalScrollBar().getModel();
				def bottom = scrollModel.getMaximum() - scrollModel.getExtent() == scrollModel.getValue();
				model.appendRow( eventEntry );
				if( bottom ) FX.deferAction( function():Void { scrollModel.setValue( scrollModel.getMaximum() - scrollModel.getExtent() ) } );
			} );
		}
	}
}