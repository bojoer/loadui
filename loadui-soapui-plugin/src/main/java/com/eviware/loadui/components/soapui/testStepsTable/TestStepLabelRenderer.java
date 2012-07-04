package com.eviware.loadui.components.soapui.testStepsTable;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class TestStepLabelRenderer extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = 983282456920150699L;

	@Override
	public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column )
	{
		if( value instanceof JLabel )
		{
			return ( JLabel )value;
		}
		return super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
	}
}
