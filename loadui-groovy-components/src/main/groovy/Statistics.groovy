import com.eviware.loadui.api.chart.ChartSerie;
import com.eviware.loadui.api.chart.Point;

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

import com.eviware.loadui.api.chart.CustomTimeRange
import com.eviware.loadui.api.chart.CustomNumericRange
import com.eviware.loadui.api.chart.ChartModel
import com.eviware.loadui.api.chart.ChartAdapter;

import com.eviware.loadui.api.terminal.InputTerminal
import com.eviware.loadui.api.terminal.OutputTerminal
import com.eviware.loadui.api.terminal.TerminalMessage

import com.eviware.loadui.api.layout.OptionsProvider
import com.eviware.loadui.impl.layout.OptionsProviderImpl

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import com.eviware.loadui.api.events.PropertyEvent
import com.eviware.loadui.api.events.CollectionEvent
import com.eviware.loadui.api.events.ActionEvent

import com.eviware.loadui.util.statistics.ValueStatistics
import com.eviware.loadui.api.ui.table.LTableModel
import com.eviware.loadui.api.summary.MutableSection

import com.eviware.loadui.util.layout.DelayedFormattedString


AGGREGATE = "Aggregate"

executor = Executors.newSingleThreadScheduledExecutor()

createOutput( 'output', 'Statistics Output')

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
createProperty( 'currentSourceID', String, "none" )
createProperty( 'addtoSummary', Boolean, false )

avgDisplay = new DelayedFormattedString( '%d', 500, 0 )
minDisplay = new DelayedFormattedString( '%d', 500, 0 )
maxDisplay = new DelayedFormattedString( '%d', 500, 0 )
stdDevDisplay = new DelayedFormattedString( '%d', 500, 0 )
tpsDisplay = new DelayedFormattedString( '%d', 500, 0 )
bpsDisplay = new DelayedFormattedString( '%d', 500, 0 )
avgTpsDisplay = new DelayedFormattedString( '%d', 500, 0 )
avgBpsDisplay = new DelayedFormattedString( '%d', 500, 0 )
percentileDisplay = new DelayedFormattedString( '%d', 500, 0 )
avgRespSizeDisplay = new DelayedFormattedString( '%d', 500, 0 )


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

y2Range = new CustomNumericRange(0, 10, 20)
y2Range.visible = true
y2Range.title = 'requests'



ChartModel chartModel = new ChartModel(xRange, yRange, y2Range, 420, 180)
chartModel.addChartListener(new ChartAdapter(){
	public void chartCleared(){
		try{
			resetBuffers()
		}
		catch(Throwable e2){
			ex(e2, 'chartCleared')
		}
	}
});

chartModel.addSerie('Max', enableMax.value, true)
chartModel.addSerie('Min', enableMin.value, true)
chartModel.addSerie('Avg', enableAverage.value, true)
chartModel.addSerie('StdDev', enableStdDev.value, true)
chartModel.addSerie('TPS', enableTPS.value, false)
chartModel.addSerie('BPS', enableBPS.value, true)
chartModel.addSerie('AvgTPS', enableAvgTPS.value, true)
chartModel.addSerie('AvgBPS', enableAvgBPS.value, true)
chartModel.addSerie('Percentile', enablePercentile.value, true)
chartModel.addSerie('AvgResponseSize', enableAvgResponseSize.value, true)
chartModel.legendColumns = 3

timeStats = new ValueStatistics( period.value * 60000 )
byteStats = new ValueStatistics( period.value * 60000 )

long max = 0
long min = Long.MAX_VALUE

agentData = [:]

future = null
boolean connected = false

