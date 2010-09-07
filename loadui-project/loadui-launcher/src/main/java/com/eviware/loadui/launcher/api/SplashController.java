package com.eviware.loadui.launcher.api;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;

import com.sun.jna.platform.WindowUtils;

public class SplashController
{
	private static JWindow window;

	public static void openSplash()
	{
		if( window == null )
		{
			window = new JWindow();
			Container contentPane = window.getContentPane();

			window.setAlwaysOnTop( true );

			ImageIcon image;
			try
			{
				WindowUtils.setWindowTransparent( window, true );
				image = new ImageIcon( SplashController.class.getResource( "/loadui-splash.png" ) );
			}
			catch( Exception e )
			{
				image = new ImageIcon( SplashController.class.getResource( "/loadui-splash-no-transparency.png" ) );
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
		if( window != null )
		{
			window.dispose();
			window = null;
		}
	}
}
