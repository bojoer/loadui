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
 * Displays statistical information  
 * 
 * @help http://www.loadui.org/Analysis/statistics.html
 * @category analysis
 * @nonBlocking true
 */

import java.util.Calendar
import java.util.Date
import java.util.List
import java.util.HashMap
import java.util.Map
import java.util.ArrayList

import com.eviware.loadui.api.component.ComponentContext

import com.eviware.loadui.api.model.CanvasItem
import com.eviware.loadui.api.model.ProjectItem
import com.eviware.loadui.api.chart.CustomTimeRange
import com.eviware.loadui.api.chart.CustomNumericRange
import com.eviware.loadui.api.chart.ChartModel
import com.eviware.loadui.api.chart.ChartAdapter

import com.eviware.loadui.api.terminal.InputTerminal
import com.eviware.loadui.api.terminal.OutputTerminal
import com.eviware.loadui.api.terminal.TerminalMessage

import com.eviware.loadui.api.layout.OptionsProvider
import com.eviware.loadui.impl.layout.OptionsProviderImpl

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import com.eviware.loadui.api.events.PropertyEvent
import com.eviware.loadui.api.events.BaseEvent
import com.eviware.loadui.api.events.CollectionEvent
import com.eviware.loadui.api.events.ActionEvent

import com.eviware.loadui.util.statistics.ValueStatistics
import com.eviware.loadui.api.ui.table.LTableModel
import com.eviware.loadui.api.summary.MutableSection

import com.eviware.loadui.util.layout.DelayedFormattedString
import com.eviware.loadui.api.chart.ChartSerie
import com.eviware.loadui.api.chart.Point

AGGREGATE = "Aggregate"
AGENT_DATA_TIMESTAMP = "AgentDataTimestamp"
AGENT_DATA_TTL = 5000

executor = Executors.newSingleThreadScheduledExecutor()

createOutput( 'output', 'Statistics Output')
statisticsInput = createInput( 'statistics', 'Runner Statistics')

//Properties
createProperty( 'period', Long, 1 )
createProperty( 'rate', Long, 500 )
createProperty( 'chartPeriod', Long, 1 )

createProperty( 'enableAverage', Boolean, true )
createProperty( 'enableMin', Boolean, true )
createProperty( 'enableMax', Boolean, true )
createProperty( 'enableStdDev', Boolean, true )
createProperty( 'enableTPS', Boolean, true )
createProperty( 'enableBPS', Boolean, true )
createProperty( 'enableAvgTPS', Boolean, true )
createProperty( 'enableAvgBPS', Boolean, true )
createProperty( 'enablePercentile', Boolean, true )
createProperty( 'enableAvgResponseSize', Boolean, true )
createProperty( 'enableRequests', Boolean, true )
createProperty( 'enableRunning', Boolean, true )
createProperty( 'enableCompleted', Boolean, true )
createProperty( 'enableQueued', Boolean, true )
createProperty( 'enableDiscarded', Boolean, true )
createProperty( 'enableFailed', Boolean, true )
createProperty( 'currentSourceID', String, "none" )
createProperty( 'addtoSummary', Boolean, false )

avgDisplay = new DelayedFormattedString( '%.2f', 500, 0f )
minDisplay = new DelayedFormattedString( '%.0f', 500, 0f )
maxDisplay = new DelayedFormattedString( '%.0f', 500, 0f )
stdDevDisplay = new DelayedFormattedString( '%.2f', 500, 0f )
tpsDisplay = new DelayedFormattedString( '%.2f', 500, 0f )
bpsDisplay = new DelayedFormattedString( '%.0f', 500, 0f )
avgTpsDisplay = new DelayedFormattedString( '%.0f', 500, 0f )
avgBpsDisplay = new DelayedFormattedString( '%.0f', 500, 0f )
percentileDisplay = new DelayedFormattedString( '%.2f', 500, 0f )
avgRespSizeDisplay = new DelayedFormattedString( '%.0f', 500, 0f )

createProperty( 'selectedAgent', String, AGGREGATE )

OptionsProvider availableAgents = new OptionsProviderImpl( AGGREGATE );
OptionsProvider availableSourceIDs = new OptionsProviderImpl( "none" );
sourceIDs = ["none"]

double timeScaleFactor = 1 //seconds
double bytesScaleFactor = 1/(1024D) //KBytes

xRange = new CustomTimeRange(chartPeriod.value * 60000, rate.value)
xRange.visible = true

