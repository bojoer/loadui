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
package com.eviware.loadui.api.component.categories;

/**
 * This is here because some of the Groovy Components in loadUI 1.0 have an
 * import statement importing this class, even though it is never used. The real
 * TriggerCategory has been renamed to GeneratorCategory. The scripts have been
 * updated, but if you have a sample project which is from 1.0, it will fail to
 * load some of the components if this class doesn't exist. DO NOT USE THIS
 * CLASS!
 * 
 * @author dain.nilsson
 */
@Deprecated
public final class TriggerCategory
{
	@Deprecated
	public TriggerCategory()
	{
		throw new RuntimeException();
	}
}
