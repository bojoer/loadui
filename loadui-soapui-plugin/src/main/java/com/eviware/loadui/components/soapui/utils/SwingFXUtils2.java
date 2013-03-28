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
package com.eviware.loadui.components.soapui.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SwingFXUtils2
{
	public static BufferedImage toBufferedImageUnchecked( Image awtImage )
	{
		Method getBufferedImage = null;
		try
		{
			getBufferedImage = awtImage.getClass().getMethod( "getBufferedImage" );
		}
		catch( NoSuchMethodException | SecurityException e )
		{
			throw new RuntimeException( e );
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
