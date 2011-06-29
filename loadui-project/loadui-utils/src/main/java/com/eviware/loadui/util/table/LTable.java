/*
 * Copyright 2011 eviware software ab
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
package com.eviware.loadui.util.table;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;

import au.com.bytecode.opencsv.CSVWriter;

import com.eviware.loadui.api.ui.table.LTableModel;

public class LTable extends JXTable
{

	private static final long serialVersionUID = 8659925359922608407L;
	private boolean autoscroll = false;

	public LTable( TableModel model )
	{
		super( model );

		addMouseMotionListener( new MouseMotionAdapter()
		{
			@Override
			public void mouseMoved( MouseEvent e )
			{
				Point p = e.getPoint();
				int row = rowAtPoint( p );
				int column = columnAtPoint( p );
				if( row > -1 && column > -1 )
				{
					setToolTipText( String.valueOf( getValueAt( row, column ) ) );
				}
			}
		} );

		getTableHeader().addMouseMotionListener( new MouseMotionAdapter()
		{
			@Override
			public void mouseMoved( MouseEvent e )
			{
				Point p = e.getPoint();
				int column = columnAtPoint( p );
				if( column > -1 )
				{
					String title = String.valueOf( getTableHeader().getColumnModel().getColumn( column ).getHeaderValue() );
					if( title.trim().length() > 0 )
					{
						getTableHeader().setToolTipText( String.valueOf( title ) );
					}
					else
					{
						getTableHeader().setToolTipText( null );
					}
				}
			}
		} );
	}

	@Override
	public void tableChanged( TableModelEvent e )
	{
		super.tableChanged( e );
		if( autoscroll )
			SwingUtilities.invokeLater( new Runnable()
			{

				@Override
				public void run()
				{
					if( getRowCount() > 0 )
						scrollRowToVisible( getRowCount() - 1 );
				}
			} );
	}

	public boolean isAutoscroll()
	{
		return autoscroll;
	}

	public void setAutoscroll( boolean autoscroll )
	{
		this.autoscroll = autoscroll;
	}

	public boolean save( File saveFile )
	{
		boolean result = true;
		CSVWriter writer = null;
		LTableModel model = ( LTableModel )getModel();
		try
		{
			writer = new CSVWriter( new FileWriter( saveFile, false ), ',' );
			for( int cnt = 0; cnt < model.getRowCount(); cnt++ )
				writer.writeNext( convertToStringArray( model.getRowAt( cnt ) ) );
			writer.flush();
		}
		catch( Exception e )
		{
			e.printStackTrace();
			result = false;
		}
		finally
		{
			if( writer != null )
				try
				{
					writer.close();
				}
				catch( IOException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return result;
	}

	/**
	 * 
	 * @param arraylist
	 * @return
	 */
	private String[] convertToStringArray( ArrayList<?> arraylist )
	{
		String[] result = new String[arraylist.size()];
		for( int cnt = 0; cnt < arraylist.size(); cnt++ )
		{
			result[cnt] = String.valueOf( arraylist.get( cnt ) );
		}
		return result;
	}
}
