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
 * Ramps up, holds steady and then ramps down.
 *
 * @id com.eviware.MultiRamp
 * @name Ramp Sequence
 * @category generators
 * @help http://loadui.org/Generators/ramp-sequence.html
 * @nonBlocking true
 */

import java.util.concurrent.TimeUnit

createProperty( 'rampLength', Long, 10 ) { calculateAcceleration() }
createProperty( 'peakRate', Long, 10 ) { calculateAcceleration() }
createProperty( 'peakLength', Long, 10 )
createProperty( 'peakRateUnit', String, 'sec' ) { calculateAcceleration() }

future = null
cancellingFuture = null
startTime = 0
triggersSent = 0
calculateAcceleration()

onAction( 'START' ) {
	calculateAcceleration()
	startTime = currentTime()
	hasPeaked = false
	scheduleNext( startTime )
	triggersSent = 0
}

onAction( 'STOP' ) {
	future?.cancel( true )
	cancellingFuture?.cancel( true )
	startTime = null
}

scheduleNext = { wakeTime ->
	def t0 = getT0()
	
//	println( "Too late with: " + wakeTime - getT0() )
//	println( "Missed triggers: " + a*(t0**2 - wakeTime**2)/2 )
	
	if( t0 >= rampLength.value && !hasPeaked ) {
		hasPeaked = true
		triggersSent = 0
		def delay = 1000000/peakRate.value
		if( peakRateUnit.value == 'min' )
			delay = 1000000/(peakRate.value/60)
		future = scheduleAtFixedRate( { trigger() }, delay, delay, TimeUnit.MICROSECONDS )
		cancellingFuture = schedule( {
			future?.cancel( true )
			a = a*-1
			scheduleNext( rampLength.value )
		}, peakLength.value, TimeUnit.SECONDS )
	} else if( t0 >= 0 ) {
		def triggersThatShouldHaveBeenSent = 0
		if( hasPeaked ) {
			triggersThatShouldHaveBeenSent = Math.floor( a*t0**2/2 - a*rampLength.value**2/2 )
		}
		else
			triggersThatShouldHaveBeenSent = Math.floor( a*t0**2/2 )
		
		while( triggersSent < triggersThatShouldHaveBeenSent ) {
			trigger()
			triggersSent++
		}
	
		t1 = Math.sqrt( 2/a + t0**2 )
		future?.cancel( true )
		def diff = Math.abs( t1 - getT0() )
		if( !Double.isNaN( diff ) ) {
			future = schedule( {
					trigger()
					triggersSent++
					scheduleNext( t1 )
				}, ( diff*1000000) as long, TimeUnit.MICROSECONDS )
		}
	}
}

def getT0() {
	if( !startTime ) return 0
	relativeTime = currentTime() - startTime
	if( relativeTime >= rampLength.value + peakLength.value )
		return startTime + rampLength.value*2 + peakLength.value - currentTime()
	if( relativeTime >= rampLength.value )
		return rampLength.value
	return relativeTime
}

layout {
	property( property:rampLength, label:'Ramp Duration\n(sec)', min:1 )
	property( property:peakLength, label:'Peak Duration\n(sec)', min:0 )
	separator( vertical:true )
	property( property:peakRate, label:'Peak Rate\n(VU/' + peakRateUnit.value + ')', min:1 )
	property( property:peakRateUnit, label:'Unit', options:['sec','min'] )
	separator( vertical:true )
	box( widget:'display' ) {
		node( label:'Rate', content: { if( getT0() > 0 ) String.format( '%7.1f', peakRateUnit.value == 'sec' ? a*getT0() : a*getT0()*60 ) else 0 }, constraints:'w 45!' )
	}
}

compactLayout {
	box( widget:'display' ) {
		node( label:'Rate', content: { if( getT0() > 0 ) String.format( '%7.1f', peakRateUnit.value == 'sec' ? a*getT0() : a*getT0()*60 ) else 0 }, constraints:'w 45!' )
	}
}

def currentTime() {
	System.currentTimeMillis() / 1000
}

def calculateAcceleration() {
	if( peakRateUnit.value == 'sec' )
		a = peakRate.value / rampLength.value
	else
		a = (peakRate.value/60) / rampLength.value
}
