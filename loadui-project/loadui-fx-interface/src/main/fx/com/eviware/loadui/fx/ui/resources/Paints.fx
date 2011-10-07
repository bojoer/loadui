/* 
 * Copyright 2011 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
/*
*Paints.fx
*
*Created on feb 10, 2010, 17:02:37 em
*/

package com.eviware.loadui.fx.ui.resources;

import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

public def DIALOG_HEADER = LinearGradient {
    endX: 0
    stops: [
        Stop { offset: 0, color: Color.web("#8F8F8F", .5) }
        Stop { offset: 0.93, color: Color.web("#4D4D4D", 0.5) }
        Stop { offset: 1, color: Color.TRANSPARENT }
    ]
}

public def INACTIVE_MENU = LinearGradient {
    endX: 0
    stops: [
        Stop { offset: 0, color: Color.web("#BBBCBE") }
        Stop { offset: 0.93, color: Color.web("#ACADB0", 0.5) }
        Stop { offset: 1, color: Color.TRANSPARENT }
    ]
}

public def MAIN_MENU = LinearGradient {
	endX: 0
	stops: [
		Stop { offset: 0, color: Color.web("#9a9c9f") }
		Stop { offset: 0.5, color: Color.web("#929497") }
		Stop { offset: 0.9, color: Color.web("#6c6e72") }
		Stop { offset: 0.93, color: Color.web("#000000", 0.5) }
		Stop { offset: 1, color: Color.TRANSPARENT }
	]
}

public def HEADER_GRAY = LinearGradient {
	endX: 0
	stops: [
		Stop { offset: 0, color: Color.web("#606060") }
		Stop { offset: 0.1, color: Color.LIGHTGRAY }
		Stop { offset: 0.2, color: Color.web("#b1b2b3") }
		Stop { offset: 0.55, color: Color.web("#a8aaac") }
		Stop { offset: 0.551, color: Color.web("#9d9ea0") }
		Stop { offset: 0.95, color: Color.web("#9d9ea0") }
		Stop { offset: 1, color: Color.WHITE }
	]
}

public def HEADER_BLUE = LinearGradient {
	endX: 0
	stops: [
		Stop { offset: 0, color: Color.web("#41b3fa") }
		Stop { offset: 0.1, color: Color.LIGHTBLUE }
		Stop { offset: 0.2, color: Color.web("#5fbefa") }
		Stop { offset: 0.55, color: Color.web("#25a8f9") }
		Stop { offset: 0.551, color: Color.web("#029af8") }
		Stop { offset: 0.95, color: Color.web("#029af8") }
		Stop { offset: 1, color: Color.web("#2c88c0") }
	]
}


public def COMPONENT_BAR_HR = LinearGradient {
	endX: 0
	stops: [
		Stop { offset: 0, color: Color.web("#5c5c5c", .1) }
		Stop { offset: 0.69, color: Color.web("#4d4d4d", .5) }
		Stop { offset: 0.7, color: Color.web("#111111", .8) }
		Stop { offset: 0.79, color: Color.web("#111111", .8) }
		Stop { offset: 0.8, color: Color.web("#898989", .8) }
		Stop { offset: 1, color: Color.web("#898989", .5) }
	]
}


public def MENU_HIGHLIGHT = LinearGradient {
	endX: 0
	stops: [
		Stop { offset: 0, color: Color.web("#ffffff", .2) }
		Stop { offset: 0.3, color: Color.web("#ffffff", .5) }
		Stop { offset: 0.8, color: Color.web("#ffffff", .1) }
		Stop { offset: 1, color: Color.web("#ffffff", 0) }
	]
}


public def SCROLLBARHANDLE_VERTICAL = LinearGradient {
	endY: 0
	stops: [
		Stop { offset: 0, color: Color.web("#a2a4a7") }
		Stop { offset: 1, color: Color.web("#818284") }
	]
}

public def SCROLLBARHANDLE_HORIZONTAL = LinearGradient {
	endX: 0
	stops: [
		Stop { offset: 0, color: Color.web("#a2a4a7") }
		Stop { offset: 1, color: Color.web("#818284") }
	]
}

public def SCROLLBARTRACK_VERTICAL = LinearGradient {
	endY: 0
	stops: [
		Stop { offset: 0, color: Color.web("#322e32") }
		Stop { offset: 1, color: Color.web("#535355") }
	]
}

public def SCROLLBARTRACK_HORIZONTAL = LinearGradient {
	endX: 0
	stops: [
		Stop { offset: 0, color: Color.web("#322e32") }
		Stop { offset: 1, color: Color.web("#535355") }
	]
}
