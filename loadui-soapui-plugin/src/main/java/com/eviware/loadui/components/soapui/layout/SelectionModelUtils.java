package com.eviware.loadui.components.soapui.layout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.SelectionModel;

public class SelectionModelUtils
{
	protected static final Logger log = LoggerFactory.getLogger( SelectionModelUtils.class );

	public static <T> ObjectProperty<T> writableSelectedItemProperty( final SelectionModel<T> selectionModel,
			final boolean ignoreNullValues )
	{
		final ObjectProperty<T> property = new ObjectPropertyBase<T>()
		{
			@Override
			public Object getBean()
			{
				return SelectionModel.class;
			}

			@Override
			public String getName()
			{
				return selectionModel.selectedItemProperty().getName();
			}
		};

		property.addListener( new ChangeListener<T>()
		{
			@Override
			public void changed( ObservableValue<? extends T> arg0, T oldValue, T newValue )
			{
				selectionModel.select( newValue );
			}
		} );

		selectionModel.selectedItemProperty().addListener( new ChangeListener<T>()
		{
			@Override
			public void changed( ObservableValue<? extends T> arg0, T arg1, T newValue )
			{
				log.debug( "selectedItemProperty: " + newValue );
				if( newValue != null || !ignoreNullValues )
				{
					log.debug( "propagating selectedItemProperty!" );
					property.set( newValue );
				}
			}
		} );

		return property;
	}
}
