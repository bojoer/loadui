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
package com.eviware.loadui.ui.fx.api.analysis;

import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import com.eviware.loadui.api.statistics.model.ChartGroup;

public interface ChartGroupView
{

	public ToggleGroup getChartGroupToggleGroup();

	public HBox getButtonBar();

	public AnchorPane getComponentGroupAnchor();

	public VBox getMainChartGroup();

	public ChartGroup getChartGroup();

	public MenuButton getMenuButton();

	public Node getNode();

}
