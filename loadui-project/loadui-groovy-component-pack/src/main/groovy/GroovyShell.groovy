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
 * Runs a Groovy Shell
 *
 * @name Groovy Shell
 * @id com.eviware.GroovyShell
 */

import groovy.ui.Console
import java.util.concurrent.TimeUnit

createProperty( '_content', String )

def console = new Console( this.class.classLoader )
console.captureStdOut = false
console.captureStdErr = false

console.run()
console.swing.edt { console.inputArea.text = _content.value }

onRelease = {
	_content.value = console.inputArea.text
	console.exit()
}

scheduleAtFixedRate( { _content.value = console.inputArea.text }, 1, 1, TimeUnit.SECONDS )