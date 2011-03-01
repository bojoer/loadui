// 
// Copyright 2011 eviware software ab
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
 * Tabulates incoming messages and creates a csv output 
 * 
 * @help http://www.loadui.org/Output/table-log-component.html
 * @name Table Log
 * @category output
 * @dependency net.sf.opencsv:opencsv:2.0
 * @nonBlocking true
 */

import com.eviware.loadui.api.ui.table.LTableModel
import com.eviware.loadui.api.events.PropertyEvent
import au.com.bytecode.opencsv.CSVWriter
import java.io.FileWriter
import java.io.FileOutputStream
import java.io.FileInputStream
import com.eviware.loadui.api.events.ActionEvent
import com.eviware.loadui.util.layout.DelayedFormattedString
import javax.swing.event.TableModelListener
import javax.swing.event.TableModelEvent
import java.text.SimpleDateFormat

import com.eviware.loadui.api.summary.MutableSection

createProperty 'maxRows', Long, 1000
createProperty 'logFilePath', String
createProperty 'saveFile', Boolean, false
createProperty 'follow', Boolean, false
createProperty 'summaryRows', Long, 0
createProperty 'appendSaveFile', Boolean, false
createProperty 'formatTimestamps', Boolean, true

myTableModel = new LTableModel(1000, follow.value as Boolean)
myTableModel.addTableModelListener(new TableModelListener() {
	public void tableChanged(TableModelEvent e){
		updateFollow()
	}
});

String saveFileName = null

writer = null
def formater = new SimpleDateFormat("HH:mm:ss:SSS")
myTableModel.maxRow = maxRows.value

updateFollow = {
	follow.value = myTableModel.follow
}

rowsDisplay = new DelayedFormattedString( '%d', 500, value { myTableModel.rowCount } )
fileDisplay = new DelayedFormattedString( '%s', 500, value { saveFileName ?: '-' } )

output = { message ->
	message.keySet().each { k -> myTableModel.addColumn k }
	lastMsgDate = new Date();
	
	if ( formatTimestamps.value )
		message.each() { key, value ->
			if ( key.toLowerCase().indexOf("timestamp") > -1 ) 
				message.put(key, formater.format(new Date(value)) )
		}

	result = myTableModel.addRow(message) 
	if( result && saveFile.value && saveFileName != null) {
		if( writer == null ){
			writer = new CSVWriter(new FileWriter(saveFileName, appendSaveFile.value), (char) ',');
		}
		try {
			String[] entries = myTableModel.lastRow
			writer.writeNext(entries)
			writer.flush()
		} catch (Exception e) {
			e.printStackTrace()
		}
	}
}

onRelease = {
	rowsDisplay.release()
	fileDisplay.release()
}

addEventListener( PropertyEvent ) { event ->
	if( event.event == PropertyEvent.Event.VALUE ) {
		if( event.property.key == 'maxRows' ) {
			myTableModel.maxRow = maxRows.value
		}
		else if( event.property.key == 'follow' && myTableModel.follow != follow.value as Boolean) {
			myTableModel.follow = follow.value
		} 
	}
}

addEventListener( ActionEvent ) { event ->
	if ( event.key == "COMPLETE" ) {
		writer?.close()
		writer = null
	}
	else if ( event.key == "START" ) {
		buildFileName()
	}
	else if ( event.key == "RESET" ) {
		myTableModel.reset()
		buildFileName()
	}
}

buildFileName = {
	if( !saveFile.value ) {
		writer?.close()
		writer = null
		return
	}
	if( writer != null ) {
		return
	}
	def filePath = "${getBaseLogDir()}${File.separator}${logFilePath.value}"
	if( !validateLogFilePath(filePath) ){
		filePath = "${getBaseLogDir()}${File.separator}logs${File.separator}table-log${File.separator}${getDefaultLogFileName()}"
		log.warn("Log file path wasn't specified properly. Try default path: [$filePath]")
		if( !validateLogFilePath(filePath) ){
			log.error("Path: [$filePath] can't be used either. Table log component name contains invalid characters. Log file won't be saved.")
			saveFileName = null
			return
		}
	}
	if( !appendSaveFile.value ){
		def f = new File(filePath)
		filePath = "${f.parent}${File.separator}${addTimestampToFileName( f.name )}"
	}
	new File(filePath).parentFile.mkdirs()
	saveFileName = filePath
}

getBaseLogDir = {
	def dir = System.getProperty("loadui.home")
	if(dir == null || dir.trim().length() == 0){
		dir = "."
	}
	return dir			
}
				
getDefaultLogFileName = {
	return getLabel().replaceAll(" ","")
}
				
validateLogFilePath = { filePath ->
	try {
		// the only good way to check if file path 
		// is correct is to try read and writing
		File temp = new File(filePath)
		temp.parentFile.mkdirs()
		if(!temp.exists()){
			FileOutputStream fos = new FileOutputStream(temp)
			fos.write([0])
			fos.close()
			temp.delete()
		}
		else{
			FileInputStream fis = new FileInputStream(temp)
			fis.read()
			fis.close()
		}
		return true
	}
	catch(Exception e){
		return false
	}	
}

addTimestampToFileName  = {name ->
	def ext = ""
	def ind = name.lastIndexOf(".")
	if( ind > -1 ){
		ext = name.substring(ind, name.length())
		name = name.substring(0, ind)
	}
	def timestamp = new Date().time
	if(name.length() > 0){
		name = "${name}-"
	}
	return "$name$timestamp$ext"
}

layout { 
	node( widget:'tableWidget', model:myTableModel ) 
}

compactLayout {
	box( widget:'display' ) {
		node( label: 'Rows', fString:rowsDisplay )
		node( label: 'Output File', fString:fileDisplay )
	}
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
		property(property: logFilePath, label: 'Log File (Comma separated, relative to loadUI home dir)' )
		property(property: appendSaveFile, label: 'Check to append selected file', )
		property(property: formatTimestamps, label: 'Check to format timestamps(hh:mm:ss:ms)')
		label('(If not appending file, its name will be used to generate new log files each time test is run.)')
	}
}

generateSummary = { chapter ->
	if (summaryRows.value > 0) {
   		MutableSection sect = chapter.addSection(getLabel())
   		sect.addTable(getLabel(), myTableModel.getLastRows(summaryRows.value))
   	}
}