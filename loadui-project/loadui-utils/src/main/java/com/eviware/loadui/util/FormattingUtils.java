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
package com.eviware.loadui.util;

import java.text.DecimalFormat;

public class FormattingUtils
{
	private final static DecimalFormat decimalFormat = new DecimalFormat();

	public static String formatTime( long seconds )
	{
		long hours = seconds / 3600;
		seconds %= 3600;
		long minutes = seconds / 60;
		seconds %= 60;

		return String.format( "%02d:%02d:%02d", hours, minutes, seconds );
	}

	public static String formatTimeMillis( long millis )
	{
		long seconds = millis / 1000;
		return String.format( "%s.%03d", formatTime( seconds ), millis % 1000 );
	}

	public static String formatFileName( String base )
	{
		return base.replaceAll( " ", "_" ).replaceAll( "[^a-zA-Z0-9-_.]", "" );
	}

	public static String formatNumber( double number, int maxDecimals )
	{
		decimalFormat.setMinimumFractionDigits( 0 );
		decimalFormat.setMaximumFractionDigits( maxDecimals );
		return decimalFormat.format( number );
	}
}
