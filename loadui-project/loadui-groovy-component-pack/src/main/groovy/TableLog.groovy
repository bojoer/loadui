// 
// Copyright 2013 SmartBear Software
// 
// Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
// versions of the EUPL (the "Licence");
// You may not use this work except in compliance with the Licence.
// You may obtain a copy of the Licence at:
// 
// http://ec.europa.eu/idabc/eupl
// 
// Unless required by applicable law or agreed to in writing, software distributed under the Licence is
// distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
// express or implied. See the Licence for the specific language governing permissions and limitations
// under the Licence.
// 

/**
 * Tabulates incoming messages and creates a csv output 
 * 
 * @id com.eviware.TableLog
 * @help http://www.loadui.org/Output/table-log-component.html
 * @name Table Log
 * @category output
 * @dependency net.sf.opencsv:opencsv:2.3
 * @nonBlocking true
 */

import au.com.bytecode.opencsv.CSVWriter
import java.io.FileWriter
import java.io.FileOutputStream
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.concurrent.CopyOnWriteArraySet

import javafx.application.Platform
import javafx.stage.FileChooser
import javafx.scene.control.TableView
import javafx.scene.control.TableColumn
import javafx.util.Callback
import javafx.beans.value.ObservableValue
import javafx.beans.value.ChangeListener

inputTerminal.description = 'Messages sent here will be displayed in the table.'
likes( inputTerminal ) { true }

createProperty( 'maxRows', Long, 1000 )
createProperty( 'logFilePath', String )
createProperty( 'saveFile', Boolean, false )
createProperty( 'follow', Boolean, false )
createProperty( 'enabledInDistMode', Boolean, false )
createProperty( 'summaryRows', Long, 0 )
createProperty( 'appendSaveFile', Boolean, false )
createProperty( 'formatTimestamps', Boolean, true )
createProperty( 'addHeaders', Boolean, false )

table = null
cellFactory = { val -> { it -> val.value[val.tableColumn.text] } as ObservableValue } as Callback
rebuildTable = { table = new TableView( prefHeight: 200, minWidth: 500 ) }
tableColumns = [] as CopyOnWriteArraySet
def latestHeader
String saveFileName = null
writer = null
def format = new SimpleDateFormat( "HH:mm:ss:SSS" )

onMessage = { o, i, m ->
	if( controller && i == remoteTerminal ) {
		//controller received message from agent
		m["Source"] = o.label
		output( m )
	}
}

output = { message ->
	def writeLog = saveFile.value && saveFileName
	if( controller || writeLog ) {
		def addedColumns = message.keySet().findAll { tableColumns.add( it ) }
		
		if ( formatTimestamps.value ) {
			message.each() { key, value ->
				if ( key.toLowerCase().contains("timestamp") ) {
					try {
						message[key] = format.format( new Date( value ) )
					} catch ( IllegalArgumentException e ) {
						log.info( "Failed to format Timestamp in a column whose name hinted about it containing a Timestamp" )
					}
				}
			}
		}
		
		if( controller ) {
			Platform.runLater {
				addedColumns.each {
					def column = new TableColumn( cellValueFactory: cellFactory, text: it, sortable: false )
					column.widthProperty().addListener( { obs, oldVal, width -> setAttribute( "width_$it", "$width" ) } as ChangeListener )
					try {
						column.width = Double.parseDouble( getAttribute( "width_$it", null ) )
					} catch( e ) {
					}
					table.columns.add( column )
				}
				table.items.add( message )
				while( table.items.size() > maxRows.value ) table.items.remove(0)
			}
		}
		
		if( writeLog ) {
			if( !writer ) writer = new CSVWriter( new FileWriter( saveFileName, appendSaveFile.value ), (char) ',' )
			try {
				def header = tableColumns as String[]
				if( addHeaders.value && !Arrays.equals( latestHeader, header ) ) {
					writer.writeNext( header )
					latestHeader = header
				}
				entries = header.collect { message[it] ?: "" } as String[]
				writer.writeNext( entries )
			} catch ( Exception e ) {
				log.error( "Error writing to log file", e )
			}
		}
	}
	
	if( ! controller && enabledInDistMode.value ) {
		// on agent and enabled, so send message to controller
		send( controllerTerminal, message )
	}
}

onAction( "START" ) { buildFileName() }

onAction( "COMPLETE" ) {
	writer?.close()
	writer = null
}

onAction( "RESET" ) { buildFileName() }

onRelease = { writer?.close() }

