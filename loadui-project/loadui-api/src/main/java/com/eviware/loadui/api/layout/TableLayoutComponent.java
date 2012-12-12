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
package com.eviware.loadui.api.layout;

import javax.swing.table.TableModel;

/**
 * A LayoutComponent which holds a TableModel value and makes it accessible from
 * the user interface.
 * 
 * @author dain.nilsson
 */
public interface TableLayoutComponent extends LayoutComponent
{
	/**
	 * Accessor for the TableModel that is made available through this
	 * LayoutComponent.
	 * 
	 * @return The bound TableModel
	 */
	public TableModel getTableModel();

	/**
	 * Each TableLayoutComponent may optionally have a label attached to it. If
	 * the component does not have a label, this method will return null.
	 * 
	 * @return The label if one exists, otherwise null.
	 */
	public String getLabel();
}
