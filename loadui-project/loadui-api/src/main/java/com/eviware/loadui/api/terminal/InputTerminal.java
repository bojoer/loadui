/*
 * Copyright 2011 eviware software ab
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
package com.eviware.loadui.api.terminal;

public interface InputTerminal extends Terminal
{
	/**
	 * Returns true if this InputTerminal "likes" the given OutputTerminal. If an
	 * InputTerminal likes an OutputTerminal, this is a strong signal that the
	 * user may want to connect the two together. Implementing this is optional.
	 * If it isn't known that the two Terminals go well together always prefer to
	 * return false.
	 * 
	 * @param outputTerminal
	 * @return
	 */
	public boolean likes( OutputTerminal outputTerminal );
}
