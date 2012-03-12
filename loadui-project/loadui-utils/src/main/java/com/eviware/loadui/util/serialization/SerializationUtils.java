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
package com.eviware.loadui.util.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.codec.binary.Base64;

/**
 * Uses standard Java Serializable API to convert an Object to and from its
 * serialized byte[] state.
 * 
 * @author dain.nilsson
 */
public class SerializationUtils
{
	public static byte[] serialize( Object object ) throws IOException
	{
		if( object == null )
			return null;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream( baos );
		oos.writeObject( object );
		oos.flush();

		return baos.toByteArray();
	}

	public static Object deserialize( byte[] serializedObject ) throws IOException, ClassNotFoundException
	{
		if( serializedObject == null )
			return null;

		return new ObjectInputStream( new ByteArrayInputStream( serializedObject ) ).readObject();
	}

	public static String serializeBase64( Object object ) throws IOException
	{
		if( object == null )
			return null;

		return Base64.encodeBase64String( serialize( object ) );
	}

	public static Object deserialize( String base64String ) throws ClassNotFoundException, IOException
	{
		if( base64String == null )
			return null;

		return deserialize( Base64.decodeBase64( base64String ) );
	}
}