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
package com.eviware.loadui.util.hacks;

@SuppressWarnings( "serial" )
public class PreventClosingStageException extends Exception
{
	private static final String message = "This is hack exception that does nothing. It is used to prevent JavaFx stage from closing.";

	@Override
	public String getMessage()
	{
		return message;
	}

	@Override
	public void printStackTrace()
	{
		System.out.println( message );
	}

	@Override
	public StackTraceElement[] getStackTrace()
	{
		return null;
	}
}
