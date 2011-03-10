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
package com.eviware.loadui.fx.statistics.chart.line;

import javafx.scene.layout.HBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.util.Sequences;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.FxUtils.*;

import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.LineChartView.LineSegment;
import java.beans.PropertyChangeEvent;

import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.util.statistics.ExecutionListenerAdapter;
import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.statistics.StatisticsWindow;
import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.api.statistics.Statistic;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.Comparable;
import java.util.EventObject;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.lang.StringBuffer;
import java.lang.System;
import javax.swing.JFileChooser;

def SEPARATOR = ",";
def NEW_LINE = System.getProperty("line.separator");

var lastSelectedFile: File;

/**
 * Panel for exporting the row data.
 *
 * @author predrag.vucetic
 */
public class RowDataPanel extends HBox {
   
   var statistics: Statistic[];
   
   public-init var segments: LineSegment[] on replace {
      var sortedSegments = Sequences.sort(for(s in segments) SegmentComparable{segment: s});
		statistics = for(s in sortedSegments) (s as SegmentComparable).segment.getStatistic(); 
   }
    
	override var styleClass = "row-data-panel";
	override var hpos = HPos.RIGHT;
	override var vpos = VPos.CENTER;
	override var nodeVPos = VPos.CENTER;
	override var padding = Insets { right: 5, top: 3 };
	
	override var spacing = 9;
	
	var destinationFile: File = null;
	
	var labelText: String;
	
	var executionManager: ExecutionManager = BeanInjector.getBean(ExecutionManager.class) on replace {
		executionManager.addExecutionListener( ExecutionManagerListener{} );
	}
	
	var testIsRunning: Boolean = false on replace {
	   labelText = if(testIsRunning){
	       "Data can't be exported while test is running.";
	   }
	   else{
	       "Export data to csv format:";
	   }
	}
	
	def confirmDialog: Dialog = Dialog {
		title: "File already exists"
		scene: AppState.byName("STATISTICS").scene
		content: [
			Text { content: "Selected file already exists. Overwrite?" },
		]
		okText: "Yes"
		cancelText: "No"
		onOk: function() {
			exportData(destinationFile);
			confirmDialog.close();
		}
		onCancel: function() {confirmDialog.close();}
		showPostInit: false
	}
					
	function exportData(file: File): Void {
	   if(file.exists()){
	       file.delete();
	   }
	   def e = StatisticsWindow.execution;
	   if( e == null ){
	       return;
	   }
	   
	   def fos: FileOutputStream = new FileOutputStream(file);
	   var length = e.getLength();
	   def compared = StatisticsWindow.comparedExecution;
	   if(compared != null and compared.getLength() > length){
	       length = compared.getLength();
	   }
	   
	   def dataSet: DataSet = DataSet{}; 
	   var start: Number = 0; 
	   def interval: Number = 3600 * 1000 / 2; //half an hour
	    
		while(start <= length){
		   if(start <= e.getLength()){ 
		   	retrieveData(start, start + interval, e, dataSet);
		   }
		   if(compared != null and start <= compared.getLength()){
		       retrieveData(start, start + interval, compared, dataSet);
		   }
		   start += interval + 1;
			dataSet.write(fos);
			fos.flush();
			dataSet.clear();
		}
		fos.close();
	} 
	
	function retrieveData(start: Number, end: Number, e: Execution, dataSet: DataSet){
		for(statistic in statistics){
		   def variable = statistic.getStatisticVariable();
		   def holder = variable.getStatisticHolder();
		   def source = statistic.getSource();
		   def points: java.lang.Iterable = statistic.getPeriod( start, end, 0, e);
      	for(p in points){
      	    def label = "{e.getLabel()}~{holder.getLabel()}~{source}~{variable.getName()}~{statistic.getName()}".replaceAll(" ", "_");
      	    dataSet.add((p as DataPoint).getTimestamp()/1000, label, (p as DataPoint).getValue());
      	} 
		}    
	}
	
