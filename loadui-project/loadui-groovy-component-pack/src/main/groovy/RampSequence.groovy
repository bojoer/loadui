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
createProperty( 'peakRateUnit', String, 'sec' ) { calculateAcceleration(); redraw() }

future = null
startTime = 0
calculateAcceleration()

onAction( 'START' ) {
	calculateAcceleration()
	startTime = currentTime()
	hasPeaked = false
	scheduleNext()
}

onAction( 'STOP' ) {
	future?.cancel( true )
	cancellingFuture?.cancel( true )
	startTime = null
}

scheduleNext = {
	def t0 = getT0()
	
	if( t0 >= rampLength.value && !hasPeaked ) {
		hasPeaked = true
		def delay = 1000000/peakRate.value
		future = scheduleAtFixedRate( { trigger() }, delay, delay, TimeUnit.MICROSECONDS )
		cancellingFuture = schedule( {
			future?.cancel( true )
			a = a*-1
			scheduleNext( rampLength.value )
		}, peakLength.value, TimeUnit.SECONDS )
	} else if( t0 >= 0 ) {
		t1 = Math.sqrt( 2/a + t0**2 )
		
		future?.cancel( true )
		def diff = Math.abs( t1 - getT0() )
		future = schedule( {
				trigger()
				scheduleNext( t1 )
			}, ( diff*1000000) as long, TimeUnit.MICROSECONDS )
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

def redraw() {
	def rate = peakRateUnit.value == 'sec' ? a*getT0() : a*getT0()*60
	layout {
		property( property:rampLength, label:'Ramp Duration\n(sec)', min:1 )
		property( property:peakLength, label:'Peak Duration\n(sec)', min:0 )
		separator( vertical:true )
		property( property:peakRate, label:'Peak Rate\n(VU/' + peakRateUnit.value + ')', min:1 )
		property( property:peakRateUnit, label:'Unit', options:['sec','min'] )
		separator( vertical:true )
		box( widget:'display' ) {
			node( label:'Rate', content: { if( getT0() > 0 ) String.format( '%7.1f', rate ) else 0 }, constraints:'w 45!' )
		}
	}
}
redraw()

def currentTime() {
	System.currentTimeMillis() / 1000
}

def calculateAcceleration() {
	if( peakRateUnit.value == 'sec' )
		a = peakRate.value / rampLength.value
	else
		a = (peakRate.value/60) / rampLength.value
}
