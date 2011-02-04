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
package com.eviware.loadui.api.layout;

/**
 * An Exception which is thrown by a WidgetFactory if it is unable to create a
 * Widget for a given LayoutComponent.
 * 
 * @author dain.nilsson
 */
public class WidgetCreationException extends Exception
{
	private static final long serialVersionUID = -6578244192943424664L;

	public WidgetCreationException( String message )
	{
		super( message );
	}

	public WidgetCreationException( Throwable cause )
	{
		super( cause );
	}

	public WidgetCreationException( String message, Throwable cause )
	{
		super( message, cause );
	}
}
