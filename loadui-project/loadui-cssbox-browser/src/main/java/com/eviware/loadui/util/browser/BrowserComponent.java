package com.eviware.loadui.util.browser;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
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

import com.gargoylesoftware.htmlunit.DefaultPageCreator;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TopLevelWindow;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.WebWindowListener;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

public class BrowserComponent extends JPanel implements Browser
{
	private static final long serialVersionUID = -2243990100698952202L;

	protected static final Logger log = LoggerFactory.getLogger( BrowserComponent.class );

	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final WebClient client = new WebClient();
	private final WebWindow window;
	private HtmlPage page;

	private URL failureUrl;
	private URL url;
	private DOMAnalyzer da;
	private Element root;
	private double desiredHeight = 100;
	private double pageHeight = 0;

	public BrowserComponent() throws FailingHttpStatusCodeException, MalformedURLException, IOException
	{
		super( new BorderLayout() );

		client.setPageCreator( new DefaultPageCreator()
		{
			private static final long serialVersionUID = 5511790383365804218L;

			@Override
			public Page createPage( WebResponse webResponse, WebWindow webWindow ) throws IOException
			{
				if( webWindow != window )
				{
					try
					{
						Desktop.getDesktop().browse( webResponse.getRequestSettings().getUrl().toURI() );
					}
					catch( URISyntaxException e )
					{
						log.error( "Unable to open external page", e );
					}

					( ( TopLevelWindow )webWindow.getTopWindow() ).close();
					return null;
				}

				return super.createPage( webResponse, webWindow );
			}
		} );

		client.addWebWindowListener( new MyWebWindowListener() );
		window = client.getCurrentWindow();

		addComponentListener( new PanelSizeListener() );
		addMouseMotionListener( new MouseMovedListener() );
		addMouseListener( new ClickListener() );
	}

	private static Node findLink( Node node )
	{
		while( node != null && !"a".equals( node.getNodeName() ) )
		{
			node = node.getParentNode();
		}

		return node;
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

	private HtmlElement lookupElementForNode( Node node )
	{
		while( !( node instanceof Element ) )
		{
			if( node == null )
			{
				//log.debug( "No element found!" );
				return null;
			}
			node = node.getParentNode();
		}

		if( root == node )
		{
			//log.debug( "Node is root: {}, returning...", root );
			return page.getDocumentElement();
		}
		else
		{
			Node sibling = node.getPreviousSibling();
			int position = 0;
			while( sibling != null )
			{
				if( sibling instanceof Element )
				{
					position++ ;
				}
				sibling = sibling.getPreviousSibling();
			}

			HtmlElement parent = lookupElementForNode( node.getParentNode() );

			if( parent == null )
			{
				return null;
			}

			return Iterables.get( parent.getChildElements(), position );
		}
	}

	public void setFailureUrl( String failureUrlString ) throws MalformedURLException
	{
		failureUrl = new URL( failureUrlString );
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
					page = client.getPage( window, new WebRequestSettings( new URL( urlString ) ) );
					return;
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

				try
				{
					if( failureUrl != null )
					{
						page = client.getPage( window, new WebRequestSettings( failureUrl ) );
					}
				}
				catch( FailingHttpStatusCodeException e )
				{
					log.error( "Failed to load OFFLINE URL: " + failureUrl, e );
				}
				catch( IOException e )
				{
					log.error( "Failed to load OFFLINE URL: " + failureUrl, e );
				}
			}
		} );

	}

	public double getDesiredHeight()
	{
		return desiredHeight;
	}

	public void setDesiredHeight( double desiredHeight )
	{
		this.desiredHeight = desiredHeight;
		if( desiredHeight > pageHeight )
		{
			try
			{
				renderPage();
			}
			catch( RuntimeException e1 )
			{
				parsePageContent( 5 );
			}
		}
	}

	public double getPageHeight()
	{
		return pageHeight;
	}

	private void parsePageContent( int tries )
	{
		WebResponse response = page.getWebResponse();
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
			log.error( "Error parsing page content", e );
		}
		catch( IOException e )
		{
			log.error( "Error parsing page content", e );
		}
		catch( RuntimeException e )
		{
			if( tries > 0 )
			{
				log.error( "Failed parsing page, tries left: " + tries, e );
				parsePageContent( tries - 1 );
			}
			else
			{
				throw e;
			}
		}
	}

	private void renderPage()
	{
		if( root != null )
		{
			final Dimension size = getSize();
			//Compensate for 2px border added around HTML content.
			size.setSize( size.getWidth() - 4, desiredHeight - 4 );

			if( size.getHeight() > 0 && size.getWidth() > 0 )
			{
				SwingUtilities.invokeLater( new Runnable()
				{
					@Override
					public void run()
					{
						BrowserCanvas canvas = new BrowserCanvas( root, da, size, url );
						removeAll();

						add( canvas, BorderLayout.CENTER );
						double oldHeight = pageHeight;
						pageHeight = canvas.getPreferredSize().getHeight();
						firePropertyChange( PAGE_HEIGHT, oldHeight, pageHeight );
					}
				} );
			}
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

				final HtmlElement element = lookupElementForNode( Objects.firstNonNull( findLink( node ), node ) );
				if( element != null )
				{
					executor.execute( new Runnable()
					{
						@Override
						public void run()
						{
							try
							{
								log.debug( "Clicking on: {}", element );
								element.click();
							}
							catch( IOException e1 )
							{
								log.error( "Failed clicking on: " + element, e1 );
							}
						}
					} );
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
						firePropertyChange( CURSOR, oldCursor, getCursor() );
					}
				}
				else
				{
					if( hover )
					{
						hover = false;
						Cursor oldCursor = getCursor();
						setCursor( Cursor.getDefaultCursor() );
						firePropertyChange( CURSOR, oldCursor, getCursor() );
					}
				}
			}
		}

		@Override
		public void mouseDragged( MouseEvent e )
		{
		}
	}

	private final class MyWebWindowListener implements WebWindowListener
	{
		@Override
		public void webWindowOpened( WebWindowEvent event )
		{
		}

		@Override
		public void webWindowContentChanged( WebWindowEvent event )
		{
			if( event.getWebWindow().getTopWindow() == window )
			{
				//				if( page != null )
				//				{
				//					page.removeDomChangeListener( domListener );
				//				}
				page = ( HtmlPage )window.getEnclosedPage();
				//page.addDomChangeListener( domListener );

				parsePageContent( 5 );
			}
			else
			{
				( ( TopLevelWindow )event.getWebWindow().getTopWindow() ).close();
			}
		}

		@Override
		public void webWindowClosed( WebWindowEvent event )
		{
		}
	}

	private final class PanelSizeListener implements ComponentListener
	{
		@Override
		public void componentShown( ComponentEvent e )
		{
			try
			{
				renderPage();
			}
			catch( RuntimeException e1 )
			{
				parsePageContent( 5 );
			}
		}

		@Override
		public void componentResized( ComponentEvent e )
		{
			try
			{
				renderPage();
			}
			catch( RuntimeException e1 )
			{
				parsePageContent( 5 );
			}
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
