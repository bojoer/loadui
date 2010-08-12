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
/*
*CreateNewProjectDialog.fx
*
*Created on feb 10, 2010, 13:06:43 em
*/

package com.eviware.loadui.fx.dialogs;

import javafx.scene.layout.LayoutInfo;
import javafx.scene.text.Text;

import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.form.fields.*;

import com.eviware.loadui.api.model.WorkspaceItem;
import java.io.File;
import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;
import java.lang.Thread;
import java.lang.Throwable;

import com.eviware.loadui.util.BeanInjector;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.io.Buffer;
import java.lang.Exception;
import org.eclipse.jetty.http.HttpSchemes;
import com.eviware.loadui.api.http.HttpClientProvider;
import com.eviware.loadui.fx.http.HttpClientHolder;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.dialogs.CreateNewAgentDialog" );

/**
 * Asks the user for a name and file for a new AgentItem to be created.
 */
public class CreateNewAgentDialog {
	/**
	 * The currently loaded WorkspaceItem. This needs to be set during initialization.
	 *
	 * @author dain.nilsson
	 */
	public-init var workspace: WorkspaceItem;
	
	var dialog: Dialog;

	var form:Form;
	var agentName: TextField;
	var agentUrl: TextField;
	
	function ok():Void  {
					if(not validateName(agentName.value as String)){
						msgDialog.title = "Agent name invalid";
						msgDialog.content = [Text{content: "Agent name '{agentName.value}' is not valid! Must be unique non zero length string."}];
						msgDialog.show();
						return;
					}
					if(not validateURL()){
						msgDialog.title = "Agent URL invalid";
						msgDialog.content = [Text{content: "URL '{agentUrl.value}' is invalid!"}];
						msgDialog.show();
						return;
					}
					if(not validateAgentAlreadyExist()){
						msgDialog.title = "Agent already in workspace";
						msgDialog.content = [Text{content: "Agent at address '{agentUrl.value}' already exists in workspace!"}];
						msgDialog.show();
						return;
					}
					
					validateAgent(); 
				}
	
	postinit {
		if( not FX.isInitialized( workspace ) )
			throw new RuntimeException( "Workspace is null!" );
		
		dialog = Dialog {
			title: "Add new agent"
			content: form = Form {
				layoutInfo: LayoutInfo { width: 250 }
				formContent: [
					agentName = TextField { label: "Agent Name", action: ok },
					agentUrl = TextField { label: "Agent URL", action: ok }
				]
			}
			okText: "Add"
			onOk: ok
		}
		
		var c = 1;
		while( not validateName( "Agent {c}" ) ) {
			c++;
		}
		agentName.value = "Agent {c}";
		agentUrl.value = "";
	}
	
	def msgDialog: Dialog = Dialog {
		title: ""
		okText: "Ok"
		noCancel: true
		showPostInit: false
	}
	
	def confirmDialog: Dialog = Dialog {
		title: "Add new agent"
		content: [
			Text { content: "No Agent detected at specified address, add anyway?" }
		]
		okText: "Ok"	
		onOk: function(){
			log.debug( "Creating new Agent: '\{\}'  with URL: '\{\}'", agentName.value, agentUrl.value );
			workspace.createAgent(agentUrl.value as String, agentName.value as String);
			confirmDialog.close();
			dialog.close();
		}
		showPostInit: false
	}
		
	function validateURL(): Boolean {
		def dn: String = "[A-Za-z0-9\\.-]\{3,\}";
		def ip: String = "([0-9]\{1,3\}\\.)\{3\}[0-9]\{1,3\}";
		
		var url: String = agentUrl.value as String;
		if(url == null or url.length() == 0){
			return false;
		}
		
		if(url.matches("^{dn}$") or url.matches("^{ip}$")){
			//no port, no protocol, ip or domain name
			agentUrl.value = "https://{url}:8443"; 
			return true
		}
		else if(url.matches("^{dn}:[0-9]\{1,5\}/?$") or url.matches("^{ip}:[0-9]\{1,5\}/?$")){
			//port, no protocol, ip or domain name
			url = "https://{url}";
			if(url.endsWith("/")){
				url = url.substring(0, url.length() - 1);
			}
			agentUrl.value = url; 
			return true
		}
		else if(url.matches("^https?://{dn}/?$") or url.matches("^https?://{ip}/?$")){
			//protocol, no port, ip or domain name
			if(url.endsWith("/")){
				url = url.substring(0, url.length() - 1);
			}
			agentUrl.value = "{url}:8443";
			return true;
		}
		else if(url.matches("^https?://{dn}:[0-9]\{1,5\}/?$") or url.matches("^https?://{ip}:[0-9]\{1,5\}/?$")){
			//full
			if(url.endsWith("/")){
				agentUrl.value = url.substring(0, url.length() - 1);
			}
			return true;
		}
		else{
			return false;
		}
	}
	
	var agentValid: Boolean = false;
	
	function validateAgent(): Void {
		try{
			agentValid = false;
			
			def exchange: DataExchange = new DataExchange();
			exchange.setMethod("HEAD");
			
			var url: String = agentUrl.value as String;
			exchange.setURL("{url}/status");
			
			if((agentUrl.value as String).startsWith("https")){
				exchange.setScheme(HttpSchemes.HTTPS_BUFFER);
				HttpClientHolder.instance.getHttpsClient().send(exchange);
			}
			else{
				exchange.setScheme(HttpSchemes.HTTP_BUFFER);
				HttpClientHolder.instance.getHttpClient().send(exchange);
			}
			
			var exchangeState: Integer = exchange.waitForDone();
			if (exchangeState == HttpExchange.STATUS_COMPLETED and agentValid){
				log.debug( "Creating new Agent: '\{\}'  with URL: '\{\}'", agentName.value, agentUrl.value );
				workspace.createAgent(agentUrl.value as String, agentName.value as String);
				dialog.close();
			}
			else{
				confirmDialog.show();
			}
		}
		catch(e: Exception){
			confirmDialog.show();
		}
	}
	
	function validateName( name:String ):Boolean {
		if(name == null or name.length() == 0){
			return false;
		}
		for( agent in workspace.getAgents() ) {
			if( agent.getLabel().equals( name ) )
				return false;
		}
		true;
	}
	
	function validateAgentAlreadyExist(): Boolean {
		var url: String = agentUrl.value as String;
		for(agent in workspace.getAgents()) {
			var agentUrl: String = agent.getUrl(); 
			if(agentUrl.endsWith("/")){
				agentUrl = agentUrl.substring(0, agentUrl.length() - 1);
			}
			if(agentUrl.equals(url)){
				return false;
			}
		}
		true;
	}

}

public class DataExchange extends HttpExchange {

	override function onResponseHeader(name: Buffer, value: Buffer): Void {
		if(name.toString('utf8').equals("Server")){
			def val: String = value.toString('utf8');
			def parts: String[] = val.split(";");
			if(sizeof parts == 2 and parts[0].equals("LoadUI Agent")){
				agentValid = true;
			}
		}
	}
	
	override function onResponseComplete(): Void {
	}
	
	override function onConnectionFailed(x: Throwable){
	}
	
	override function onException(x: Throwable){
	}
	
	override function onExpire(){
	}
}
