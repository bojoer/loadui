package com.eviware.loadui.ui.fx.util.test;

import static com.google.common.collect.Iterables.get;

import java.util.Collection;

import javafx.scene.Node;

public class ComponentHandle
{
	public final Collection<Node> inputs;
	public final Collection<Node> outputs;
	private final TestFX controller;

	ComponentHandle( Collection<Node> inputs, Collection<Node> outputs, TestFX controller )
	{
		this.inputs = inputs;
		this.outputs = outputs;
		this.controller = controller;
	}

	public void connectTo( ComponentHandle otherComponent )
	{
		controller.drag( get( outputs, 0 ) ).to( get( otherComponent.inputs, 0 ) );
	}
}
