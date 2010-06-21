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

createProperty 'maxRows', Long, 1000
createProperty 'fileName', File 
createProperty 'saveFile', Boolean, false
createProperty 'follow', Boolean, false

myTableModel = new LTableModel(1000, follow.value as Boolean)
myTableModel.addTableModelListener(new TableModelListener() {
	public void tableChanged(TableModelEvent e){
		updateFollow()
	}
});

updateFollow = {
	follow.value = myTableModel.isFollow()
}

onMessage = { incoming, outgoing, message ->
	message.keySet().each { k -> myTableModel.addColumn k }
	
	result = myTableModel.addRow(message) 
	if( result && saveFile.value ) {
		try {
			char sep = ','
			writer = new CSVWriter(new FileWriter(fileName.value, true), sep);
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
settings( label: "General", constraints: 'wrap 2' ) {
	property(property: maxRows, label: 'What is maximum size of table(rows)?' )
	label( "Logging" )
	box(constraints:"growx, wrap 1") {
		property(property: saveFile, label: 'Should be output saved?' )
		property(property: fileName, label: 'Where should be output saved?' )
	}
} 
