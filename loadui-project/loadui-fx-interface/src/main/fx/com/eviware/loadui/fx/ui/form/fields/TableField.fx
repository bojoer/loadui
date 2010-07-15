/* 
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.fx.ui.form.fields;

import java.lang.IllegalArgumentException;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;

import javax.swing.table.TableModel;

import javafx.scene.CustomNode;
import javafx.scene.Node;
import javafx.ext.swing.SwingComponent;

import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.serialization.Value;

import java.io.File;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXTable;
import java.awt.Dimension;
import java.util.HashMap;

import java.util.Observable;
import java.util.Observer;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import com.eviware.loadui.api.ui.table.SettingsTableModel;

/**
 * Constructs a new TableField using the supplied arguments.
 */
public function build(id: String, label: String, value: Object) {
	TableField { id: id, label: label, value: value }
}

/**
 * A table field FormField.
 *
 * @author predrag.vucetic
 */
public class TableField extends CustomNode, FormField, Observer {

    public var model: TableModel;
    
    public var buttons: Node[];

	override var value on replace {
		if( value != null and not ( value instanceof TableModel ) )
			throw new IllegalArgumentException( "Value must be of type TableModel!" );
		model = value as TableModel;
		table.setModel(model);
	}
	
    public var dataHashCode = model.hashCode();
    
    var table: JXTable;
    var node: Node;
    
    override var width = 400;
    override var height = 220;
    
    override public function create(): Node {
        table = new JXTable(model);
        table.setIntercellSpacing(new Dimension( 10, 0 ));
        table.setAutoCreateColumnsFromModel(true);
        table.setVisibleRowCount(2);
        table.setHorizontalScrollEnabled(true);
        table.setPreferredScrollableViewportSize(new Dimension(width, height - 40));
        node = SwingComponent.wrap(new JScrollPane(table));
        VBox{
        	padding: Insets { top: 0 right: 0 bottom: 0 left: 0}
        	spacing: 2	
        	content: [
        		HBox{
        			spacing: 5
        			content: bind buttons
        		}
        		node
        	]
        }
    }
    
 /*   function apply() {
        var cnt = 0;
        while( cnt < sizeof table.data ) {
            var row = table.data[cnt];
            var prop = data[cnt];
            if( prop.getKey().equals(row.cells[0].cell) ) {
                var value = row.cells[1].cell;
                if ( prop.getType() == Long.class ) {
                    (prop as com.eviware.loadui.api.serialization.Value).setValue( Long.valueOf(value) );
                } else if ( prop.getType() == Double.class ) {
                    (prop as com.eviware.loadui.api.serialization.Value).setValue( Double.valueOf(value) );
                } else if ( prop.getType() == Boolean.class ) {
	                (prop as com.eviware.loadui.api.serialization.Value).setValue( value );
	            } else if ( prop.getType() == File.class ) {
                    (prop as com.eviware.loadui.api.serialization.Value).setValue( new File( value ) );
                } else {
                    (prop as com.eviware.loadui.api.serialization.Value).setValue( value );
                }
            }
            cnt++;
        }
    }*/
    
    override public function getPrefHeight(width) {
        height;
    }
            
    override public function getPrefWidth(height) {
        width;
    }
    
    override function update(observable: Observable, arg: Object) {
         FX.deferAction(
             function(): Void {
                 dataHashCode = model.hashCode();
             }
         );
     }
}
