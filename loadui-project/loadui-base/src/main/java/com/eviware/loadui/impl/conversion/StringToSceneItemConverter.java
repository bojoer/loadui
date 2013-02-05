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
package com.eviware.loadui.impl.conversion;

import org.apache.xmlbeans.XmlException;
import org.springframework.core.convert.converter.Converter;

import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.config.LoaduiSceneDocumentConfig;
import com.eviware.loadui.impl.model.SceneItemImpl;

public class StringToSceneItemConverter implements Converter<String, SceneItem>
{
	@Override
	public SceneItem convert( String source )
	{
		try
		{
			LoaduiSceneDocumentConfig doc = LoaduiSceneDocumentConfig.Factory.parse( source );

			return SceneItemImpl.newInstance( null, doc.getLoaduiScene() );
		}
		catch( XmlException e )
		{
			throw new RuntimeException( e );
		}
	}
}
