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
package com.eviware.loadui.launcher.api;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;

public class SplashController
{
	private static JWindow window;

	public static void openSplash()
	{
		if( window == null )
		{
			window = new JWindow();
			Container contentPane = window.getContentPane();

			ImageIcon image;
			try
			{
				Class<?> awtUtilitiesClass = Class.forName( "com.sun.awt.AWTUtilities" );

				Method mSetWindowOpaque = awtUtilitiesClass.getMethod( "setWindowOpaque", Window.class, boolean.class );
				mSetWindowOpaque.invoke( null, window, false );

				image = new ImageIcon( new File( "res/loadui-splash.png" ).toURI().toURL() );
			}
			catch( Exception e )
			{
				System.out.println( "Unable to create transparent window, using non-transparent splash: " + e.getMessage() );
				try
				{
					image = new ImageIcon( new File( "res/loadui-splash-no-transparency.png" ).toURI().toURL() );
				}
				catch( MalformedURLException e1 )
				{
					image = new ImageIcon();
				}
			}

			JLabel label = new JLabel( image );
			contentPane.add( label );
			window.pack();

			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension labelSize = label.getPreferredSize();
			window.setLocation( screenSize.width / 2 - ( labelSize.width / 2 ), screenSize.height / 2
					- ( labelSize.height / 2 ) );

			window.setVisible( true );
		}
	}

	public static void closeSplash()
	{
		java.util.logging.Logger.getLogger( SplashController.class.getName() ).info( "Closing splash..." );

		if( window != null )
		{
			window.dispose();
			window = null;
		}
	}
}
