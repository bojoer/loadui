/*
 * Copyright 2011 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.api.ui.inspector;

/**
 * A content panel which is displayed on the bottom InspectorPanel.
 * 
 * @author dain.nilsson
 */
public interface Inspector
{
	/**
	 * Each Inspector needs to have a short unique name which is used to identify
	 * the Inspector.
	 * 
	 * @return A static String which identifies the Inspector
	 */
	public String getName();

	/**
	 * The content for the Inspector which is shown on screen. Should be a
	 * javafx.scene.layout.Node, preferable implementing
	 * javafx.scene.layout.Resizable.
	 * 
	 * @return A Content Panel
	 */
	public Object getPanel();

	/**
	 * This method is called to notify the Inspector that its content panel is
	 * visible on screen.
	 */
	public void onShow();

	/**
	 * This method is called to notify the Inspector that its content panel is no
	 * longer visible on screen.
	 */
	public void onHide();

	/**
	 * Gets the URL to a web site providing help for the Inspector, or null if no
	 * such web site exists.
	 * 
	 * @return The full URL of a help page for the Inspector.
	 */
	public String getHelpUrl();

}
