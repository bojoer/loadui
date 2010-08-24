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
*TextField.fx
*
*Created on feb 22, 2010, 12:55:05 em
*/
package com.eviware.loadui.fx.ui.form.fields;

import javafx.scene.control.Label;
import java.lang.IllegalArgumentException;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;

import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.layout.widgets.TimeSpinner;

public function build( id:String, label:String, value:Object ) {
	QuartzCronField { id:id, label:label, value: value }
}

/**
 * A field for entering time for quartz cron trigger.
 *
 * @author predrag
 */
public class QuartzCronField extends HBox, FormField {
	
	override var layoutInfo = LayoutInfo { hfill: true vfill: false hgrow: Priority.ALWAYS vgrow: Priority.NEVER };
	override var padding = Insets { top: 0 right: 0 bottom: 0 left: 0 };
	override var spacing = 2;
	override var nodeVPos = VPos.CENTER;

	def hSpinner:TimeSpinner = TimeSpinner {
		range: 24
		layoutInfo: LayoutInfo { hfill: true vfill: false hgrow: Priority.ALWAYS vgrow: Priority.NEVER}
	}
	
	def mSpinner:TimeSpinner = TimeSpinner {
		range: 60
		layoutInfo: LayoutInfo { hfill: true vfill: false hgrow: Priority.ALWAYS vgrow: Priority.NEVER}
	}
	
	def sSpinner:TimeSpinner = TimeSpinner {
		range: 60
		layoutInfo: LayoutInfo { hfill: true vfill: false hgrow: Priority.ALWAYS vgrow: Priority.NEVER}
		allowAnyTime: false
	}
	
	def hValue = bind hSpinner.value on replace {
		buildValue();
	}

	def mValue = bind mSpinner.value on replace {
		buildValue();
	}
	
	def sValue = bind sSpinner.value on replace {
		buildValue();
	}
	
	var shouldBuild = true;
	
	override var value on replace {
		if( value != null and not ( value instanceof String ) )
			throw new IllegalArgumentException( "Value must be of type String!" );

		parseValue( value as String );
	}
	
	init {
		content = [
			hSpinner,
			Label { text: ":" },
			mSpinner,
			Label { text: ":" },
			sSpinner
		];
	}
	
	function parseValue(total: String):Void {
		def ssmmhh: String[] = total.split(" ");
		shouldBuild = false;
		if(ssmmhh.size() == 3) {
			try {
				hSpinner.value = if( ssmmhh[2] == TimeSpinner.ANY_TIME ) TimeSpinner.ANY_TIME else Integer.valueOf( ssmmhh[2] );
				mSpinner.value = if( ssmmhh[1] == TimeSpinner.ANY_TIME ) TimeSpinner.ANY_TIME else Integer.valueOf( ssmmhh[1] );
				sSpinner.value = if( ssmmhh[0] == TimeSpinner.ANY_TIME ) TimeSpinner.ANY_TIME else Integer.valueOf( ssmmhh[0] );
				shouldBuild = true;
				return;
			} catch( e ) {
			}
		}
		hSpinner.value = 0;
		mSpinner.value = 0;
		sSpinner.value = 0;
		shouldBuild = true;
	}
	
	function buildValue():Void {
		if( shouldBuild ) value = "{sSpinner.value} {mSpinner.value} {hSpinner.value}";
	}
}