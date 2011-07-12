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
 * Sends an empty message in a variance pattern depending on the settings.
 * 
 * @id com.eviware.Variance
 * @help http://www.loadui.org/Generators/variance-component.html
 * @category generators
 * @nonBlocking true
 */

import com.eviware.loadui.api.events.PropertyEvent
import com.eviware.loadui.api.events.ActionEvent
import com.eviware.loadui.util.layout.DelayedFormattedString


//Properties
createProperty( 'rate', Long, 10 )
createProperty( 'unit', String, 'Seconds' )
createProperty( 'shape', String, 'Saw-tooth' )
createProperty( 'amplitude', Long, 5 )
createProperty( 'period', Long, 60 )

currentRate = 0
currentDelay = 0
timeEllapsed = 0
maxRate = rate.value + (amplitude.value/2)
minRate = (rate.value - (amplitude.value/2) < 0)?0:rate.value - (amplitude.value/2)
msPerUnit = 1000

rateDisplay = new DelayedFormattedString( '%d / %s', 500, currentRate.longValue(), unit.value )

timer = new Timer(true)
scheduled = false

future = null

//Saw tooth parameters
gradient = 0D

onRelease = {  rateDisplay.release() }

reset = {
	currentDelay = 0
	timeEllapsed = 0
	
	if (shape.value == "Saw-tooth") {
		currentRate = minRate
		//Some sanity checks
		if (minRate.longValue() != 0) {
			if (msPerUnit/minRate > period.value * 1000) {
				gradient = 0
				currentRate = maxRate
			}
		} 
		
		if (currentRate != maxRate) {
			gradient = (double)((double)amplitude.value/ (period.value * 1000D))
			currentRate = minRate
			while (currentRate.longValue() == 0) {
				timeEllapsed++
				currentRate = minRate + timeEllapsed * gradient
			}
		}
	}
	
	if (shape.value == "Sine-wave" ) {
		currentRate = rate.value
	}
	
	if (shape.value == "Square" ) {
		currentRate = maxRate
	}
	
	scheduled = false
}

start = {
	reset();
	schedule();
}

schedule = {
	if (stateProperty.value && running && !scheduled) {
		timeEllapsed += currentDelay
		
		if (shape.value == "Saw-tooth") {
			if (gradient > 0) {
				if (timeEllapsed >= period.value * 1000) {
					timeEllapsed = 0
					currentRate = minRate
				} else {
					currentRate = minRate + timeEllapsed * gradient
				}
			}
		}
		
		if (shape.value == "Sine-wave" ) {
			currentRate = amplitude.value * Math.sin((double)(timeEllapsed  * Math.PI)/((double)period.value * 1000D)) + rate.value
		}
		
		if (shape.value == "Square" ) {
			if (timeEllapsed >= period.value * 500) {
				timeEllapsed = 0
				if (currentRate == maxRate) 
					currentRate = minRate
				else
					currentRate = maxRate
			}
		}
		
		if (shape.value == "Square" && currentRate == 0) {
			currentDelay = period * 500
			future = timer.runAfter(currentDelay) { schedule() } //In this case we are not firing any events for a while	
		} else {
			if (currentRate.longValue() > 0) 
				currentDelay = msPerUnit/currentRate
			if (currentDelay.longValue() == 0)
				currentDelay = 1 // This is to avoid the cases where the delay is low to the point where the events would fire to quickly for us to deal with
			
			
			future = timer.runAfter(currentDelay.intValue()) {
				rateDisplay.setArgs( currentRate.longValue(), unit.value )
				trigger()
				scheduled = false
				schedule()
			}
			scheduled = true
		}
	}
}

addEventListener( PropertyEvent ) { event ->
	if ( event.event == PropertyEvent.Event.VALUE ) {
		
		if( event.property == unit ) {
			if ( unit.value == "Sec" )
				msPerUnit = 1000
			if ( unit.value == "Min" )
				msPerUnit = 60000
			if ( unit.value == "Hour" )
				msPerUnit = 3600000
		}
		
		maxRate = rate.value + (amplitude.value/2)
		minRate = (rate.value - (amplitude.value/2) < 0)?0:rate.value - (amplitude.value/2)
		currentRate = 0
		
		rateDisplay.setArgs( currentRate.longValue(), unit.value )
		if (event.property == stateProperty && !stateProperty.value)
			future?.cancel()
		if (stateProperty.value)
			future?.cancel()
		start()
	}
}

onAction("START") { schedule() }
onAction("STOP") { future?.cancel() }
onAction("RESET") { reset() }

//Layout
layout  { 
	property( property:rate, label:'Rate', min:0 ) 
	separator( vertical:true )
	property( property:unit, label:'Unit', options:['Sec','Min','Hour'] )
	separator( vertical:true )
	
	node(widget: 'selectorWidget', label:'Variance type', showLabels:false, labels:['Saw-tooth', 'Sine-wave', 'Square'], 
			images:['variance2_shape.png', 'variance1_shape.png', 'variance3_shape.png'], default: shape.value, selected: shape)
	
	separator( vertical:true )
	property( property:amplitude, label:'Amplitude', min: 0 )
	separator( vertical:true )
	property( property:period, label:'Period\n(seconds)', min: 1 )
	separator( vertical:true )
	box ( layout:"wrap, ins 0" ) {
		box( widget:'display' ) {
			node( label:'Rate', fString:rateDisplay, constraints:"w 60!" )
		}
		action( label:"Restart", action: { start() }, constraints:"align right" )
	}
}

//Compact Layout
compactLayout  {
	box( widget:'display' ) {
		node( label:'Rate', fString:rateDisplay )
	}
}

reset();
if (running)
	start();

//Settings
//settings( label: "Settings", layout: 'wrap 2' ) {
//	box(layout:"growx, wrap 1") {
//		property( property:rate, label:'Rate', min:0 ) 
//		property( property:shape, label:'Shape', options:['Saw-tooth', 'Sine-wave', 'Square'] )
//		property( property:amplitude, label:'Amplitude', min: 0 )
//		property( property:unit, label:'Unit', options:['Sec','Min','Hour'] )
//		property( property:period, label:'Period (seconds)', min: 1 )
//	}
//} 
