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
package com.eviware.loadui.ui.fx.views.assertions;

import com.eviware.loadui.api.traits.Labeled;

import javafx.scene.control.TreeCell;

public class LabeledTreeCell extends TreeCell<Labeled>
{
	@Override
	public void updateItem( Labeled item, boolean empty )
	{
		super.updateItem( item, empty );

		if( empty )
		{
			setText( null );
		}
		else
		{
			setText( item.getLabel() );
		}
	}
}
