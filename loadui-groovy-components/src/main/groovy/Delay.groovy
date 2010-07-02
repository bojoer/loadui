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
 * Delays incoming messages for a period of time. Delay can be fixed or random. Random delay could be determined by
 * uniform or gaussian distribution. Number of random delay could be also set.
 * 
 * @help http://www.loadui.org/Flow-Control/delay.html
 * @category flow
 * @nonBlocking true
 * @dependency commons-math:commons-math:1.2
 */
 
 import com.eviware.loadui.api.events.PropertyEvent
 
 import java.util.concurrent.Executors
 import java.util.concurrent.TimeUnit
 import com.eviware.loadui.api.events.ActionEvent
 import org.apache.commons.math.random.UniformRandomGenerator
 import org.apache.commons.math.random.RandomAdaptor
 import org.apache.commons.math.random.JDKRandomGenerator
 import com.eviware.loadui.util.layout.DelayedFormattedString
 
 display = new DelayedFormattedString( ' %d /ms ', 500, 0 )
 
 output = createOutput( 'output', "Message Output" )
 
 createProperty('delay', Long, 0)
 createProperty('selected', String, 'none')
 createProperty('randomDelay', Integer, 0)
 
random = new RandomAdaptor(new JDKRandomGenerator())
uniformRandom = new UniformRandomGenerator(random)

executor = Executors.newSingleThreadScheduledExecutor()
 
 onMessage = { incoming, outgoing, message ->
    
    delayIsRandom = random.nextInt(101) > randomDelay.value
    
    if ( selected.value == 'none'  ) {
        message.put("actualDelay", delay.value )
        executor.schedule( { 
                 send( output, message);
                       display.setArgs( message.get("actualDelay") ) }, delay.value, TimeUnit.MILLISECONDS ) 
    }
    if ( selected.value == 'Gauss' && delayIsRandom ) {
        tmpDelay = Math.abs( (int)(random.nextGaussian() * delay.value) )
        message.put("actualDelay", tmpDelay )
        executor.schedule( { send( output, message);
                             display.setArgs( message.get("actualDelay") )  }, tmpDelay, TimeUnit.MILLISECONDS )
    }
    if ( selected.value == 'Uniform' && delayIsRandom ) {
        tmpDelay = Math.abs( (int)(uniformRandom.nextNormalizedDouble()/Math.sqrt(3)) * delay.value )
        message.put("actualDelay", tmpDelay )
        executor.schedule( { send( output, message);
                             display.setArgs( message.get("actualDelay") ) }, tmpDelay, TimeUnit.MILLISECONDS ) 
    }
 }
 
 onRelease = {
   display.release()
   executor.shutdownNow()
 }

 addEventListener( ActionEvent ) { event ->
	if ( event.key == "STOP" ) {
		executor.shutdownNow()
	}
	
	if ( event.key == "START" ) {
		executor = Executors.newSingleThreadScheduledExecutor()
	}
	
	if ( event.key == "RESET" ) {
	    display.setArgs(0)
	}
 }

 layout (constraints:'center') { 
    property( property:delay, label:"Delay(ms)", min:0, step:100, span:60000 ) 
    separator( vertical:true )
    node(widget: 'selectorWidget', labels:["none", "Gauss", "Uniform"], default: selected.value, selected: selected)
    property( property: randomDelay, label:'Random(%)', min:0, max: 100 )
    separator( vertical:true )
    box( widget:'display',  constraints:'w 100!' ) {
        node( label:'delay ', fString:display, constraints:'wrap' )
    }
 }
