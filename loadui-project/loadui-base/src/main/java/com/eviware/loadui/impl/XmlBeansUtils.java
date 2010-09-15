package com.eviware.loadui.impl;

import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.XmlTokenSource;

public class XmlBeansUtils
{
	public static void saveToFile( XmlTokenSource source, File target ) throws IOException
	{
		File backup = File.createTempFile( "loadui-temp-", ".bak", target.getParentFile() );
		backup.delete();
		File temp = File.createTempFile( "loadui-temp-", ".xml", target.getParentFile() );
		source.save( temp );
		if( !target.renameTo( backup ) )
		{
			temp.delete();
			throw new IOException( "Error saving file: " + target + "! Unable to create backup!" );
		}
		if( !temp.renameTo( target ) )
		{
			backup.delete();
			throw new IOException( "Error saving file: " + target + "! Unable to write to file!" );
		}

		backup.delete();
	}
}