yRange = new CustomNumericRange(0, 10, 20)
yRange.visible = true
yRange.title = 'ms KB'

y2Range = new CustomNumericRange(0, 5, 20)
y2Range.visible = true
y2Range.title = 'requests'

ChartModel chartModel = new ChartModel(xRange, yRange, y2Range, 420, 180)
chartModel.addChartListener( new ChartAdapter() {
	public void chartCleared() {
		try {
			resetBuffers()
		}
		catch(Throwable e2) {
			ex(e2, 'chartCleared')
		}
	}
} )

chartModel.addSerie('Max', enableMax.value, true)
chartModel.addSerie('Min', enableMin.value, true)
chartModel.addSerie('Avg', enableAverage.value, true)
chartModel.addSerie('StdDev', enableStdDev.value, true)
chartModel.addSerie('TPS', enableTPS.value, true)
chartModel.addSerie('BPS', enableBPS.value, true)
chartModel.addSerie('AvgTPS', enableAvgTPS.value, true)
chartModel.addSerie('AvgBPS', enableAvgBPS.value, true)
chartModel.addSerie('Percentile', enablePercentile.value, true)
chartModel.addSerie('AvgResponseSize', enableAvgResponseSize.value, true)
chartModel.addSerie('Requests', enableRequests.value, false)
chartModel.addSerie('Running', enableRunning.value, false)
chartModel.addSerie('Completed', enableCompleted.value, false)
chartModel.addSerie('Queued', enableQueued.value, false)
chartModel.addSerie('Discarded', enableDiscarded.value, false)
chartModel.addSerie('Failed', enableFailed.value, false)
chartModel.legendColumns = 3

timeStats = new ValueStatistics( period.value * 60000 )
byteStats = new ValueStatistics( period.value * 60000 )

long max = 0
long min = Long.MAX_VALUE

agentData = [:]
agentStatistics = null

future = null
boolean connected = true

analyze = { message ->
	try {
		long timestamp = System.currentTimeMillis()
		
		if( !message.containsKey('TimeTaken') )
			return
		
		String sourceID = message['id'] == null ? message['ID']:message['id'] 
		
		if (!(sourceID == null) && !sourceIDs.contains(sourceID)) {
			sourceIDs.add(sourceID)
			availableSourceIDs.options = sourceIDs
		}
		
		if (currentSourceID.value == "none" || currentSourceID.value == sourceID) {
				long timeTaken = message['TimeTaken']
			timeStats.addValue( timestamp, timeTaken )
		
			long bytesCount = message['Bytes'] 
			if(bytesCount < 0 && message.containsKey('Response'))
				bytesCount = message['Response'].length
			if(bytesCount <= 0) bytesCount = 0
			byteStats.addValue( timestamp, bytesCount )
		}
	} catch(Exception e) {
		ex(e, 'Statistics -> analyze')
	}
}

onMessage = { o, i, m ->
	super.onTerminalMessage(o, i, m)
	if(i == remoteTerminal) {
		def data = new HashMap(m)
		data[AGENT_DATA_TIMESTAMP] = System.currentTimeMillis()
		agentData[o.label] = data
	}
	
	if( i == statisticsInput ) {
		if (  m.keySet().containsAll(["Requests", "Queued", "Running", "Completed", "Failed", "Discarded"]) ) {
			agentStatistics = new HashMap(m)
		} 
	}
}

onRelease = { 
	executor.shutdownNow()
	avgDisplay.release()
	minDisplay.release()
	maxDisplay.release()
	stdDevDisplay.release()
	tpsDisplay.release()
	bpsDisplay.release()
	avgTpsDisplay.release()
	avgBpsDisplay.release()
	percentileDisplay.release()
	avgRespSizeDisplay.release()
}

onConnect = { outgoing, incoming ->
	connected = (inputTerminal.connections.size() > 0) || (statisticsInput.connections.size() > 0)
}

onDisconnect = { outgoing, incoming ->
	connected = (inputTerminal.connections.size() > 0) || (statisticsInput.connections.size() > 0)
}

