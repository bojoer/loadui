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
package com.eviware.loadui.impl.conversion;

import org.springframework.core.convert.converter.Converter;

import com.eviware.loadui.config.LoaduiComponentDocumentConfig;
import com.eviware.loadui.impl.model.ComponentItemImpl;

public class ComponentItemImplToStringConverter implements Converter<ComponentItemImpl, String>
{
	public static final String SEPARATOR = ":";

	@Override
	public String convert( ComponentItemImpl source )
	{
		LoaduiComponentDocumentConfig doc = LoaduiComponentDocumentConfig.Factory.newInstance();
		doc.addNewLoaduiComponent().set( source.getConfig() );
		return source.getCanvas().getId() + SEPARATOR + doc.xmlText();
	}

}
