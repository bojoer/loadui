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
package com.eviware.loadui.fx.summary;

public class SummaryButtonGroup {

	var buttonList: SummaryButton[] = [];
	
	public function add(button: SummaryButton){
		insert button into buttonList;
	}
	
	public function remove(button: SummaryButton){
		delete button from buttonList;
	}
	
	public function unselect(){
		for(button in buttonList){
			button.selected = false;
		}
	}
}
