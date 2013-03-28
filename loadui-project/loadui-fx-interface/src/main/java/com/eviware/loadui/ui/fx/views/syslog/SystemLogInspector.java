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
package com.eviware.loadui.ui.fx.views.syslog;

import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.RegionBuilder;

import com.eviware.loadui.ui.fx.api.Inspector;

public class SystemLogInspector implements Inspector
{
	@Override
	public void initialize( ReadOnlyProperty<Scene> sceneProperty )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getName()
	{
		return "System Log";
	}

	@Override
	public String getPerspectiveRegex()
	{
		return null;
	}

	@Override
	public Node getPanel()
	{
		return RegionBuilder.create().style( "-fx-background-color: yellow;" ).build();
	}

	@Override
	public void onShow()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onHide()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getHelpUrl()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
