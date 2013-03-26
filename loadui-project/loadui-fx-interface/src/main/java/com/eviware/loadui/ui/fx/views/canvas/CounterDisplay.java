/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.ui.fx.views.canvas;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.layout.VBox;

public abstract class CounterDisplay extends VBox
{
	protected Label numberDisplay;
	protected Formatting formatting;

	public enum Formatting
	{
		NONE, TIME
	}

	public abstract void setValue( long value );

	public static Label label( String name )
	{
		return LabelBuilder.create().text( name ).minWidth(25).style( "-fx-font-size: 10px;" ).build();
	}

	public static Label numberDisplay()
	{
		
		return LabelBuilder
				.create()
				.minWidth( 45 )
				.prefWidth(50)
				.style("-fx-text-fill: #f2f2f2; -fx-font-size: 10px; ")
				.alignment( Pos.CENTER )
				.build();
	}
}
