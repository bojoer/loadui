/*
*SwingLayoutComponentNode.fx
*
*Created on dec 16, 2011, 14:23:45 em
*/

package com.eviware.loadui.fx.ui.layout;

import javafx.scene.layout.Stack;
import javafx.ext.swing.SwingComponent;
import javax.swing.JComponent;

import java.awt.Dimension;

public class SwingLayoutComponentNode extends LayoutComponentNode {
	
	public-init var component:JComponent;
	
	def stack = Stack {
		override var width on replace {
			component.setPreferredSize( new Dimension( width, height ) );
		}
		
		override var height on replace {
			component.setPreferredSize( new Dimension( width, height ) );
		}
		content: SwingComponent.wrap( component )
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
