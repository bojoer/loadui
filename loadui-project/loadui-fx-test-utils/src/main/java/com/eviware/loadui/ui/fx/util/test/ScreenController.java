package com.eviware.loadui.ui.fx.util.test;

import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

public interface ScreenController
{
	public Point2D getMouse();

	public void move( double x, double y );

	public void press( MouseButton button );

	public void release( MouseButton button );

	public void press( KeyCode key );

	public void release( KeyCode key );
}
