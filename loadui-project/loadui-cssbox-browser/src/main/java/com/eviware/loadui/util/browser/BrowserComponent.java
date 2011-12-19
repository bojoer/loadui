package com.eviware.loadui.util.browser;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.demo.DOMSource;
import org.fit.cssbox.layout.Box;
import org.fit.cssbox.layout.BrowserCanvas;
import org.fit.cssbox.layout.ElementBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.WebWindowListener;

public class BrowserComponent extends JPanel implements Browser
{
	protected static final Logger log = LoggerFactory.getLogger( BrowserComponent.class );

	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport( this );
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final WebClient client = new WebClient();

	private URL url;
	private DOMAnalyzer da;
	private Element root;
	private double pageHeight = 0;

	public BrowserComponent( String urlString ) throws FailingHttpStatusCodeException, MalformedURLException,
			IOException
	{
		super( new BorderLayout() );

		client.addWebWindowListener( new MyWebWindowListener() );
		setUrl( urlString );

		addComponentListener( new PanelSizeListener() );
		addMouseMotionListener( new MouseMovedListener() );
		addMouseListener( new ClickListener() );
	}

	private static String findLink( Node node )
	{
		while( node != null && !"a".equals( node.getNodeName() ) )
		{
			node = node.getParentNode();
		}

		if( node != null )
		{
			Node namedItem = node.getAttributes().getNamedItem( "href" );
			if( namedItem != null )
			{
				return namedItem.getNodeValue();
			}
		}

		return null;
	}

	private static Box locateBox( Box box, int x, int y )
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

	public void setUrl( final String urlString )
	{
		executor.execute( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					client.getPage( urlString );
				}
				catch( FailingHttpStatusCodeException e )
				{
					log.error( "Failed to load URL: " + urlString, e );
				}
				catch( MalformedURLException e )
				{
					log.error( "Failed to load URL: " + urlString, e );
				}
				catch( IOException e )
				{
					log.error( "Failed to load URL: " + urlString, e );
				}
			}
		} );

	}

	@Override
	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
		propertyChangeSupport.addPropertyChangeListener( listener );
	}

	@Override
	public void removePropertyChangeListener( PropertyChangeListener listener )
	{
		propertyChangeSupport.removePropertyChangeListener( listener );
	}

	public double getPageHeight()
	{
		return pageHeight;
	}

	private void renderPage()
	{
		if( root != null )
		{
			SwingUtilities.invokeLater( new Runnable()
			{
				@Override
				public void run()
				{
					removeAll();
					Dimension size = getSize();
					//Compensate for 2px border added around HTML content.
					size.setSize( size.getWidth() - 4, size.getHeight() - 4 );

					if( size.getHeight() > 0 && size.getWidth() > 0 )
					{
						BrowserCanvas canvas = new BrowserCanvas( root, da, size, url );
						add( canvas, BorderLayout.CENTER );
						double oldHeight = pageHeight;
						pageHeight = canvas.getPreferredSize().getHeight();
						propertyChangeSupport.firePropertyChange( PAGE_HEIGHT, oldHeight, pageHeight );
					}
				}
			} );
		}
	}

	private final class ClickListener implements MouseListener
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
				if( getComponentCount() == 0 )
				{
					return;
				}

				BrowserCanvas canvas = ( BrowserCanvas )getComponent( 0 );

				Box box = locateBox( canvas.getViewport(), e.getX(), e.getY() );
				Node node = box.getNode();

				String href = findLink( node );
				if( href != null )
				{
					try
					{
						URL newUrl = new URL( url, href );
						client.getPage( newUrl );
					}
					catch( FailingHttpStatusCodeException e1 )
					{
						log.error( "Failed to get page at: " + href, e1 );
					}
					catch( MalformedURLException e1 )
					{
						log.error( "Failed to get page at: " + href, e1 );
					}
					catch( IOException e1 )
					{
						log.error( "Failed to get page at: " + href, e1 );
					}
				}
			}
		}
	}

	private final class MouseMovedListener implements MouseMotionListener
	{
		private boolean hover = false;

		@Override
		public void mouseMoved( MouseEvent e )
		{
			if( getComponentCount() == 0 )
			{
				return;
			}

			BrowserCanvas canvas = ( BrowserCanvas )getComponent( 0 );

			Box box = locateBox( canvas.getViewport(), e.getX(), e.getY() );
			if( box != null )
			{
				Node node = box.getNode();
				if( findLink( node ) != null )
				{
					if( !hover )
					{
						hover = true;
						Cursor oldCursor = getCursor();
						setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
						propertyChangeSupport.firePropertyChange( CURSOR, oldCursor, getCursor() );
					}
				}
				else
				{
					if( hover )
					{
						hover = false;
						Cursor oldCursor = getCursor();
						setCursor( Cursor.getDefaultCursor() );
						propertyChangeSupport.firePropertyChange( CURSOR, oldCursor, getCursor() );
					}
				}
			}
		}

		@Override
		public void mouseDragged( MouseEvent e )
		{
		}
	}

	private class MyWebWindowListener implements WebWindowListener
	{
		@Override
		public void webWindowOpened( WebWindowEvent event )
		{
			log.debug( "webWindowOpened: {}", event );
		}

		@Override
		public void webWindowContentChanged( WebWindowEvent event )
		{
			WebResponse response = event.getWebWindow().getEnclosedPage().getWebResponse();
			url = response.getRequestSettings().getUrl();

			//Parse the input document
			try
			{
				Document doc = new DOMSource( response.getContentAsStream() ).parse();

				//Create the CSS analyzer
				da = new DOMAnalyzer( doc, url );
				da.attributesToStyles(); //convert the HTML presentation attributes to inline styles
				da.addStyleSheet( null, CSSNorm.stdStyleSheet() ); //use the standard style sheet
				da.addStyleSheet( null, CSSNorm.userStyleSheet() ); //use the additional style sheet
				da.getStyleSheets(); //load the author style sheets

				//Display the result
				root = da.getRoot();

				renderPage();
			}
			catch( SAXException e )
			{
				e.printStackTrace();
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}

		@Override
		public void webWindowClosed( WebWindowEvent event )
		{
			log.debug( "webWindowClosed: {}", event );
		}
	}

	private final class PanelSizeListener implements ComponentListener
	{
		@Override
		public void componentShown( ComponentEvent e )
		{
			renderPage();
		}

		@Override
		public void componentResized( ComponentEvent e )
		{
			renderPage();
		}

		@Override
		public void componentMoved( ComponentEvent e )
		{
		}

		@Override
		public void componentHidden( ComponentEvent e )
		{
		}
	}
}
