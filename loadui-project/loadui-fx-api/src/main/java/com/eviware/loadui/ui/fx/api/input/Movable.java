package com.eviware.loadui.ui.fx.api.input;

import javafx.scene.Node;

public interface Movable extends Draggable
{

	public Node getNode();

	public Node getHandle();

}