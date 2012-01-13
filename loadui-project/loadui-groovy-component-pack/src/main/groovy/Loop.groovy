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
 * Loops a part of your test a certain number of times
 *
 * @id com.eviware.Loop
 * @help http://www.loadui.org/Flow-Control/loop-component.html
 * @category flow
 * @nonBlocking true
 */

createProperty( 'iterations', Long, 0 )

incomingTerminal.description = 'Recieved VUs will loop a set number of times.'
createOutgoing( 'loop' )
loop.label = 'Continue loop'
loop.description = 'VUs will be directed here until they have passed the Loop component the set number of times'
createOutgoing( 'exit' )
exit.label = 'Exit loop'
exit.description = 'Once a VU has passed the loop the set number of times, it will exit from here'

def loopCounter = 'LoopCount_'+id
def completedCount = 0

onAction( "RESET" ) { completedCount = 0 }

onMessage = { incoming, outgoing, message ->
    def count = (message[loopCounter] ?: 0) + 1
    if( count > iterations.value ) {
        message.remove( loopCounter )
        send( exit, message )
        completedCount++
    } else {
        message[loopCounter] = count
        send( loop, message )
    }
}

layout {
    property( property: iterations, label: 'Loop Count', min: 0 )
    separator( vertical:true )
    box( widget: 'display', layout: 'wrap 1' ) {
        node( label: 'Loop Count', content: { iterations.value }, constraints: 'wmin 70' )
        node( label: 'Completed', content: { completedCount }, constraints: 'wmin 70' )
    }
}

compactLayout {
    box( widget: 'display', layout: 'wrap 2' ) {
        node( label: 'Loops', content: { iterations.value }, constraints: 'wmin 40' )
        node( label: 'Compl.', content: { completedCount }, constraints: 'wmin 40' )
    }
}