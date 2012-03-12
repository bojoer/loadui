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

startTime = 0
calculateAcceleration()

onAction( 'START' ) {
	startTime = currentTime()
	scheduleNext()
}


onAction( 'STOP' ) {
	future?.cancel( true )
}

scheduleNext = {
	t0 = getT0()
	if( t0 > 0 ) {
		t1 = Math.sqrt( 2/a + t0**2 )
		future = schedule( {
				trigger()
				scheduleNext()
			}, ( (t1-t0)*1000 ).intValue(), TimeUnit.MILLISECONDS )
	}
}

def getT0() {
	if( !startTime ) return 0
	relativeTime = currentTime() - startTime
	if( relativeTime >= rampLength.value + peakLength.value )
		return startTime + rampLength.value*2 + peakLength.value - currentTime()
	if( relativeTime >= rampLength.value )
		return rampLength.value
	return currentTime() - startTime
}

layout {
	property( property:rampLength, label:'Ramp Duration\n(s)', min:0 ) 
	property( property:peakRate, label:'Peak Rate', min:1 ) 
	property( property:peakLength, label:'Peak Duration\n(s)', min:0 ) 
	separator( vertical:true )
	box( widget:'display' ) {
		node( label:'Rate', content: { if( getT0() > 0 ) String.format( '%7.1f', a*getT0() ) else 0 }, constraints:'wmin 75' )
	}
}

def currentTime() {
	System.currentTimeMillis() / 1000
}

def calculateAcceleration() {
	a = peakRate.value / rampLength.value
}
