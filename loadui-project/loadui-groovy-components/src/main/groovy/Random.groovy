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
 * Sends and empty message at random intervals around a base rate
 * 
 * @category generators
 * @nonBlocking true
 */

import com.eviware.loadui.api.events.PropertyEvent
import com.eviware.loadui.api.events.ActionEvent
import com.eviware.loadui.util.layout.DelayedFormattedString

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

executor = Executors.newSingleThreadScheduledExecutor()

//Properties
createProperty( 'rate', Long, 10 )
createProperty( 'unit', String, 'Sec' )
createProperty( 'type', String, 'Uniform' )
createProperty( 'factor', Long, 0 )

msPerUnit = 1000
defaultDelay = msPerUnit/rate.value
currentDelay = 0

display = new DelayedFormattedString( '%d/%s', 200, rate.value, unit.value )
randomDisplay = new DelayedFormattedString( '%d %s', 200, factor.value, "%" )

random = new Random()

timer = new Timer(true)

future = null

onRelease = {  display.release() }

schedule = {
	if (rate.value > 0 && stateProperty.value) {
		if (factor.value > 0) {
			if ( type.value == 'Uniform' ) 
				currentDelay = defaultDelay + ((random.nextDouble() - 0.5) * (factor.value / 100) * defaultDelay)
			if ( type.value == 'Gaussian' )  
				currentDelay = defaultDelay + (random.nextGaussian() * (factor.value / 100) * defaultDelay * 0.3)
			if ( type.value == 'Exponential' ) 
				currentDelay = defaultDelay * (-Math.log(1-(random.nextDouble())))
		} else
			currentDelay = defaultDelay
		
		//if (currentDelay.intValue() == 0)
		//	currentDelay = 1
		
		
		future = timer.runAfter(currentDelay.intValue()) {
			trigger()
			schedule()
		}
	}
}

addEventListener( PropertyEvent ) { event ->
	if ( event.event == PropertyEvent.Event.VALUE ) {
		
		if (event.property == stateProperty && !stateProperty.value)
			future?.cancel()
		if (stateProperty.value)
			future?.cancel()
		
		if( event.property == unit ) {
			if ( unit.value == "Sec" )
				msPerUnit = 1000
			if ( unit.value == "Min" )
				msPerUnit = 60000
			if ( unit.value == "Hour" )
				msPerUnit = 3600000
		}
		if (rate.value > 0)
			defaultDelay = msPerUnit/rate.value
		display.setArgs(rate.value, unit.value)
		randomDisplay.setArgs(factor.value, "%")
		schedule()
	}
}

addEventListener( ActionEvent ) { event ->
	if ( event.key == "STOP" ) {
		future?.cancel();
	}
	
	if ( event.key == "START" ) {
		future?.cancel()
		schedule()
	}
	
	//RESET in this case would not really do anything
}

//Layout
layout { 
	property( property:rate, label:'Base Rate', min:0 ) 
	separator( vertical:true )
	property( property:unit, label:'Unit', options:['Sec','Min','Hour'] )
	separator( vertical:true )
	
	node(widget: 'selectorWidget', label:'Distribution', labels:['Uniform','Exponential','Gaussian'], 
			images:['linear_shape.png', 'poisson_shape.png', 'gauss_shape.png'], default: type.value, selected: type)
	
	separator( vertical:true )
	property( property:factor, label:'Random\nFactor', min: 0, max: 100, step: 1 )
	separator( vertical:true )
	box( widget:'display', constraints:'align center') {
		node( label:'current rate', fString:display )
		node( label:'random', fString:randomDisplay )
	}
}

//Settings
//settings( label: "Settings", constraints: 'wrap 2' ) {
//	box(constraints:"growx, wrap 1") {
//		property( property:rate, label:'Base Rate', min:0 ) 
//		property( property:unit, label:'Unit', options:['Sec','Min','Hour'] )
//		property( property:type, label:'Distribution', options:['Uniform','Exponential','Gaussian'] )
//		property( property:factor, label:'Random Factor', min: 0, max: 100, step: 1 )
//	}
//} 

if (running)
	schedule();
