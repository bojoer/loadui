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
 * A statistics diagram that is interactively updated and scrolls 
 * sideways over time. 
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
createProperty( 'sourceID', String, "" )
createProperty( 'addtoSummary', Boolean, false )

createProperty( 'selectedRunner', String, AGGREGATE )

OptionsProvider availableRunners = new OptionsProviderImpl( AGGREGATE );

double timeScaleFactor = 1 //seconds
double bytesScaleFactor = 1/(1024D) //KBytes

xRange = new CustomTimeRange(chartPeriod.value * 60000, rate.value)
xRange.visible = true

yRange = new CustomNumericRange(0, 10, 20)
yRange.visible = true
yRange.title = 'ms KB'

ChartModel chartModel = new ChartModel(xRange, yRange, 420, 180)
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

chartModel.addSerie('Max', enableMax.value)
chartModel.addSerie('Min', enableMin.value)
chartModel.addSerie('Avg', enableAverage.value)
chartModel.addSerie('StdDev', enableStdDev.value)
chartModel.addSerie('TPS', enableTPS.value)
chartModel.addSerie('BPS', enableBPS.value)
chartModel.addSerie('AvgTPS', enableAvgTPS.value)
chartModel.addSerie('AvgBPS', enableAvgBPS.value)
chartModel.legendColumns = 4

timeStats = new ValueStatistics( period.value * 60000 )
byteStats = new ValueStatistics( period.value * 60000 )

long max = 0
long min = Long.MAX_VALUE

runnerData = [:]

future = null
boolean connected = false

analyze = { message ->
	try {
		long timestamp = System.currentTimeMillis()
		
		long timeTaken = message['TimeTaken']
		timeStats.addValue( timestamp, timeTaken )
		
		long bytesCount = message['Bytes'] 
		if(bytesCount < 0 && message.containsKey('Response'))
			bytesCount = message['Response'].length
		if(bytesCount <= 0) bytesCount = 0
		byteStats.addValue( timestamp, bytesCount )
	} catch(Exception e) {
		ex(e, 'Statistics -> analyze')
	}
}

onMessage = { o, i, m ->
	
	super.onTerminalMessage(o, i, m)
	if(i == remoteTerminal ) {
		runnerData[o.label] = new HashMap(m)
	}
}

onRelease = { executor.shutdownNow() }

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
			
			bdata = byteStats.getData( currentTime )
			message['Bps'] = bdata['Vps']
			message['Avg-Bps'] = bdata['Avg-Vps']
			
			message['Timestamp'] = currentTime
			message['ID'] = sourceID.value
			
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
	if( selectedRunner.value == AGGREGATE ) {
		try {
			int count = 0
			for( d in runnerData.values() ) {
				if( !d.isEmpty() ) {
					data['Max'] = Math.max( d['Max'], data['Max'] ?: 0 )
					data['Min'] = Math.min( d['Min'], data['Min'] ?: Long.MAX_VALUE )
					data['Avg'] = (data['Avg'] ?: 0) + d['Avg']
					data['Std-Dev'] = (data['Std-Dev'] ?: 0) + d['Std-Dev']
					data['Tps'] = (data['Tps'] ?: 0) + d['Tps']
					data['Avg-Tps'] = (data['Avg-Tps'] ?: 0) + d['Avg-Tps']
					data['Bps'] = (data['Bps'] ?: 0) + (d['Bps'] ?: 0)
					data['Avg-Bps'] = (data['Avg-Bps'] ?: 0) + (d['Avg-Bps'] ?: 0)
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
		data = runnerData[selectedRunner.value]
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
	} catch( e ) {
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
	setSignature(output, signature)
}

resetComponent = {
	chartModel.clear()
}

resetBuffers = {
	timeStats.reset()
	byteStats.reset()
	runnerData = [:]
}

fixOptions = {
	def options = [ AGGREGATE ]
	options.addAll( runnerTerminals.collect { it.label }.sort() )
	availableRunners.options = options
}

addEventListener( ActionEvent ) { event ->
	if( event.key == 'RESET' ) resetComponent()
	else if( event.key == 'STOP' ) {
		runnerData.clear()
		if( !controller )
			send( controllerTerminal, newMessage() )
	}
	//else if ( event.key == 'START' ) schedule()
}

addEventListener( CollectionEvent ) { event ->
	if( event.key == ComponentContext.RUNNER_TERMINALS ) {
		resetComponent()
		fixOptions()
	}
}

//Layout
layout(constraints:'fillx, wrap 1') {
	node( widget: 'chartWidget', model: chartModel )
	property( property: selectedRunner, label: 'View statistics from', options: availableRunners, widget:'comboBox' )
}

settings( label: 'Properties', constraints: 'wrap 2' ) {
	property(property: addtoSummary, label: "Add last result to summary?")
	box(constraints:'growx, wrap 1') {
		property(property: enableAverage, label: 'Enable Average' )
		property(property: enableMin, label: 'Enable Min' )
		property(property: enableMax, label: 'Enable Max' )
		property(property: enableStdDev, label: 'Enable Std Dev' )
		property(property: enableTPS, label: 'Enable TPS' )
		property(property: enableBPS, label: 'Enable BPS' )
		property(property: enableAvgTPS, label: 'Enable Average TPS' )
		property(property: enableAvgBPS, label: 'Enable Average BPS' )
		property(property: sourceID, label: 'Source ID' )
	}
} 

settings( label: "Periods", constraints: 'wrap 2' ) {
	box(constraints:"growx, wrap 1") {
		property(property: rate, label: 'Refresh rate (ms)' )
		property(property: chartPeriod, label: 'Chart period (min)' )
		property(property: period, label: 'History (min)' )
	}
} 

generateSummary = { chapter ->
	if (addtoSummary.value) {
		LTableModel table = new LTableModel(1, false);
		ArrayList values = new ArrayList();
		table.addColumn("SourceID");
		values.add(sourceID.value);
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
		
		table.addRow(values);
		
		MutableSection sect = chapter.addSection(getLabel());
		sect.addTable(getLabel(), table)
   	}

}

buildSignature()
fixOptions()
schedule()
