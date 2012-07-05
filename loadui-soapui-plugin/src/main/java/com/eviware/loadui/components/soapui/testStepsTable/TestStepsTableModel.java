package com.eviware.loadui.components.soapui.testStepsTable;

import java.awt.Color;
import java.awt.Dimension;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.components.soapui.SoapUISamplerComponent;
import com.eviware.loadui.impl.layout.LayoutComponentImpl;
import com.eviware.loadui.impl.layout.PropertyLayoutComponentImpl;
import com.eviware.loadui.util.ScheduledExecutor;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;

/**
 * Backend for the TestStep table in the soapUI Runner.
 * 
 * @author henrik.olsson
 * 
 */

public final class TestStepsTableModel extends AbstractTableModel implements Releasable
{
	private static final Logger log = LoggerFactory.getLogger( TestStepsTableModel.class );

	public static final int TESTSTEP_LABEL_COLUMN = 0;
	public static final int TESTSTEP_DISABLED_COLUMN = 1;
	public static final int TESTSTEP_COUNT_COLUMN = 2;

	private static final long serialVersionUID = 7482180639074166973L;
	private final ImageIcon disabledIcon = new ImageIcon( getClass().getResource( "/images/disabledTestStep.png" ) );
	private static final ImageIcon enabledIcon = null;

	private WsdlTestCase testCase;
	private final SoapUISamplerComponent component;
	public ScheduledFuture<?> future;
	private final long updateInterval = 500;

	private final LoadingCache<Integer, JLabel> testStepLabels = CacheBuilder.newBuilder().build(
			new CacheLoader<Integer, JLabel>()
			{
				@Override
				public JLabel load( final Integer stepIndex ) throws Exception
				{
					final TestStep step = testCase.getTestStepAt( stepIndex );
					JLabel label = new JLabel( step.getLabel(), step.getIcon(), JLabel.LEFT );
					try
					{
						label.setEnabled( !component.getTestStepIsDisabled( stepIndex ) );
					}
					catch( Exception e )
					{
						log.debug( "EXCEPTION: " + e.getMessage() );
						return null;
					}
					return label;
				}
			} );

	public static TestStepsTableModel newInstance( @Nonnull final SoapUISamplerComponent component )
	{
		TestStepsTableModel t = new TestStepsTableModel( component );
		t.initRefresher();
		return t;
	}

	private TestStepsTableModel( @Nonnull final SoapUISamplerComponent component )
	{
		this.component = component;
	}

	public void initRefresher()
	{
		future = ScheduledExecutor.instance.scheduleAtFixedRate( new Runnable()
		{
			@Override
			public void run()
			{
				for( int row = 0; row < getRowCount(); row++ )
				{
					fireTableCellUpdated( row, TESTSTEP_COUNT_COLUMN );
				}
			}
		}, updateInterval, updateInterval, TimeUnit.MILLISECONDS );
	}

	@Override
	public void release()
	{
		future.cancel( true );
	}

	@Override
	public String getColumnName( int column )
	{
		switch( column )
		{
		case TESTSTEP_LABEL_COLUMN :
			return "TestStep";
		case TESTSTEP_DISABLED_COLUMN :
			return "Disable";
		case TESTSTEP_COUNT_COLUMN :
			return "Count";
		default :
			throw new IllegalArgumentException( "Illegal column index specified: " + column + ". Number of columns is "
					+ getColumnCount() + "." );
		}
	}

	public void updateTestCase( @Nonnull WsdlTestCase newTestCase )
	{
		this.testCase = newTestCase;
		testStepLabels.invalidateAll();
		fireTableDataChanged();
	}

	@Override
	public Class<?> getColumnClass( int columnIndex )
	{
		switch( columnIndex )
		{
		case TESTSTEP_LABEL_COLUMN :
			return JLabel.class;
		case TESTSTEP_DISABLED_COLUMN :
			return ImageIcon.class;
		case TESTSTEP_COUNT_COLUMN :
			return Integer.class;
		default :
			throw new IllegalArgumentException( "Illegal column index specified: " + columnIndex
					+ ". Number of columns is " + getColumnCount() + "." );
		}
	}

	@Override
	public int getRowCount()
	{
		if( testCase == null )
			return 0;
		return testCase.getTestStepCount();
	}

	@Override
	public int getColumnCount()
	{
		return 3;
	}

	@Override
	public Object getValueAt( int rowIndex, int columnIndex )
	{
		if( rowIndex >= getRowCount() )
			return null;

		assert testCase != null;

		switch( columnIndex )
		{
		case TESTSTEP_LABEL_COLUMN :
			return testStepLabels.getUnchecked( rowIndex );
		case TESTSTEP_DISABLED_COLUMN :
			return( component.getTestStepIsDisabled( rowIndex ) ? disabledIcon : enabledIcon );
		case TESTSTEP_COUNT_COLUMN :
			return component.getTestStepInvocationCount( rowIndex );
		default :
			throw new IllegalArgumentException( "Illegal column index specified: " + columnIndex
					+ ". Number of columns is " + getColumnCount() + "." );
		}
	}

	public LayoutComponentImpl buildLayout()
	{
		final JTable jTable = new JTable( this );
		jTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
		jTable.getColumnModel().getColumn( 0 ).setMinWidth( 196 );
		jTable.getColumnModel().getColumn( 0 ).setMaxWidth( 196 );
		jTable.getColumnModel().getColumn( 0 ).setResizable( false );
		jTable.getColumnModel().getColumn( 1 ).setMinWidth( 47 );
		jTable.getColumnModel().getColumn( 1 ).setMaxWidth( 47 );
		jTable.getColumnModel().getColumn( 1 ).setResizable( false );
		jTable.getColumnModel().getColumn( 2 ).setMinWidth( 60 );
		jTable.getColumnModel().getColumn( 2 ).setMaxWidth( 60 );
		jTable.getColumnModel().getColumn( 2 ).setResizable( false );
		jTable.setDefaultRenderer( JLabel.class, new TestStepLabelRenderer() );
		jTable.setIntercellSpacing( new Dimension( 11, 0 ) );
		jTable.setGridColor( new Color( 200, 200, 200 ) );
		jTable.getTableHeader().setReorderingAllowed( false );
		jTable.setEnabled( false );
		jTable.setRowHeight( jTable.getRowHeight() + 4 );
		jTable.addMouseListener( new TestStepsTableMouseListener( jTable, component ) );

		return new LayoutComponentImpl( ImmutableMap
				.<String, Object> builder()
				.put( "component",
						new JScrollPane( jTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
								ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER ) ) //
				.put( "componentHeight", 125 ) //
				.put( "componentWidth", 314 ) //
				.put( PropertyLayoutComponentImpl.CONSTRAINTS, "spanx 3, h 125!" ) //
				.build() );
	}
}