analyze = { message ->
	try {
		long timestamp = System.currentTimeMillis()
		
		if( !message.containsKey('TimeTaken') )
			return
		
		String sourceID = message['id']
		
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
	if(i == remoteTerminal ) {
		agentData[o.label] = new HashMap(m)
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
	connected = inputTerminal.connections.size() > 0
}

onDisconnect = { outgoing, incoming ->
	connected = inputTerminal.connections.size() > 0
}

calculate = {
	if( !connected || !running )
		return
	
	long currentTime = System.currentTimeMillis()
	
	try {
		if( timeStats.size() > 0 ) {
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
			
			send(controllerTerminal, message)
			send(output, message)
		}
	} catch(Throwable e1) {
		ex(e1, 'calculate')
	}
	
	if( controller )
		updateChart( currentTime )
}

updateChart = { currentTime ->
	def data = [:]
	if( selectedAgent.value == AGGREGATE ) {
		try {
			int count = 0
			for( d in agentData.values() ) {
				if( !d.isEmpty() ) {
					data['Max'] = Math.max( d['Max'], data['Max'] ?: 0 )
					data['Min'] = Math.min( d['Min'], data['Min'] ?: Long.MAX_VALUE )
					data['Avg'] = (data['Avg'] ?: 0) + d['Avg']
					data['Std-Dev'] = (data['Std-Dev'] ?: 0) + d['Std-Dev']
					data['Tps'] = (data['Tps'] ?: 0) + d['Tps']
					data['Avg-Tps'] = (data['Avg-Tps'] ?: 0) + d['Avg-Tps']
					data['Bps'] = (data['Bps'] ?: 0) + (d['Bps'] ?: 0)
					data['Avg-Bps'] = (data['Avg-Bps'] ?: 0) + (d['Avg-Bps'] ?: 0)
					data['Percentile'] = (data['Percentile'] ?: 0) + (d['Percentile'] ?: 0)
					data['AvgResponseSize'] = (data['AvgResponseSize'] ?: 0) + (d['AvgResponseSize'] ?: 0)
					count++
				}
			}
			if( count == 0 )
				return
			data['Avg'] /= count
			data['Std-Dev'] /= count
		} catch( e ) { ex(e, 'Aggregating')
		}
	} else
		data = agentData[selectedAgent.value]
	if(data == null || data.isEmpty())
		return
	
	try {
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
		avgDisplay.setArgs(data['Avg'] )
		minDisplay.setArgs(data['Min'])
		maxDisplay.setArgs(data['Max'])
		stdDevDisplay.setArgs(data['Std-Dev'])
		tpsDisplay.setArgs(data['Tps'])
		bpsDisplay.setArgs(data['Bps'])
		avgTpsDisplay.setArgs(data['Avg-Tps'])
		avgBpsDisplay.setArgs(data['Avg-Bps'])
		percentileDisplay.setArgs(data['Percentile'])
		avgRespSizeDisplay.setArgs(data['AvgResponseSize'])
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
}

resetBuffers = {
	timeStats.reset()
	byteStats.reset()
	agentData = [:]
}

fixOptions = {
	def options = [ AGGREGATE ]
	options.addAll( agentTerminals.collect { it.label }.sort() )
	availableAgents.options = options
}

addEventListener( ActionEvent ) { event ->
	if( event.key == 'RESET' ) resetComponent()
	else if( event.key == 'STOP' ) {
		agentData.clear()
		if( !controller )
			send( controllerTerminal, newMessage() )
	}
	//else if ( event.key == 'START' ) schedule()
}

addEventListener( CollectionEvent ) { event ->
	if( event.key == ComponentContext.AGENT_TERMINALS ) {
		resetComponent()
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
	box( widget:'display', layout:'wrap 5, align right' ) {
		node( label:'Average', fString:avgDisplay )
		node( label:'Minimum', fString:minDisplay )
		node( label:'Maximum', fString:maxDisplay )
		node( label:'Standard Deviation', fString:stdDevDisplay )
		node( label:'TPS', fString:tpsDisplay )
		node( label:'BPS', fString:bpsDisplay )
		node( label:'Average TPS', fString:avgTpsDisplay )
		node( label:'Average BPS', fString:avgBpsDisplay )
		node( label:'Percentile', fString:percentileDisplay )
		node( label:'Average Response Size', fString:avgRespSizeDisplay )
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
			values.add(data['Bps']?.round(2));
		}
		if(enableAvgTPS.value) {
			table.addColumn("Avg TPS");
			values.add(data['Avg-Tps']);
		}
		if(enableAvgBPS.value) {
			table.addColumn("Avg BPS");
			values.add(data['Avg-Bps']);
		}
		if(enableStdDev.value) {
			table.addColumn("Std-Dev");
			values.add(data['Std-Dev'].round(2));
		}
		if(enablePercentile.value) {
			table.addColumn("Percentile");
			values.add(data['Percentile'].round(2));
		}
		if(enableAvgResponseSize.value) {
			table.addColumn("AvgResponseSize");
			values.add(data['AvgResponseSize']);
		}
		
		table.addRow(values);
		
		MutableSection sect = chapter.addSection(getLabel());
		sect.addTable(getLabel(), table)
   	}

}

buildSignature()
fixOptions()
schedule()
