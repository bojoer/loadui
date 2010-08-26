// 
// Copyright 2010 eviware software ab
// 
// Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
// versions of the EUPL (the "Licence");
// You may not use this work except in compliance with the Licence.
// You may obtain a copy of the Licence at:
// 
// http://ec.europa.eu/idabc/eupl5
// 
// Unless required by applicable law or agreed to in writing, software distributed under the Licence is
// distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
// express or implied. See the Licence for the specific language governing permissions and limitations
// under the Licence.
// 

/**
 * Component that shows incoming messages in table format. Size of table row numbers can be changed.
 * Whole output is saved in file in csv format. 
 * 
 * @help http://www.loadui.org/Output/table-log.html
 * @name Table Log
 * @category output
 * @dependency net.sf.opencsv:opencsv:2.0
 * @nonBlocking true
 */

import com.eviware.loadui.api.ui.table.LTableModel
import com.eviware.loadui.api.events.PropertyEvent
import au.com.bytecode.opencsv.CSVWriter
import java.io.FileWriter
import com.eviware.loadui.api.events.ActionEvent
import javax.swing.event.TableModelListener
import javax.swing.event.TableModelEvent

import com.eviware.loadui.api.summary.MutableSection

createProperty 'maxRows', Long, 1000
createProperty 'fileName', File 
createProperty 'saveFile', Boolean, false
createProperty 'follow', Boolean, false
createProperty 'summaryRows', Long, 0
createProperty 'appendSaveFile', Boolean, false

myTableModel = new LTableModel(1000, follow.value as Boolean)
myTableModel.addTableModelListener(new TableModelListener() {
	public void tableChanged(TableModelEvent e){
		updateFollow()
	}
});

saveFileName = (fileName?.value as File)?.getName()

updateFollow = {
	follow.value = myTableModel.isFollow()
}


onMessage = { incoming, outgoing, message ->
	super.onTerminalMessage(incoming, outgoing, message)
	message.keySet().each { k -> myTableModel.addColumn k }
	lastMsgDate = new Date();
	
	result = myTableModel.addRow(message) 
	if( result && saveFile.value ) {
		try {
			char sep = ','
			
			writer = new CSVWriter(new FileWriter(saveFileName, true), sep);
			String[] entries = myTableModel.getLastRow()
			writer.writeNext(entries)
			writer.flush()
		} catch (Exception e) {
			println(e.printStackTrace())
		} finally {
			writer.close()
		}
	}
}

addEventListener( PropertyEvent ) { event ->
	if( event.event == PropertyEvent.Event.VALUE ) {
		if( event.property.key == 'maxRows' ) {
			myTableModel.setMaxRow(maxRows.value as Integer)
		}
		else if( event.property.key == 'follow' && myTableModel.isFollow() != follow.value as Boolean) {
			myTableModel.setFollow(follow.value as Boolean)
		}
	}
}

addEventListener( ActionEvent ) { event ->
	if ( event.key == "START" ) {
		if( appendSaveFile.value )
			saveFileName = (fileName.value as File).getName()
		else {
			saveFileName = (fileName.value as File).getName()
			def ext = saveFileName.substring(saveFileName.lastIndexOf("."), saveFileName.length())
			def name = saveFileName.substring(0, saveFileName.lastIndexOf("."))
			def timestamp = new Date().getTime()
			saveFileName = "${(fileName.value as File).getParent()}${File.separator}$name-$timestamp$ext"
			println saveFileName
		}
	}

	if ( event.key == "RESET" ) {
		myTableModel.reset()
	}
}
// layout
layout 
{ 
	node( widget:"tableWidget", model:myTableModel ) 
}

// settings
settings( label: "General" ) {
	box {
		property(property: maxRows, label: 'Max Rows in Table' )
	}
	box {
		property(property: summaryRows, label: 'Max Rows in Summary' )
	}
	
}

settings(label:'Logging') {
	box {
		property(property: saveFile, label: 'Save Logs?' )
		property(property: fileName, label: 'Log File (Comma Separated) ' )
		label('If not appending file, its name will be used to generate new log files each time test is run.')
		property(property: appendSaveFile, label: 'Check to append selected file', )
	}
}

generateSummary = { chapter ->
	if (summaryRows.value > 0) {
   		MutableSection sect = chapter.addSection(getLabel())
   		sect.addTable(getLabel(), myTableModel.getLastRows(summaryRows.value))
   	}
}
