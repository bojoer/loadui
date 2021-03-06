/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.util.summary;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.eviware.loadui.util.FormattingUtils;

public class CalendarUtils
{
	public static String formatInterval( Date startTime, Date endTime )
	{
		return FormattingUtils.formatTime( ( endTime.getTime() - startTime.getTime() ) / 1000 );
	}

	public static String formatInterval( long intervalInMillis )
	{
		return FormattingUtils.formatTime( intervalInMillis / 1000 );
	}

	public static String formatAbsoluteTime( long time )
	{
		return formatAbsoluteTime( new Date( time ) );
	}

	public static String formatAbsoluteTime( Date date )
	{
		if( date != null )
		{
			return new SimpleDateFormat( "HH:mm:ss" ).format( date );
		}
		else
		{
			return "N/A";
		}
	}
}