calculate = {
	if( !connected || !running )
		return
	
	long currentTime = System.currentTimeMillis()
	
	try {
		if( timeStats.size() > 0 || agentData.size() > 0 || agentStatistics?.size() > 0 ) {
			def message = newMessage()
			data = timeStats.getData( currentTime )
			message['Max'] = data['Max']
			message['Min'] = data['Min']
			message['Avg'] = data['Avg']
			message['Std-Dev'] = data['Std-Dev']
			message['Tps'] = data['Tps']
			message['Avg-Tps'] = data['Avg-Tps']
			message['Percentile'] = data['Percentile']
			
			bdata = byteStats.getData( currentTime )
			message['Bps'] = bdata['Vps']
			message['Avg-Bps'] = bdata['Avg-Vps']
			message['AvgResponseSize'] = bdata['AvgResponseSize']
			message['id'] = "test"
			
			message['Timestamp'] = currentTime
			message['ID'] = currentSourceID.value
			
			if (agentStatistics != null) {
				message['Requests'] = agentStatistics['Requests']
				message['Running'] = agentStatistics['Running']
				message['Discarded'] = agentStatistics['Discarded']
				message['Queued'] = agentStatistics['Queued']
				message['Failed'] = agentStatistics['Failed']
				message['Completed'] = agentStatistics['Completed']
			}

			send(output, message)
			send(controllerTerminal, message)
		}
	} catch(Throwable e1) {
		ex(e1, 'calculate')
	}
	def oldKeys = agentData.keySet().findAll { agentData[it][AGENT_DATA_TIMESTAMP] < currentTime - AGENT_DATA_TTL }
	oldKeys.each { agentData.remove( it ) }
	if( controller  && ( timeStats.size() > 0 || agentData.size() > 0 ))
		updateChart( currentTime )
}

updateChart = { currentTime ->
	def data = [:]
	if( selectedAgent.value == AGGREGATE ) {
		try {
			int count = 0
			def local = canvas instanceof ProjectItem || canvas.project?.workspace.localMode
			if (inputTerminal.connections.size() > 0 || statisticsInput.connections.size() > 0) {
				for( k in agentData.keySet() ) {
					//if in dist mode ignore data received on controllerTerminal, in local take only from data from it
					if(!local && !k.equals("controllerTerminal") || local && k.equals("controllerTerminal")){
						def d = agentData[k]
						//size gt 1 because sometimes messages contain only timestamp but there is no actual data
						if( !d.isEmpty() && d.size() > 1 ) {	
							data['Max'] = Math.max( d['Max'] ?: 0, data['Max'] ?: 0 )
							data['Min'] = Math.min( d['Min'] ?: Long.MAX_VALUE, data['Min'] ?: Long.MAX_VALUE )
							data['Avg'] = (data['Avg'] ?: 0) + (d['Avg'] ?: 0)
							data['Std-Dev'] = (data['Std-Dev'] ?: 0) + (d['Std-Dev'] ?: 0)
							data['Tps'] = (data['Tps'] ?: 0) + (d['Tps'] ?: 0)
							data['Avg-Tps'] = (data['Avg-Tps'] ?: 0) + (d['Avg-Tps'] ?: 0)
							data['Bps'] = (data['Bps'] ?: 0) + (d['Bps'] ?: 0)
							data['Avg-Bps'] = (data['Avg-Bps'] ?: 0) + (d['Avg-Bps'] ?: 0)
							data['Percentile'] = (data['Percentile'] ?: 0) + (d['Percentile'] ?: 0)
							data['AvgResponseSize'] = (data['AvgResponseSize'] ?: 0) + (d['AvgResponseSize'] ?: 0)
							data['Requests'] = (data['Requests'] ?:  0) + (d['Requests']?:0)
							data['Running'] = (data['Running'] ?:  0) + (d['Running']?:0) 
							data['Completed'] = (data['Completed'] ?: 0) + (d['Completed']?:0)
							data['Queued'] = (data['Queued'] ?: 0) + (d['Queued']?:0)
							data['Discarded'] = (data['Discarded'] ?: 0) + (d['Discarded']?:0)
							data['Failed'] = (data['Failed'] ?: 0) + (d['Failed']?:0)
							count++
						}
					}
				}
			}
			if( count != 0 ) {
				data['Avg'] /= count
				data['Std-Dev'] /= count
			}
		} catch( e ) { ex(e, 'Aggregating')
		}
	} else {
		data = agentData[selectedAgent.value]
	}
	if(data == null || data.isEmpty()) {
		return
	}
	try {
		if (inputTerminal.connections.size() > 0 || remoteTerminal.connections.size() > 0  ) {
			if(enableMax.value) chartModel.addPoint(0, currentTime, data['Max'] * timeScaleFactor)
			if(enableMin.value) chartModel.addPoint(1, currentTime, data['Min'] * timeScaleFactor)
			if(enableAverage.value) chartModel.addPoint(2, currentTime, data['Avg'] * timeScaleFactor)
			if(enableStdDev.value) chartModel.addPoint(3, currentTime, data['Std-Dev'] * timeScaleFactor)
			if(enableTPS.value) chartModel.addPoint(4, currentTime, data['Tps'])
			if(enableBPS.value) chartModel.addPoint(5, currentTime, data['Bps'] * bytesScaleFactor)
			if(enableAvgTPS.value) chartModel.addPoint(6, currentTime, data['Avg-Tps'])
			if(enableAvgBPS.value) chartModel.addPoint(7, currentTime, data['Avg-Bps'] * bytesScaleFactor)
			if(enablePercentile.value) chartModel.addPoint(8, currentTime, data['Percentile'])
			if(enableAvgResponseSize.value) chartModel.addPoint(9, currentTime, data['AvgResponseSize'] * bytesScaleFactor)
		}
		
		if (statisticsInput.connections.size() > 0 || remoteTerminal.connections.size() > 0  ) {
			if(enableRequests.value) chartModel.addPoint(10, currentTime, data['Requests'])
			if(enableRunning.value) chartModel.addPoint(11, currentTime, data['Running'])
			if(enableCompleted.value) chartModel.addPoint(12, currentTime, data['Completed'])
			if(enableQueued.value) chartModel.addPoint(13, currentTime, data['Queued'])
			if(enableDiscarded.value) chartModel.addPoint(14, currentTime, data['Discarded'])
			if(enableFailed.value) chartModel.addPoint(15, currentTime, data['Failed'])
		}
		
		if (inputTerminal.connections.size() > 0) {
			avgDisplay.setArgs((float)data['Avg']  * timeScaleFactor)
			minDisplay.setArgs((float)data['Min']  * timeScaleFactor)
			maxDisplay.setArgs((float)data['Max'] * timeScaleFactor)
			stdDevDisplay.setArgs((float)data['Std-Dev'] * timeScaleFactor)
			tpsDisplay.setArgs((float)data['Tps'])
			bpsDisplay.setArgs((float)data['Bps'] * bytesScaleFactor)
			avgTpsDisplay.setArgs((float)data['Avg-Tps'])
			avgBpsDisplay.setArgs((float)data['Avg-Bps'] * bytesScaleFactor)
			percentileDisplay.setArgs((float)data['Percentile'])
			avgRespSizeDisplay.setArgs((float)data['AvgResponseSize']  * bytesScaleFactor)
		}
	} catch( e ) {
		e.printStackTrace()
	}

}

