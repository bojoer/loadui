/*
*SwingLayoutComponentNode.fx
*
*Created on dec 16, 2011, 14:23:45 em
*/

package com.eviware.loadui.fx.ui.layout;

import javafx.scene.layout.Stack;
import javafx.ext.swing.SwingComponent;
import javax.swing.JComponent;

import com.sun.javafx.scene.layout.Region;

import java.awt.Dimension;

public class SwingLayoutComponentNode extends LayoutComponentNode {
	
	public-init var component:JComponent;
	public var fixedHeight:Number = -1;
	public var fixedWidth:Number = -1;
	
	def stack:Stack = Stack {
		override var width on replace {
			component.setPreferredSize( new Dimension( getWidth(), getHeight() ) ); 
		}
		
		override var height on replace {
			component.setPreferredSize( new Dimension( getWidth(), getHeight() ) );
		}
		content: SwingComponent.wrap( component ) 
	}
	
	init {
		stack.blocksMouse = true;
	}
	
	function getHeight()
	{
		if (fixedHeight == -1) height else fixedHeight;
	}
	
	function getWidth()
	{
		if (fixedWidth == -1) width else fixedWidth;
	}
	
	override function getPrefHeight( width:Float ) {
		stack.height;
	}
	
	override function getPrefWidth( height:Float ) {
		stack.width;
	}
	
	override function create() {
		stack
	}

};
