package com.eviware.loadui.components.soapui.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SwingFXUtils2
{
	@SuppressWarnings( "null" )
	public static BufferedImage toBufferedImageUnchecked( Image awtImage )
	{
		Method getBufferedImage = null;
		try
		{
			getBufferedImage = awtImage.getClass().getMethod( "getBufferedImage" );
		}
		catch( NoSuchMethodException | SecurityException e )
		{
			e.printStackTrace();
		}
		BufferedImage bufferedImage = null;
		try
		{
			bufferedImage = ( BufferedImage )getBufferedImage.invoke( awtImage );
		}
		catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e )
		{
			e.printStackTrace();
		}
		return bufferedImage;
	}
}