ex = {t, m ->
	println('-------------------------------')
	println("exception in $m method in groovy")
	println("type: $t")
	println('stacktrace:')
	boolean first = true
	for(item in t.stackTrace){
		if(first) {
			println(item)
			first = false
		} else {
			println("\t$item")
		}
	}
	println('-------------------------------')
}

schedule = {
	future?.cancel( true )
	future = executor.scheduleAtFixedRate( calculate, rate.value, rate.value, TimeUnit.MILLISECONDS )
}

addEventListener(PropertyEvent) { event ->
	try {
		if (event.event == PropertyEvent.Event.VALUE) {
			if(event.property == rate) {
				xRange.setRate(rate.value)
				schedule()
			}
			else if(event.property == chartPeriod) {
				xRange.setPeriod(chartPeriod.value * 60000)
			}
			else if(event.property == enableMin) {
				chartModel.enableSerie('Min', enableMin.value)
				buildSignature()
			}
			else if(event.property == enableMax) {
				chartModel.enableSerie('Max', enableMax.value)
				buildSignature()
			}
			else if(event.property == enableAverage) {
				chartModel.enableSerie('Avg', enableAverage.value)
				buildSignature()
			}
			else if(event.property == enableStdDev) {
				chartModel.enableSerie('StdDev', enableStdDev.value)
				buildSignature()
			}
			else if(event.property == enableTPS) {
				chartModel.enableSerie('TPS', enableTPS.value)
				buildSignature()
			}
			else if(event.property == enableBPS) {
				chartModel.enableSerie('BPS', enableBPS.value)
				buildSignature()
			}
			else if(event.property == enableAvgTPS) {
				chartModel.enableSerie('AvgTPS', enableAvgTPS.value)
				buildSignature()
			}
			else if(event.property == enableAvgBPS) {
				chartModel.enableSerie('AvgBPS', enableAvgBPS.value)
				buildSignature()
			}
			else if(event.property == enablePercentile) {
				chartModel.enableSerie('Percentile', enablePercentile.value)
				buildSignature()
			}
			else if(event.property == enableAvgResponseSize) {
				chartModel.enableSerie('AvgResponseSize', enableAvgResponseSize.value)
				buildSignature()
			}
			else if(event.property == enableRequests) {
				chartModel.enableSerie('Requests', enableRequests.value)
			}
			else if(event.property == enableRunning) {
				chartModel.enableSerie('Running', enableRunning.value)
			}
			else if(event.property == enableCompleted) {
				chartModel.enableSerie('Completed', enableCompleted.value)
			}
			else if(event.property == enableQueued) {
				chartModel.enableSerie('Queued', enableQueued.value)
			}
			else if(event.property == enableDiscarded) {
				chartModel.enableSerie('Discarded', enableDiscarded.value)
			}
			else if(event.property == enableFailed) {
				chartModel.enableSerie('Failed', enableFailed.value)
			}
		}
	}
	catch(Throwable e2){
		ex(e2, 'addEventListener')
	}
}

