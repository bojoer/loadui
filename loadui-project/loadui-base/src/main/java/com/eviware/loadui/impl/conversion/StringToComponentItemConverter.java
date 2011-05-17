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

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.config.LoaduiComponentDocumentConfig;
import com.eviware.loadui.impl.model.CanvasItemImpl;

public class StringToComponentItemConverter implements Converter<String, ComponentItem>
{
	private final AddressableRegistry addressableRegistry;

	public StringToComponentItemConverter( AddressableRegistry addressableRegistry )
	{
		this.addressableRegistry = addressableRegistry;
	}

	@Override
	public ComponentItem convert( String source )
	{
		try
		{
			String[] parts = source.split( ComponentItemImplToStringConverter.SEPARATOR, 2 );
			CanvasItemImpl<?> canvas = ( CanvasItemImpl<?> )addressableRegistry.lookup( parts[0] );
			LoaduiComponentDocumentConfig doc = LoaduiComponentDocumentConfig.Factory.parse( parts[1] );

			return canvas.injectComponent( doc.getLoaduiComponent() );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}

}
