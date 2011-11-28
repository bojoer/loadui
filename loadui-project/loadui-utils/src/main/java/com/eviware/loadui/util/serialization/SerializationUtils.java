package com.eviware.loadui.util.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream( baos );
		oos.writeObject( object );
		oos.flush();

		return baos.toByteArray();
	}

	public static Object deserialize( byte[] serializedObject ) throws IOException, ClassNotFoundException
	{
		return new ObjectInputStream( new ByteArrayInputStream( serializedObject ) ).readObject();
	}
}