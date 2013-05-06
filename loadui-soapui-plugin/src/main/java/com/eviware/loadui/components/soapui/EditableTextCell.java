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

package com.eviware.loadui.components.soapui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import com.eviware.soapui.model.testsuite.TestProperty;

/**
 * Editable Table cell containing a TextField for each cell. Used as an
 * alternative to the javafx class TextFieldTableCell but with enhanced editing
 * functionality
 * 
 * @author maximilian.skog
 * 
 */

class EditableTextCell extends TableCell<TestProperty, String>
{

	private TextField textField;

	public EditableTextCell()
	{
	}

	@Override
	public void startEdit()
	{
		if( !isEmpty() )
		{
			super.startEdit();
			createTextField();
			setText( null );
			setGraphic( textField );
			textField.selectAll();
		}
	}

	@Override
	public void cancelEdit()
	{
		super.cancelEdit();

		setText( ( String )getItem() );
		setGraphic( null );
	}

	@Override
	public void updateItem( String item, boolean empty )
	{
		super.updateItem( item, empty );

		if( empty )
		{
			setText( null );
			setGraphic( null );
		}
		else
		{
			if( isEditing() )
			{
				if( textField != null )
				{
					textField.setText( getString() );
				}
				setText( null );
				setGraphic( textField );
			}
			else
			{
				setText( getString() );
				setGraphic( null );
			}
		}
	}

	private void createTextField()
	{
		textField = new TextField( getString() );
		textField.setMinWidth( this.getWidth() - this.getGraphicTextGap() * 2 );
		textField.focusedProperty().addListener( new ChangeListener<Boolean>()
		{
			@Override
			public void changed( ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2 )
			{
				if( !arg2 )
				{
					commitEdit( textField.getText() );
				}
			}
		} );

		textField.setOnKeyPressed( new EventHandler<KeyEvent>()
		{
			@Override
			public void handle( KeyEvent ke )
			{
				if( ke.getCode().equals( KeyCode.ENTER ) )
				{
					commitEdit( textField.getText() );
				}
			}
		} );
	}

	private String getString()
	{
		return getItem() == null ? "" : getItem().toString();
	}
}