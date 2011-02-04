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
package com.eviware.loadui.util.summary;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalendarUtils
{

	private static final long HOUR = 3600000L;

	public static String getFormattedPeriod( Date startTime, Date endTime )
	{
		Date period = getPeriod( startTime, endTime );
		return format( period );
	}

	public static String format( long time )
	{
		return format( new Date( time ) );
	}

	public static String format( Date date )
	{
		if( date != null )
		{
			SimpleDateFormat dateFormat;
			if( date.getTime() < HOUR )
			{
				dateFormat = new SimpleDateFormat( "00:mm:ss" );
			}
			else
			{
				dateFormat = new SimpleDateFormat( "HH:mm:ss" );
			}
			return dateFormat.format( date );
		}
		else
		{
			return "N/A";
		}
	}

	public static Date getPeriod( Date startTime, Date endTime )
	{
		if( startTime != null && endTime != null )
		{
			Calendar end = Calendar.getInstance();
			end.setTime( endTime );

			Calendar start = Calendar.getInstance();
			start.setTime( startTime );

			end.add( Calendar.YEAR, -start.get( Calendar.YEAR ) );
			end.add( Calendar.MONTH, -start.get( Calendar.MONTH ) );
			end.add( Calendar.DATE, -start.get( Calendar.DATE ) );
			end.add( Calendar.HOUR, -start.get( Calendar.HOUR ) );
			end.add( Calendar.MINUTE, -start.get( Calendar.MINUTE ) );
			end.add( Calendar.SECOND, -start.get( Calendar.SECOND ) );
			end.set( Calendar.MILLISECOND, 0 );

			return end.getTime();
		}
		else
		{
			return null;
		}
	}
}