buildSignature = {
	def signature = [:]
	if(enableAverage.value) signature['Avg'] = Long
	if(enableMin.value) signature['Min'] = Long
	if(enableMax.value) signature['Max'] = Long
	if(enableTPS.value) signature['Tps'] = Double
	if(enableBPS.value) signature['Bps'] = Double
	if(enableAvgTPS.value) signature['Avg-Tps'] = Double
	if(enableAvgBPS.value) signature['Avg-Bps'] = Long
	if(enableStdDev.value) signature['Std-Dev'] = Long
	if(enablePercentile.value) signature['Percentile'] = Long
	if(enablePercentile.value) signature['AvgResponseSize'] = Long
	setSignature(output, signature)
}

resetComponent = {
	chartModel.clear()
	sourceIDs = ["none"]
	currentSourceID.value = "none"
	availableSourceIDs.options = sourceIDs 
	avgDisplay.setArgs(0f)
	minDisplay.setArgs(0f)
	maxDisplay.setArgs(0f)
	stdDevDisplay.setArgs(0f)
	tpsDisplay.setArgs(0f)
	bpsDisplay.setArgs(0f)
	avgTpsDisplay.setArgs(0f)
	avgBpsDisplay.setArgs(0f)
	percentileDisplay.setArgs(0f)
	avgRespSizeDisplay.setArgs(0f)
}

resetBuffers = {
	timeStats.reset()
	byteStats.reset()
	agentData = [:]
	agentStatistics = null
}

fixOptions = {
	def options = [ AGGREGATE ]
	options.addAll( agentTerminals.collect { it.label }.sort() )
	availableAgents.options = options
}

addEventListener( ActionEvent ) { event ->
	if( event.key == 'RESET' ) resetComponent()
	else if( event.key == 'STOP' ) {
		agentData?.clear() // this should never be null but just in case
		agentStatistics?.clear() // this could be null, so it needs check
		if( !controller )
			send( controllerTerminal, newMessage() )
	}
	//else if ( event.key == 'START' ) {
	//	schedule()
	//}
	chartModel.setTestRunning( canvas.running )
}

addEventListener( canvas, BaseEvent ) { event ->
	if( event.key == CanvasItem.RUNNING ) chartModel.setTestRunning( canvas.running )
}

addEventListener( CollectionEvent ) { event ->
	if( event.key == ComponentContext.AGENT_TERMINALS ) {
		fixOptions()
	}
}

//Layout
layout(layout:'fillx, wrap 2') {
	node( widget: 'chartWidget', constraints: "spanx 2, wrap", model: chartModel )
	property( property: selectedAgent, label: 'View statistics from', options: availableAgents, widget:'comboBox' )
	property( property: currentSourceID, label: 'Source ID', options: availableSourceIDs, widget:'comboBox', 
		contstraints: "w 100!" )
}

