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
package com.eviware.loadui.util.browser;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;

import javax.swing.JScrollPane;

import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.demo.DOMSource;
import org.fit.cssbox.layout.Box;
import org.fit.cssbox.layout.BrowserCanvas;
import org.fit.cssbox.layout.ElementBox;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class BrowserComponent extends JScrollPane implements Browser
{
	private final HashSet<CursorListener> cursorListeners = new HashSet<CursorListener>();

	private MyBrowserCanvas canvas;

	private static MyBrowserCanvas initializeCanvas( String urlString ) throws IOException, SAXException
	{
		//Open the network connection 
		URL url = new URL( urlString );
		URLConnection con = url.openConnection();
		InputStream is = con.getInputStream();

		//Parse the input document
		DOMSource parser = new DOMSource( is );
		Document doc = parser.parse();

		//Create the CSS analyzer
		DOMAnalyzer da = new DOMAnalyzer( doc, url );
		da.attributesToStyles(); //convert the HTML presentation attributes to inline styles
		da.addStyleSheet( null, CSSNorm.stdStyleSheet() ); //use the standard style sheet
		da.addStyleSheet( null, CSSNorm.userStyleSheet() ); //use the additional style sheet
		da.getStyleSheets(); //load the author style sheets

		//Display the result
		Element root = da.getRoot();

		return new MyBrowserCanvas( root, da, new java.awt.Dimension( 640, 1024 ), url );
	}

	public BrowserComponent( String url ) throws IOException, SAXException
	{
		super( initializeCanvas( url ), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER );

		canvas = ( MyBrowserCanvas )getViewport().getView();

		canvas.addMouseListener( new MouseListener()
		{
			@Override
			public void mouseReleased( MouseEvent e )
			{
			}

			@Override
			public void mousePressed( MouseEvent e )
			{
			}

			@Override
			public void mouseExited( MouseEvent e )
			{
			}

			@Override
			public void mouseEntered( MouseEvent e )
			{
			}

			@Override
			public void mouseClicked( MouseEvent e )
			{
				if( e.getButton() == MouseEvent.BUTTON1 )
				{
					Box box = locateBox( canvas.getViewport(), e.getX(), e.getY() );
					Node node = box.getNode();

					String href = findLink( node );
					if( href != null )
					{
						try
						{
							canvas.setUrl( href );
						}
						catch( IOException e1 )
						{
							e1.printStackTrace();
						}
						catch( SAXException e1 )
						{
							e1.printStackTrace();
						}
					}
				}
			}
		} );

		canvas.addMouseMotionListener( new MouseMotionListener()
		{
			private boolean hover = false;

			@Override
			public void mouseMoved( MouseEvent e )
			{
				Box box = locateBox( canvas.getViewport(), e.getX(), e.getY() );
				if( box != null )
				{
					Node node = box.getNode();
					if( findLink( node ) != null )
					{
						if( !hover )
						{
							hover = true;
							//System.out.println( "HAND" );
							setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
							notifyListeners( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
						}
					}
					else
					{
						if( hover )
						{
							hover = false;
							//System.out.println( "NO HAND" );
							setCursor( Cursor.getDefaultCursor() );
							notifyListeners( Cursor.getDefaultCursor() );
						}
					}
				}
			}

			@Override
			public void mouseDragged( MouseEvent e )
			{
			}
		} );

		getVerticalScrollBar().setUnitIncrement( 16 );
	}

	private String findLink( Node node )
	{
		while( node != null && !"a".equals( node.getNodeName() ) )
		{
			node = node.getParentNode();
		}

		if( node != null )
		{
			return node.getAttributes().getNamedItem( "href" ).getNodeValue();
		}

		return null;
	}

	private Box locateBox( Box box, int x, int y )
	{
		Box found = null;
		Rectangle bounds = box.getAbsoluteBounds();
		if( bounds.contains( x, y ) )
		{
			found = box;
		}

		//find if there is something smallest that fits among the child boxes
		if( box instanceof ElementBox )
		{
			ElementBox el = ( ElementBox )box;
			for( int i = el.getStartChild(); i < el.getEndChild(); i++ )
			{
				Box inside = locateBox( el.getSubBox( i ), x, y );
				if( inside != null )
				{
					if( found == null )
					{
						found = inside;
					}
					else
					{
						if( inside.getAbsoluteBounds().width * inside.getAbsoluteBounds().height < found.getAbsoluteBounds().width
								* found.getAbsoluteBounds().height )
						{
							found = inside;
						}
					}
				}
			}
		}

		return found;
	}

	public void setUrl( String urlString ) throws IOException, SAXException
	{
		canvas.setUrl( urlString );
	}

	@Override
	public void addCursorListener( CursorListener listener )
	{
		cursorListeners.add( listener );
	}

	@Override
	public void removeCursorListener( CursorListener listener )
	{
		cursorListeners.remove( listener );
	}

	private void notifyListeners( Cursor cursor )
	{
		for( CursorListener listener : cursorListeners )
		{
			listener.handleCursorChanged( cursor );
		}
	}

	private static class MyBrowserCanvas extends BrowserCanvas
	{
		public MyBrowserCanvas( Element root, DOMAnalyzer decoder, Dimension dim, URL baseurl )
		{
			super( root, decoder, dim, baseurl );
		}

		public void setUrl( String urlString ) throws IOException, SAXException
		{
			URL url = new URL( baseurl, urlString );

			URLConnection con = url.openConnection();
			InputStream is = con.getInputStream();

			//Parse the input document
			DOMSource parser = new DOMSource( is );
			Document doc = parser.parse();

			//Create the CSS analyzer
			DOMAnalyzer da = new DOMAnalyzer( doc, url );
			da.attributesToStyles(); //convert the HTML presentation attributes to inline styles
			da.addStyleSheet( null, CSSNorm.stdStyleSheet() ); //use the standard style sheet
			da.addStyleSheet( null, CSSNorm.userStyleSheet() ); //use the additional style sheet
			da.getStyleSheets(); //load the author style sheets

			//Display the result
			Element root = da.getRoot();

			this.root = root;
			this.decoder = da;
			this.baseurl = url;
			createLayout( getSize() );
		}
	}
}
