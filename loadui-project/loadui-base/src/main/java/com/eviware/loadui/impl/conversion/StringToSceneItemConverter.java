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
package com.eviware.loadui.impl.conversion;

import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.springframework.core.convert.converter.Converter;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.config.LoaduiSceneDocumentConfig;
import com.eviware.loadui.impl.model.SceneItemImpl;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.StringUtils;

public class StringToSceneItemConverter implements Converter<String, SceneItem>
{
	@Override
	public SceneItem convert( String source )
	{
		try
		{
			List<String> parts = StringUtils.deserialize( source );
			LoaduiSceneDocumentConfig doc = LoaduiSceneDocumentConfig.Factory.parse( parts.get( 0 ) );

			return SceneItemImpl.newInstance(
					( ProjectItem )BeanInjector.getBean( AddressableRegistry.class ).lookup( parts.get( 1 ) ),
					doc.getLoaduiScene() );
		}
		catch( XmlException e )
		{
			throw new RuntimeException( e );
		}
	}
}
