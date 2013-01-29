package com.eviware.loadui.ui.fx.api.input;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.Node;

public interface Selectable
{

	public Node getNode();

	public ReadOnlyBooleanProperty selectedProperty();

	public boolean isSelected();

	public void select();

	public void deselect();

}