	init {
		content = [
			Label { 
				text: bind labelText
			}
			Button{
			   text: "Export Raw data"
			   disable: bind testIsRunning
			   action: function(){
		        def chooser = new JFileChooser(lastSelectedFile);
              chooser.addChoosableFileFilter(new FileChooserFileFilter());
              chooser.setAcceptAllFileFilterUsed(false);
              if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(null)) {
                  destinationFile = chooser.getSelectedFile();
                  if( not destinationFile.getName().endsWith(".csv") ) {
                     def newname = "{destinationFile.getAbsolutePath()}.csv";
                     destinationFile = new File(newname); 
                  }
                  lastSelectedFile = destinationFile;
                  if(destinationFile != null and destinationFile.exists()){
                     confirmDialog.show();
                  }
                  else{
                  	exportData(destinationFile);
                  }
              }
			   }
			}
		];
	}
}

class DataSet {
   def columns: ArrayList = new ArrayList(); 
	def data: HashMap = new HashMap();
	
	var headerWritten: Boolean = false;
	
	function add(timestamp: Long, label: String, value: Object){
	    if(not columns.contains(label)){
	        columns.add(label);
	    }
	    if(data.get(timestamp) == null){
	        data.put(timestamp, DataRow{ timestamp: timestamp });
	    }
	    def index: Number = columns.indexOf(label);
	    def row: DataRow = data.get(timestamp) as DataRow; 
	    row.add(index, value);
	}
	
	function getHeader(): String {
       def sb: StringBuffer = new StringBuffer();
       sb.append("timestamp");
       for(c in columns){
         sb.append(SEPARATOR);
         sb.append(c);
       }
       sb.append(NEW_LINE);
       sb.toString().toUpperCase();
   }
   
	function write(os: OutputStream){
	    if(not headerWritten){
	       os.write(getHeader().getBytes("utf8")); 
	       headerWritten = true;
	    }
	    def dataRowList = new ArrayList( data.values() );
	    Collections.sort( dataRowList );
	    for(row in dataRowList){
	    	os.write((row as DataRow).toString().getBytes("utf8"));
	    }
	}
	
	function clear(){
	    data.clear();
	}    
}

class DataRow extends Comparable {
    public-init var timestamp: Long;

    var data = [];

    function add(index: Integer, value: Object){
        while(index >= sizeof data){
            insert "" into data;
        }
        data[index] = value;
    }
    
    override function compareTo( o: Object ): Integer {
		if( o == null )
			return 1;
		(timestamp - (o as DataRow).timestamp) as Integer;
	 }
    
    override function toString(): String {
       def sb: StringBuffer = new StringBuffer();
       sb.append(timestamp);
       for(value in data){
         sb.append(SEPARATOR);
         sb.append(value);
       }
       sb.append(NEW_LINE);
       sb.toString();
    }
}

class ExecutionManagerListener extends ExecutionListenerAdapter {
   override function executionStarted( state: ExecutionManager.State ) {
		FxUtils.runInFxThread( function():Void { testIsRunning = true; } );
   }
   override function executionStopped( state: ExecutionManager.State ) {
		FxUtils.runInFxThread( function():Void { testIsRunning = false; } );
   }
}

class  FileChooserFileFilter extends javax.swing.filechooser.FileFilter {
	override public function accept(f:File):Boolean {
		f.getName().toLowerCase().endsWith(".csv") or f.isDirectory()
	}
	 
	override public function getDescription():String {
		".csv files";
	}
}

class SegmentComparable extends Comparable {
	var segment: LineSegment;
	override function compareTo(other: Object): Integer {
	    def name = segment.getStatistic().getStatisticVariable().getStatisticHolder().getLabel();
	    def otherName = (other as SegmentComparable).segment.getStatistic().getStatisticVariable().getStatisticHolder().getLabel();  
	    return name.compareTo(otherName);
	}
}
