/*
 * Copyright 2011 eviware software ab
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
package com.eviware.loadui.api.messaging;

import com.eviware.loadui.LoadUI;

/**
 * Exception thrown when attempting to connect two MessageEndpoints with
 * mismatched version strings.
 * 
 * @author dain.nilsson
 */
public class VersionMismatchException extends Exception
{
	private static final long serialVersionUID = 329453281235558858L;

	private final String version;

	public VersionMismatchException( String version )
	{
		super( "Attempted to connect to a MessageEndpoint using a different version of the protocol. Local version: "
				+ LoadUI.AGENT_VERSION + " Remote version: " + version );

		this.version = version;
	}

	public String getMismatchedVersion()
	{
		return version;
	}
}
