package com.eviware.loadui.util;

import java.net.URL;
import java.net.URLConnection;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.util.StringUtils;

public class NewVersionChecker
{
	public static final Logger log = LoggerFactory.getLogger( NewVersionChecker.class );

	private static final String LATEST_VERSION_XML_LOCATION = "http://dl.eviware.com/version-update/loadui-version-testFuture.xml";

	private static boolean isVersionDesired( @Nonnull String latestVersion )
	{
		String currentLoaduiVersion = LoadUI.VERSION;
		int snapshotIndex = currentLoaduiVersion.indexOf( "SNAPSHOT" );
		boolean isSnapshot = snapshotIndex > 0;
		//if version is snapshot strip SNAPSHOT
		if( isSnapshot )
		{
			currentLoaduiVersion = currentLoaduiVersion.substring( 0, snapshotIndex - 1 );
		}

		if( StringUtils.isNullOrEmpty( latestVersion ) )
			return false;

		// user has to be notified when SNAPSHOT version became OFFICIAL 
		if( isSnapshot && currentLoaduiVersion.equals( latestVersion ) )
		{
			return true;
		}
		if( currentLoaduiVersion.compareTo( latestVersion ) < 0 )
		{
			return true;
		}

		return false;
	}

	private static String getElementContent( String tagName, Element rootElement )
	{
		NodeList elmntElmntLst = rootElement.getElementsByTagName( tagName );
		Element elmnt = ( Element )elmntElmntLst.item( 0 );
		NodeList nodes = elmnt.getChildNodes();
		return nodes.item( 0 ).getNodeValue().toString();
	}

	private static boolean shouldThisVersionBeSkipped( String versionName, WorkspaceItem workspace )
	{
		return workspace.getProperty( WorkspaceItem.IGNORED_VERSION_UPDATE ).getValue().equals( versionName );
	}

	public static VersionInfo checkForNewVersion( WorkspaceItem workspace )
	{
		String versionName = null;
		String releaseNotes = null;
		String downloadUrl = null;

		try
		{
			URL versionUrl = new URL( LATEST_VERSION_XML_LOCATION );
			URLConnection connection = versionUrl.openConnection();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse( connection.getInputStream() );
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName( "version" );

			Node fstNode = nodeLst.item( 0 );

			if( fstNode.getNodeType() == Node.ELEMENT_NODE )
			{
				Element fstElmnt = ( Element )fstNode;

				versionName = getElementContent( "version-number", fstElmnt );
				getElementContent( "release-notes-core", fstElmnt ); //TODO: Add Pro/OS check
				releaseNotes = getElementContent( "release-notes-pro", fstElmnt );
				getElementContent( "download-link-core", fstElmnt );
				downloadUrl = getElementContent( "download-link-pro", fstElmnt );
			}
		}

		catch( Exception e )
		{
			log.debug( "Error while checking for new version: ", e );
			return null;
		}

		log.debug( "Latest version is: {}", versionName );
		log.debug( "Current version is: {}", LoadUI.VERSION );

		if( isVersionDesired( versionName ) && !shouldThisVersionBeSkipped( versionName, workspace ) )
			return new VersionInfo( versionName, releaseNotes, downloadUrl, workspace );
		return null;
	}

	public static class VersionInfo
	{
		@Nonnull
		final public String versionName;
		@Nonnull
		final public String releaseNotes;
		@Nonnull
		final public String downloadUrl;
		@Nonnull
		final private WorkspaceItem workspace;

		VersionInfo( String versionName, String releaseNotes, String downloadUrl, WorkspaceItem workspace )
		{
			this.versionName = versionName;
			this.releaseNotes = releaseNotes;
			this.downloadUrl = downloadUrl;
			this.workspace = workspace;
		}

		public void skipThisVersion()
		{
			workspace.getProperty( WorkspaceItem.IGNORED_VERSION_UPDATE ).setValue( versionName );
		}
	}

}
