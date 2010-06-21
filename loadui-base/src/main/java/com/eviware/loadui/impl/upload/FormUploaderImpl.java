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
package com.eviware.loadui.impl.upload;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;

import com.eviware.loadui.api.upload.FileFormField;
import com.eviware.loadui.api.upload.FormField;
import com.eviware.loadui.api.upload.FormUploader;
import com.myjavatools.web.ClientHttpRequest;

public class FormUploaderImpl implements FormUploader
{
	@Override
	public void upload( String target, Collection<FormField> fields )
	{
		try
		{
			ClientHttpRequest request = new ClientHttpRequest( target );
			for( FormField field : fields )
			{
				if( field instanceof FileFormField )
				{
					FileFormField fileField = ( FileFormField )field;
					request.setParameter( fileField.getFieldName(), fileField.getValue(), new FileInputStream( fileField
							.getFile() ) );
				}
			}
			request.post().close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
