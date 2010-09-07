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

			JLabel label = new JLabel( new ImageIcon( SplashController.class.getResource( "/loadui-splash.png" ) ) );
			Container contentPane = window.getContentPane();
			contentPane.add( label );
			window.pack();

			window.setAlwaysOnTop( true );

			WindowUtils.setWindowTransparent( window, true );

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