compactLayout {
	box( widget:'display', layout:'align left, wrap 5' ) {
		node( label:'Average ', fString:avgDisplay, constraints:'w 60!' )
		node( label:'Minimum ', fString:minDisplay, constraints:'w 60!' )
		node( label:'Maximum ', fString:maxDisplay, constraints:'w 60!' )
		node( label:'Std Dev ', fString:stdDevDisplay, constraints:'w 60!' )
		node( label:'TPS     ', fString:tpsDisplay, constraints:'w 60!' )
		node( label:'BPS     ', fString:bpsDisplay, constraints:'w 60!' )
		node( label:'Avg TPS ', fString:avgTpsDisplay, constraints:'w 60!' )
		node( label:'Avg BPS ', fString:avgBpsDisplay, constraints:'w 60!' )
		node( label:'Perc    ', fString:percentileDisplay, constraints:'w 60!' )
		node( label:'Avg Size', fString:avgRespSizeDisplay, constraints:'w 60!' )
	}
}

settings( label: 'Properties' ) {
	property(property: addtoSummary, label: "Add last result to summary?")
	box {
		property(property: enableAverage, label: 'Enable Average' )
		property(property: enableMin, label: 'Enable Min' )
		property(property: enableMax, label: 'Enable Max' )
		property(property: enableStdDev, label: 'Enable Std Dev' )
		property(property: enableTPS, label: 'Enable TPS' )
		property(property: enableBPS, label: 'Enable BPS' )
		property(property: enableAvgTPS, label: 'Enable Average TPS' )
		property(property: enableAvgBPS, label: 'Enable Average BPS' )
		property(property: enablePercentile, label: '90% Percentile' )
		property(property: enableAvgResponseSize, label: 'Average Response Size' )
		property(property: currentSourceID, label: 'Source ID' )
	}
} 

settings( label: 'Statistics' ) {
	box {
		property(property: enableRequests, label: 'Enable Requests' )
		property(property: enableRunning, label: 'Enable Running' )
		property(property: enableCompleted, label: 'Enable Completed' )
		property(property: enableQueued, label: 'Enable Queued' )
		property(property: enableDiscarded, label: 'Enable Discarded' )
		property(property: enableFailed, label: 'Enable Failed' )
	}
} 

settings( label: "Periods" ) {
	property(property: rate, label: 'Refresh rate (ms)' )
	property(property: chartPeriod, label: 'Chart period (min)' )
	property(property: period, label: 'History (min)' )
} 

generateSummary = { chapter ->
	if (addtoSummary.value) {
		LTableModel table = new LTableModel(1, false);
		ArrayList values = new ArrayList();
		table.addColumn("SourceID");
		values.add(currentSourceID.value);
		if(enableAverage.value) {
			table.addColumn("Avg");
			values.add(data['Avg'].round(2));
		}
		if(enableMin.value) {
			table.addColumn("Min");
			values.add(data['Min']);
		}
		if(enableMax.value) {
			table.addColumn("Max");
			values.add(data['Max']);
		}
		if(enableTPS.value) {
			table.addColumn("TPS");
			values.add(data['Tps'].round(2));
		}
		if(enableBPS.value) {
			table.addColumn("BPS");
			values.add(bpsDisplay.toString());
		}
		if(enableAvgTPS.value) {
			table.addColumn("Avg TPS");
			values.add(data['Avg-Tps']);
		}
		if(enableAvgBPS.value) {
			table.addColumn("Avg BPS");
			values.add(avgBpsDisplay.toString());
		}
		if(enableStdDev.value) {
			table.addColumn("Std-Dev");
			values.add(data['Std-Dev'].round(2));
		}
		if(enablePercentile.value) {
			table.addColumn("Perc");
			values.add(data['Percentile'].round(2));
		}
		if(enableAvgResponseSize.value) {
			table.addColumn("Avg Size");
			values.add(data['AvgResponseSize']);
		}
		
		table.addRow(values);
		
		MutableSection sect = chapter.addSection(getLabel());
		sect.addTable(getLabel(), table)
   	}

}

//disable it here, because sometimes when you initialize chart
//whithout enabling series it wasn't working correctly, so it is
//safer to enable everythig first and then disable what is not needed
enableMax.value = false
enableMin.value = false
enableBPS.value = false
enableAvgTPS.value = false
enableAvgBPS.value = false
enablePercentile.value = false
enableAvgResponseSize.value = false
enableRequests.value = false
enableCompleted.value = false
enableDiscarded.value = false

buildSignature()
fixOptions()
schedule()
chartModel.setTestRunning( canvas.running )