/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.api.component.categories;

import com.eviware.loadui.api.component.ComponentBehavior;
import com.eviware.loadui.api.terminal.InputTerminal;

/**
 * Analysis Components analyze input received and display the results.
 * 
 * @author dain.nilsson
 */
public interface AnalysisCategory extends ComponentBehavior
{
	/**
	 * The String identifier of the category.
	 */
	public static final String CATEGORY = "Analysis";

	/**
	 * The color of the category.
	 */
	public static final String COLOR = "#eebf0f";

	/**
	 * The label of the InputTerminal which is returned by getInputTerminal().
	 */
	public static final String INPUT_TERMINAL = "inputTerminal";

	/**
	 * The InputTerminal which is used to to direct TerminalMessages into the
	 * Analysis Component.
	 * 
	 * @return
	 */
	public InputTerminal getInputTerminal();
}
