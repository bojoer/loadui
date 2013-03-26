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
package com.eviware.loadui.ui.fx.control.behavior;

import static javafx.scene.input.KeyCode.PAGE_DOWN;
import static javafx.scene.input.KeyCode.PAGE_UP;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;

import com.eviware.loadui.ui.fx.control.ToolBox;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;

public class ToolBoxBehavior<E extends Node> extends BehaviorBase<ToolBox<E>>
{
	protected static final String SCROLL_UP_ACTION = "ScrollUp";
	protected static final String SCROLL_DOWN_ACTION = "ScrollDown";

	protected static final List<KeyBinding> TOOL_BOX_BINDINGS = new ArrayList<>();

	static
	{
		TOOL_BOX_BINDINGS.add( new KeyBinding( PAGE_UP, SCROLL_UP_ACTION ) );
		TOOL_BOX_BINDINGS.add( new KeyBinding( PAGE_DOWN, SCROLL_DOWN_ACTION ) );
	}

	private Runnable onScrollPageUp;
	private Runnable onScrollPageDown;

	@Override
	protected List<KeyBinding> createKeyBindings()
	{
		return TOOL_BOX_BINDINGS;
	}

	@Override
	protected void callAction( String name )
	{
		switch( name )
		{
		case SCROLL_UP_ACTION :
			scrollPageUp();
			break;
		case SCROLL_DOWN_ACTION :
			scrollPageDown();
			break;
		default :
			super.callAction( name );
		}
	}

	public void setOnScrollPageUp( Runnable runnable )
	{
		onScrollPageUp = runnable;
	}

	public void setOnScrollPageDown( Runnable runnable )
	{
		onScrollPageDown = runnable;
	}

	public ToolBoxBehavior( ToolBox<E> control )
	{
		super( control );
	}

	private void scrollPageUp()
	{
		if( onScrollPageUp != null )
			onScrollPageUp.run();
	}

	private void scrollPageDown()
	{
		if( onScrollPageDown != null )
			onScrollPageDown.run();
	}
}
