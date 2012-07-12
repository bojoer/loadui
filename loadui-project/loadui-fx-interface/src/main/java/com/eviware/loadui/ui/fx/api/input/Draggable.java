package com.eviware.loadui.ui.fx.api.input;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;

public interface Draggable
{
	public ReadOnlyBooleanProperty acceptableProperty();

	public boolean isAcceptable();

	public ReadOnlyBooleanProperty draggingProperty();

	public boolean isDragging();

	public ObjectProperty<Object> dataProperty();

	public Object getData();

	public void setData( Object data );
}
