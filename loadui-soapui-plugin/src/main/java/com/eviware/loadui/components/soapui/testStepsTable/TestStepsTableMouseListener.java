package com.eviware.loadui.components.soapui.testStepsTable;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTable;

import com.eviware.loadui.components.soapui.SoapUISamplerComponent;

public class TestStepsTableMouseListener implements MouseListener
{
	private final JTable jTable;
	private final SoapUISamplerComponent component;

	public TestStepsTableMouseListener( JTable jTable, SoapUISamplerComponent component )
	{
		this.jTable = jTable;
		this.component = component;
	}

	@Override
	public void mouseClicked( MouseEvent e )
	{
		if( e.getButton() == MouseEvent.BUTTON1 && jTable.columnAtPoint( e.getPoint() ) == 1 )
		{
			int testStep = jTable.rowAtPoint( e.getPoint() );
			boolean isAlreadyDisabled = component.getTestStepIsDisabled( testStep );
			component.setTestStepIsDisabled( jTable.rowAtPoint( e.getPoint() ), !isAlreadyDisabled );
		}
	}

	@Override
	public void mousePressed( MouseEvent e )
	{
	}

	@Override
	public void mouseReleased( MouseEvent e )
	{
	}

	@Override
	public void mouseEntered( MouseEvent e )
	{
	}

	@Override
	public void mouseExited( MouseEvent e )
	{
	}
}
