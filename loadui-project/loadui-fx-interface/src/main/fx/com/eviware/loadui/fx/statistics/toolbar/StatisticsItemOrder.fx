/* 
 * Copyright 2011 eviware software ab
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
package com.eviware.loadui.fx.statistics.toolbar;

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

import com.eviware.loadui.fx.ui.toolbar.ToolbarItemNode;

//Used for ordering the items
def itemOrder:String[] = [ "response times", "request throughput", "bytes throughput", "request percentile", "assertion failures", "request errors", "running requests"  ];

public class StatisticsItemOrder extends Comparator {
	public override function compare( o1, o2 ) {
		def t1:ToolbarItemNode = o1 as ToolbarItemNode;
		def t2:ToolbarItemNode = o2 as ToolbarItemNode;
		def index1 = Sequences.indexOf( itemOrder, t1.label.toLowerCase() );
		def index2 = Sequences.indexOf( itemOrder, t2.label.toLowerCase() );
	
		if (index1 == index2 )
			t1.label.compareTo( t2.label )
		else
			index1-index2;
	}
}