buildFileName = {
	if( !saveFile.value ) {
		writer?.close()
		writer = null
		return
	}
	if( writer ) return

	def filePath = "${getBaseLogDir()}${File.separator}${logFilePath.value}"
	if( !validateLogFilePath( filePath ) ) {
		filePath = "${getBaseLogDir()}${File.separator}logs${File.separator}table-log${File.separator}${getDefaultLogFileName()}"
		log.warn( "Log file path wasn't specified properly. Try default path: [$filePath]" )
		if( !validateLogFilePath( filePath ) ) {
			log.error("Path: [$filePath] can't be used either. Table log component name contains invalid characters. Log file won't be saved.")
			saveFileName = null
			return
		}
	}
	if( !appendSaveFile.value ) {
		def f = new File( filePath )
		filePath = "${f.parent}${File.separator}${addTimestampToFileName( f.name )}"
	}
	new File( filePath ).parentFile.mkdirs()
	saveFileName = filePath
}

getBaseLogDir = { System.getProperty( 'loadui.home', '.' ) }
getDefaultLogFileName = { getLabel().replaceAll( ' ','' ) }
				
validateLogFilePath = { filePath ->
	try {
		// the only good way to check if file path 
		// is correct is to try read and writing
		def temp = new File( filePath )
		temp.parentFile.mkdirs()
		if( !temp.exists() ) {
			def fos = new FileOutputStream( temp )
			fos.write( [0] )
			fos.close()
			temp.delete()
		} else {
			def fis = new FileInputStream( temp )
			fis.read()
			fis.close()
		}
		return true
	} catch( e ) {
		return false
	}	
}

addTimestampToFileName = { it.replaceAll('^(.*?)(\\.\\w+)?$', '$1-'+System.currentTimeMillis()+'$2') }

refreshLayout = {
	rebuildTable()
	layout(layout: 'wrap 4') {
		node( component: table, constraints: 'span' )
		action( label: 'Reset', action: { table.items.clear() } )
		action( label: 'Clear', action: {
			tableColumns.clear()
			refreshLayout()
		} )
		action( label: 'Save', action: {
			def fileChooser = new FileChooser( title: 'Save log' )
			fileChooser.extensionFilters.add( new FileChooser.ExtensionFilter( 'CSV', '*.csv' ) )
			def saveFile = fileChooser.showSaveDialog( table.scene.window )
			if( saveFile ) {
				try {
					def writer = new CSVWriter( new FileWriter( saveFile, false ), (char) ',' )
					writer.writeNext( tableColumns as String[] )
					table.items.each { message -> writer.writeNext( tableColumns.collect { message[it] ?: "" } as String[] ) }
					writer.close()
				} catch ( e ) {
					log.error( 'Failed writing log to file!', e )
				}
			}
		} )
		property( property: enabledInDistMode, label: 'Enabled in distributed mode', constraints: 'aligny center, alignx right' )
	}
	compactLayout {
		box( widget: 'display' ) {
			node( label: 'Rows', content: { table.items.size() } )
			node( label: 'Output File', content: { saveFileName ?: '-' } )
		}
	}
}
if( controller ) refreshLayout()


settings( label: "General" ) {
	box {
		property( property: maxRows, label: 'Max Rows in Table' )
	}
	box {
		property( property: summaryRows, label: 'Max Rows in Summary' )
	}	
}

settings( label:'Logging' ) {
	box {
		property( property: saveFile, label: 'Save Logs?' )
		property( property: logFilePath, label: 'Log File (Comma separated, relative to loadUI home dir)' )
		property( property: appendSaveFile, label: 'Check to append selected file' )
		property( property: formatTimestamps, label: 'Check to format timestamps(hh:mm:ss:ms)' )
		property( property: addHeaders, label: 'Check to add headers to a file' )
		label( '(If not appending file, its name will be used to generate new log files each time test is run.)' )
	}
}

generateSummary = { chapter ->
	if( summaryRows.value > 0 ) {
		int nRows = summaryRows.value
		def rows = table.items.subList( table.items.size() - nRows, table.items.size() )
		def cols = tableColumns as List
		chapter.addSection( getLabel() ).addTable( getLabel(), new javax.swing.table.AbstractTableModel() {
			int getColumnCount() { cols.size() }
			int getRowCount() { nRows }
			String getColumnName( int c ) { cols[c] }
			Object getValueAt( int r, int c ) { rows[r][cols[c]] }
		} )
	}
}