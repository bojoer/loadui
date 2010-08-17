package com.eviware.loadui.launcher.api;

import java.awt.Dimension;
import java.awt.Toolkit;

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
			JLabel label = new JLabel( new ImageIcon( SplashController.class.getResource( "/loadui-splash.png" ) ) );
			window.getContentPane().add( label );
			window.pack();

			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension labelSize = label.getPreferredSize();
			window.setLocation( screenSize.width / 2 - ( labelSize.width / 2 ), screenSize.height / 2
					- ( labelSize.height / 2 ) );

			window.setAlwaysOnTop( true );
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
