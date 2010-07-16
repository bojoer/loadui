/* 
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.fx.agents.discovery;

import com.eviware.loadui.api.discovery.AgentDiscovery;
import com.eviware.loadui.api.discovery.AgentDiscovery.*;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.MainWindow;

import java.util.HashSet;
import java.util.Collection;

public-read var instance: AgentDiscoverer;

public class AgentDiscoverer {

	var AgentDiscovery: AgentDiscovery;
	public function setAgentDiscovery(AgentDiscovery: AgentDiscovery):Void { 
		this.AgentDiscovery = AgentDiscovery; 
	}

	function initialize(): Void {
		instance = this;
	}
	
	function destroy(): Void {
		instance = this;
	}
	
	public function getNewAgents(): AgentReference[] {
		var result: AgentReference[] = [];
		if(AgentDiscovery != null){
			var ids = new HashSet();
			var urls = new HashSet();
			var workspace: WorkspaceItem = MainWindow.instance.workspace;
			if(workspace != null){
				for(r in workspace.getAgents()){
					ids.add(r.getId());
					urls.add(r.getUrl());
				}
			}
	
			var discovered: Collection = AgentDiscovery.getDiscoveredAgents();
			for(d in discovered){
				def id = (d as AgentReference).getId();
				def url = (d as AgentReference).getUrl();
				if(not ids.contains(id) and not urls.contains(url)){
					insert d as AgentReference into result;
				}
			}
		}
		result;
	}
	
}


