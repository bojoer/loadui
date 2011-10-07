// 
// Copyright 2011 SmartBear Software
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
 * Sends an empty message in a variance pattern depending on the settings.
 * 
 * @id com.eviware.Variance
 * @help http://www.loadui.org/Generators/variance-component.html
 * @category generators
 * @nonBlocking true
 */

import com.eviware.loadui.api.events.PropertyEvent
import com.eviware.loadui.api.events.ActionEvent
import java.util.concurrent.TimeUnit

def FUNCTIONS = [
	'Saw-tooth': { amp, progress -> amp * progress -amp/2  },
	'Sine-wave': { amp, progress -> amp/2 * Math.sin( 2*Math.PI * progress) },
	'Square': { amp, progress -> progress < 0.5  ? -amp/2 : amp/2 }
]

def UNITS = [
	'Sec': TimeUnit.SECONDS,
	'Min': TimeUnit.MINUTES,
	'Hour': TimeUnit.HOURS
]

//Properties
createProperty( 'rate', Long, 10 )
createProperty( 'unit', String, 'Sec' )
createProperty( 'shape', String, 'Saw-tooth' )
createProperty( 'amplitude', Long, 5 )
createProperty( 'period', Long, 60 )

currentRate = rate.value
startTime = System.currentTimeMillis()

calculateRate = {
	def per = period.value * 1000
	def progress = ( ( System.currentTimeMillis() - startTime ) % per ) / per
	def newRate = Math.round( rate.value + FUNCTIONS[shape.value]( amplitude.value, progress ) )
	if( currentRate != newRate ) {
		currentRate = newRate
		schedule()
	}
}

future = null
pollFuture = null
initialize = {
	startTime = System.currentTimeMillis()
	pollFuture?.cancel( true )
	pollFuture = scheduleAtFixedRate( calculateRate, 0, 250, TimeUnit.MILLISECONDS )
	schedule()
}

schedule = {
	future?.cancel( true )
	if( stateProperty.value && currentRate > 0 ) {	
		def triggerDelay = UNITS[unit.value].toMicros(1) / currentRate
		future = scheduleAtFixedRate( { trigger() }, triggerDelay, triggerDelay, TimeUnit.MICROSECONDS )
	}
}

onAction("START") { initialize() }
onAction("STOP") { future?.cancel( true ) ; pollFuture?.cancel( true ) }
onAction("RESET") { startTime = System.currentTimeMillis() }

layout  { 
	property( property: rate, label:'Base Rate', min: 0 ) 
	property( property: unit, label:'Unit', options: UNITS.keySet() )
	separator( vertical: true )
	node( widget: 'selectorWidget', label: 'Variance type', showLabels: false, labels: FUNCTIONS.keySet(), 
			images: ['variance2_shape.png', 'variance1_shape.png', 'variance3_shape.png'], default: shape.value, selected: shape )
	separator( vertical: true )
	property( property: amplitude, label:'Amplitude', min: 0 )
	separator( vertical: true )
	property( property: period, label:'Period\n(seconds)', min: 1 )
	separator( vertical: true )
	box ( layout: "wrap, ins 0" ) {
		box( widget: 'display' ) {
			node( label: 'Rate', content: { "$currentRate / $unit.value" }, constraints: "w 60!" )
		}
		action( label: "Restart", action: { startTime = System.currentTimeMillis() }, constraints: "align right" )
	}
}

compactLayout  {
	box( widget:'display' ) {
		node( label:'Rate', content: { "$currentRate / $unit.value" } )
	}
}

if( running ) initialize()