package com.eviware.loadui.util;

public class FormattingUtils
{
	public static String formatTime( long seconds )
	{
		long hours = seconds / 3600;
		seconds %= 3600;
		long minutes = seconds / 60;
		seconds %= 60;
		
		return String.format( "%02d:%02d:%02d", hours, minutes, seconds );
	}
}
