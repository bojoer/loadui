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
/*
 *  loadUI, copyright (C) 2009 eviware.com 
 *
 *  loadUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  loadUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.loadui.fx.ui.toolbar;

import javafx.scene.CustomNode;
import javafx.scene.Cursor;
import javafx.scene.layout.Resizable;
import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.effect.Glow;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextOrigin;
import javafx.scene.image.ImageView;
import javafx.geometry.Point2D;
import javafx.animation.transition.TranslateTransition;
import javafx.fxd.FXDNode;
import javafx.util.Sequences;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import javafx.util.Math;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.resources.Paints;
import com.eviware.loadui.fx.ui.pagination.Pagination;
import com.eviware.loadui.fx.ui.dnd.DraggableFrame;

//import org.jfxtras.animation.wipe.XWipePanel;
import com.eviware.loadui.fx.ui.XWipePanel;
import org.jfxtras.animation.wipe.SlideWipe;
import org.jfxtras.scene.shape.MultiRoundRectangle;

import javafx.scene.layout.VBox;
import javafx.scene.layout.Panel;
import javafx.scene.layout.Stack;
import javafx.scene.control.Label;
import java.util.Comparator;
import javafx.util.Sequences;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import com.eviware.loadui.api.component.categories.*;

public def GROUP_HEIGHT = 126;

/*
 * Used for ordering toolbar items.
 */

public class ItemOrder extends Comparator {
	
	def loadGeneratorOrder:String[] = [ "FIXED RATE", "VARIANCE", "RANDOM", "RAMP", "USAGE", "FIXED LOAD" ];
	def analysisOrder:String[] = [ "ASSERTION", "STATISTICS" ];
	def flowOrder:String[] = [ "SPLITTER", "DELAY", "CONDITION", "LOOP" ];
	def runnerOrder:String[] = [ "SOAPUI RUNNER", "WEB PAGE RUNNER", "SCRIPT RUNNER", "PROCESS RUNNER" ];
	    	
	public override function compare(o1, o2) {
		 def t1:ToolbarItemNode = o1 as ToolbarItemNode;
		 def t2:ToolbarItemNode = o2 as ToolbarItemNode;
		 
		 if (t1.category.equalsIgnoreCase("VU Generators") and t2.category.equalsIgnoreCase("VU Generators")) {
			var index1 = Sequences.indexOf(loadGeneratorOrder, t1.label.toUpperCase());
		 	var index2 = Sequences.indexOf(loadGeneratorOrder, t2.label.toUpperCase());
		     
		 	if (not (index1 == -1 or index2 == -1))
		 		 return index1-index2;
		 }
		
		 
		 if (t1.category.equalsIgnoreCase("Analysis") and t2.category.equalsIgnoreCase("Analysis")) {
		 	var index1 = Sequences.indexOf(analysisOrder, t1.label.toUpperCase());
		 	var index2 = Sequences.indexOf(analysisOrder, t2.label.toUpperCase());
		 	        
		 	if (not (index1 == -1 or index2 == -1))
		 		return index1-index2;
		 }
		 	    
		if (t1.category.equalsIgnoreCase("Flow") and t2.category.equalsIgnoreCase("Flow")) {
			var index1 = Sequences.indexOf(flowOrder, t1.label.toUpperCase());
			var index2 = Sequences.indexOf(flowOrder, t2.label.toUpperCase());
			        
			if (not (index1 == -1 or index2 == -1))
				return index1-index2;
		}
		
		if (t1.category.equalsIgnoreCase("Runners") and t2.category.equalsIgnoreCase("Runners")) {
			var index1 = Sequences.indexOf(runnerOrder, t1.label.toUpperCase());
			var index2 = Sequences.indexOf(runnerOrder, t2.label.toUpperCase());
			        
			if (not (index1 == -1 or index2 == -1))
				return index1-index2;
		}
	    
	    return o1.toString().compareTo(o2.toString());
	}
}


