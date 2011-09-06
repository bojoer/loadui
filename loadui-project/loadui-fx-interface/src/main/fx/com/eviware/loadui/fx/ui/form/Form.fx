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
/*
*Form.fx
*
*Created on feb 22, 2010, 12:37:59 em
*/

package com.eviware.loadui.fx.ui.form;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Container;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.LayoutInfo;
import javafx.util.Math;

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
public class Form extends Container {

	public-init var singleColumn = false;
	
	public var margin = 3.0;
	
	def fieldMap = new HashMap();
	def labelMap = new HashMap();
	var fields: FormField[];
	
	public var onCommit: function():Void;
	
	public function commit():Void { onCommit() }

	/**
	 * The content to display in the form.
	 */
	public var formContent: FormItem[] on replace {
		fieldMap.clear();
		labelMap.clear();
		fields = null;
		content = [];
		
		for( item in formContent ) {
			for( field in item.fields ) {
				def node = field as Node;
				if( not node.id.equals( "" ) and fieldMap.put( node.id, node ) != null )
					throw new RuntimeException( "A Field with that ID already exists!" );
				
				insert field into fields;
				if( not field.skipLabel ) {
					def label = Label { text: bind field.label }
					labelMap.put( field, label );
					insert label into content;
				}
				insert node into content;
			}
		}
		
		requestLayout();
	}
	
	var prefWidth:Number = -1 on replace {
		requestLayout();
	}
	var prefHeight:Number = -1 on replace {
		requestLayout();
	}
	
	override function doLayout():Void {
		var labelWidth = 0.0;
		for( label in labelMap.values() ) {
			labelWidth = Math.max( labelWidth, getNodePrefWidth( label as Node ) )
		}
		
		var offsetY = margin;
		var fieldWidth = 0.0;
		var doubleWidth = 0.0;
		var managed = getManaged( content );
		while( sizeof managed > 0 ) {
			def one = managed[0];
			delete managed[0];
			var rowHeight:Number;
			if( labelMap.containsValue( one ) and not singleColumn ) {
				def two = managed[0];
				delete managed[0];
				fieldWidth = Math.max( fieldWidth, getNodePrefWidth( two ) );
				rowHeight = Math.max( getNodePrefHeight( one ), getNodePrefHeight( two ) );
				layoutNode( one, 0, offsetY, labelWidth, rowHeight );
				layoutNode( two, labelWidth + margin, offsetY, width - labelWidth - margin, rowHeight );
			} else {
				rowHeight = getNodePrefHeight( one );
				doubleWidth = Math.max( doubleWidth, getNodePrefWidth( one ) );
				layoutNode( one, 0, offsetY, width, rowHeight );
			}
			offsetY += rowHeight + margin;
		}
		prefHeight = offsetY;
		prefWidth = Math.max( labelWidth + fieldWidth + margin, doubleWidth );
	}
	
	override function getPrefHeight( width:Number ) {
		if( prefHeight == -1 )
			doLayout();
			
		prefHeight;
	}
	
	override function getPrefWidth( height:Number ) {
		if( prefWidth == -1 )
			doLayout();
			
		prefWidth;
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

}
