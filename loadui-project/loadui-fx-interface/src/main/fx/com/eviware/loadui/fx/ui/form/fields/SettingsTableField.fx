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
package com.eviware.loadui.fx.ui.form.fields;

/**
 * @author robert
 */
 
import javafx.scene.CustomNode;
import javafx.scene.Node;
import javafx.util.Math;
import javafx.ext.swing.SwingComponent;

import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.serialization.Value;
import com.eviware.loadui.api.ui.table.SettingsTableModel;
import com.eviware.loadui.api.ui.table.SettingsTableModel.PropertyProxy;

import java.io.File;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXTable;
import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelListener;

import org.jdesktop.swingx.table.TableColumnExt;

public function build( id:String, label:String, value:Object ) {
    SettingsTableField { id:id, label:label, value:value }
}

public class SettingsTableField extends CustomNode, FormField {
    
    public-init var onTableUpdate:function():Void;
    public-init var data:HashMap;
    def model = new SettingsTableModel();
    def onUpdateListener = new OnUpdateListener();
    
//    public var dataHashCode = model.hashCode();
    
    var pane:JScrollPane;
    
    override public function create():Node {
    	model.addTableModelListener( onUpdateListener );
    	
    	for( propertyProxy in data.values()[p | not ((p as PropertyProxy).getName() as String).startsWith("_")] ){ 
           	model.addRow(propertyProxy as PropertyProxy);
        }

        var table: JXTable = new JXTable(model);
        table.setEditable(true);
        def ce = new DefaultCellEditor(new JTextField());
        for(i in [0..table.getColumnCount()-1]){
        	table.getColumnExt(i).setEditable(true);
        	table.getColumnExt(i).setCellEditor(ce);
        }
        table.setIntercellSpacing(new Dimension( 10, 0 ));
        table.setAutoCreateColumnsFromModel(true);
        table.setVisibleRowCount(2);
        table.setHorizontalScrollEnabled(true);
        table.setPreferredScrollableViewportSize(new Dimension(400, 220));

        SwingComponent.wrap( pane = new JScrollPane(table));
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
    	Math.max( pane.getPreferredSize().getHeight(), 220 )
    }
            
    override public function getPrefWidth(height) {
    	Math.max( pane.getPreferredSize().getWidth(), 400 )
    }
}


class OnUpdateListener extends TableModelListener {
	override function tableChanged(e)
	{
		onTableUpdate();
	}
}
