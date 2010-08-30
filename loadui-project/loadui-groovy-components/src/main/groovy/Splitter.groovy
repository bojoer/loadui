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
 * Non blocking component which sends incoming messages to several outputs. Two modes:
 * - Random ( choose random output )
 * - Round-Robin method ( going sequential trough all outputs )
 * 
 * @help http://www.loadui.org/Flow-Control/splitter.html
 * @category flow
 * @nonBlocking true
 */
 
 import java.util.concurrent.Executors
 import java.util.concurrent.TimeUnit

 import com.eviware.loadui.api.events.PropertyEvent
 import com.eviware.loadui.util.collections.ObservableList
 import com.eviware.loadui.util.layout.DelayedFormattedString
 import com.eviware.loadui.api.events.ActionEvent
 
 // one output minimum
 
 createProperty('selected', String, "Round-Robin" )
 createProperty('outputs', Integer, 1 )
 createProperty('total', Integer, 0 )
 createProperty('counterUse', Boolean, true )
 createProperty('updateCounterDelay', Long, 500)
 
 // locals

 total.value = 0
 
 def roundRobinNext = 0
 ObservableList outputStats = new ObservableList()
 outputStats.add(0)
 for( i in 1..9 ) {
    outputStats.add(-1)
}
 cnt = 0
 while( outputs.value > cnt ) {
 	createOutgoing()
 	outputStats.set( cnt, 0 )
        cnt++
 }
    
 display = new DelayedFormattedString( '%d', 500, 0 )
 outputDisplay = new DelayedFormattedString( '%s', 500, value({ outputStats.findAll({ it >= 0 }).join('          ') }) )

 executor = Executors.newSingleThreadScheduledExecutor()
 future = executor.scheduleWithFixedDelay( { outputStats.update() }, updateCounterDelay.value, updateCounterDelay.value, TimeUnit.MILLISECONDS ) 

 onMessage = { incoming, outgoing, message ->
 	super.onTerminalMessage(incoming, outgoing, message)
   try {
    def next = 0;
    switch( selected.value ) {
        case "Round-Robin": 
            next = roundRobinNext
            outputStats.set(next, outputStats.get(next) + 1)
            send ( getOutgoingTerminalList().get(next), message )
            if( roundRobinNext + 1 == getOutgoingTerminalList().size() )
                roundRobinNext = 0
            else
                roundRobinNext++
            break
        case "Random" :
            random = new Random()
            next = random.nextInt(getOutgoingTerminalList().size())
            outputStats.set(next, outputStats.get(next) + 1)
            send ( getOutgoingTerminalList().get(next), message )
            break
    }
    total.value++
    display.setArgs( total.value )
   } catch ( Exception e ) {
 	println e.printStackTrace   
   }

} 
 addEventListener( PropertyEvent ) { event ->
    if( event.event == PropertyEvent.Event.VALUE ) {
        switch( event.getProperty().getKey() ) {
            case 'outputs': 
                while ( outputs.value != getOutgoingTerminalList().size() ) {
                    if ( outputs.value > getOutgoingTerminalList().size() ) {
                        createOutgoing()
                        outputStats.set(getOutgoingTerminalList().size() -1, 0)
                    } else {
                        total.value = total.value - outputStats.get(getOutgoingTerminalList().size() -1)
                        display.setArgs( total.value )
                        outputStats.set(getOutgoingTerminalList().size() -1, -1)
                        deleteOutgoing()
                    }
                }
    		outputStats.update()
                break;
        }
    }
 }
 
 addEventListener( ActionEvent ) { event ->
	if ( event.key == "STOP" ) {
	  executor.schedule( { 
		executor?.shutdownNow()
	  	executor = Executors.newSingleThreadScheduledExecutor()
	  }, updateCounterDelay.value, TimeUnit.MILLISECONDS)
	}
	
	if ( event.key == "START" ) {
           if ( future == null )
	    future = executor.scheduleWithFixedDelay( { outputStats.update() }, updateCounterDelay.value, updateCounterDelay.value, TimeUnit.MILLISECONDS ) 
	}
	
	if ( event.key == "RESET" ) {
	    future?.cancel(true)
	    executor?.shutdownNow()
	    roundRobinNext = 0
	    selected.value = "Round-Robin"
	    total.value = 0
	    display.setArgs( 0 )
            for( i in 0..9 ) {
	     if( outputStats.get(i) > -1 )
	          outputStats.set(i as Integer,0)
	    }
	    outputStats.update()
	    executor = Executors.newSingleThreadScheduledExecutor()
	    future = executor.scheduleWithFixedDelay( { outputStats.update() }, updateCounterDelay.value, updateCounterDelay.value, TimeUnit.MILLISECONDS ) 
	}
}

 onRelease = {
   display.release()
	outputDisplay.release()
   future.cancel(true)
   executor.shutdownNow()
 }

 
 settings( label: "Counter Settings", layout: 'wrap 2' ) {
	box( layout:"wrap 1", constraints:"growx" ) {
		property(property: counterUse, label: 'Enable Counters' )
		property(property: updateCounterDelay, label: 'Time interval for refreshing counters(ms)' )
	}
 }
 
 layout ( layout:'center' ) { 
    node(widget: 'selectorWidget', label:"Type", labels:["Round-Robin", "Random"], default: "Round-Robin", selected: selected)
    separator( vertical: true )
    node( widget: 'sliderWidget', property: outputs, constraints:'center, w 270!' )
    separator( vertical: true ) 
    box( layout: 'wrap, ins 0' ) {
	    box( widget:'display',  constraints:'w 100!' ) {
			 node( label:'Count', fString:display, constraints:'wrap' )
	    }
	    action( label:'Clear', action: {  
	       total.value = 0
		    display.setArgs( 0 )
			 for( i in 0..9 ) {
			     if( outputStats.get(i) > -1 ) outputStats.set(i as Integer,0)
			 }
			 outputStats.update()
	    }, constraints:'right' )
	}
    separator( vertical: false )
    node( widget: 'counterWidget', counters: outputStats , onOff: counterUse, constraints:'span 5,center')
  }
 
compactLayout() {
	box( widget: 'display', layout: 'wrap, fillx', constraints: 'growx' ) {
		node( label: 'Count', fString: display )
		node( label: 'Distribution', fString: outputDisplay )
	}
}