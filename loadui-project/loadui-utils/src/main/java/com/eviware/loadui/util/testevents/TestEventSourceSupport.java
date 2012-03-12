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
package com.eviware.loadui.util.testevents;

import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import com.google.common.base.Objects;

/**
 * Support class for generating a unique hash String to be used by
 * TestEvent.Sources, based on a label and arbitrary data.
 * 
 * @author dain.nilsson
 */
public class TestEventSourceSupport
{
	private String label;
	private byte[] data = new byte[0];
	private String hash;

	public TestEventSourceSupport( String label, byte[] data )
	{
		this.label = label;
		setData( data );
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel( String newLabel )
	{
		if( !Objects.equal( label, newLabel ) )
		{
			label = newLabel;
			updateHash();
		}
	}

	public byte[] getData()
	{
		byte[] dataCopy = new byte[data.length];
		System.arraycopy( data, 0, dataCopy, 0, data.length );

		return dataCopy;
	}

	public void setData( byte[] newData )
	{
		if( !Arrays.equals( data, newData ) )
		{
			data = new byte[newData.length];
			System.arraycopy( newData, 0, data, 0, newData.length );
			updateHash();
		}
	}

	private void updateHash()
	{
		byte[] labelBytes = label.getBytes();
		byte[] combined = new byte[labelBytes.length + data.length];
		System.arraycopy( labelBytes, 0, combined, 0, labelBytes.length );
		System.arraycopy( data, 0, combined, labelBytes.length, data.length );

		hash = Base64.encodeBase64String( DigestUtils.md5( combined ) );
	}

	public String getHash()
	{
		return hash;
	}
}
