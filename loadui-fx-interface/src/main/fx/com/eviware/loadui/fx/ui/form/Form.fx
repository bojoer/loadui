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
/*
*Form.fx
*
*Created on feb 22, 2010, 12:37:59 em
*/

package com.eviware.loadui.fx.ui.form;

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.control.Label;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;

import org.jfxtras.scene.layout.XMigLayout;
import org.jfxtras.scene.layout.XMigLayout.*;
import net.miginfocom.layout.*;

import java.util.HashMap;
import java.util.Map;
import java.lang.RuntimeException;
import java.lang.Class;
import java.io.File;

import com.eviware.loadui.fx.ui.form.fields.*;

import javax.swing.table.TableModel;

function buildFieldTypeMap() {
	def fieldTypeMap = new HashMap();
	fieldTypeMap.put( String.class, TextField.build );
	fieldTypeMap.put( Double.class, DoubleInputField.build );
	fieldTypeMap.put( Float.class, FloatInputField.build );
	fieldTypeMap.put( Long.class, LongInputField.build );
	fieldTypeMap.put( File.class, FileInputField.build );
	fieldTypeMap.put( Boolean.class, CheckBoxField.build );
	fieldTypeMap.put( TableModel.class, TableField.build );
	
	fieldTypeMap
}

def fieldTypeMap = buildFieldTypeMap();

/**
 * Creates and returns a new FormField for the given parameter type, using the given arguments.
 */
public function fieldForType( type:Class, id:String, label:String, value:Object ):FormField {
	(fieldTypeMap.get( type ) as function( id:String, label:String, value:Object ):FormField)( id, label, value ) as FormField;
}

/**
 * A form capable of holding several rows of fields.
 * 
 * @author dain.nilsson
 */
public class Form extends XMigLayout {

	public-init var singleColumn = false;
	
	def fieldMap = new HashMap();
	var fields: FormField[];
	def skippedLabelCC = new CC().span( 2 );
	
	init {
		layoutInfo = LayoutInfo { 
			vfill: true, 
			hfill: true, 
			width: bind width, 
			height: bind height
			hgrow: Priority.SOMETIMES 
			vgrow: Priority.NEVER 
		};
		constraints = new LC().fillX().wrapAfter( if(singleColumn) 1 else 2 ).insets( "0" ).gridGapY("8");
		columns = new AC().index( 1 ).grow().fill();
	}
	
	/**
	 * The content to display in the form.
	 */
	public var formContent: FormItem[] on replace {
		var newContent: Node[] = [];
		fieldMap.clear();
		fields = null;
		
		for( item in formContent ) {
			for( field in item.fields ) {
				def node = field as Node;
				if( not node.id.equals( "" ) and fieldMap.put( node.id, node ) != null )
					throw new RuntimeException( "A Field with that ID already exists!" );
				
				insert field into fields;
				if( field.skipLabel ) {
					insert migNode( node, skippedLabelCC ) into newContent;
				} else {
					def label = Label { 
						text: field.label
					}
					insert [ migNode( label, new CC().minWidth("{label.getPrefWidth(-1)+5}") ), node ] into newContent;
				}
			}
		}
		
		content = newContent;
	}
	
	/**
	 * Gets the FormField which has the given id, or null if no matching FormField is found.
	 */
	public function getField( id:String ):FormField {
		fieldMap.get( id ) as FormField
	}
	
	/**
	 * Gets the value of the FormField which has the given id, or null if no matching FormField is found.
	 */
	public function getValue( id:String ):Object {
		getField( id ).value;
	}
	
	postinit {
	    layout();
	}
}
