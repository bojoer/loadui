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
package com.eviware.loadui.api.charting.line;

import java.awt.BasicStroke;

/**
 * Defines the style of a stroke in a line chart.
 * 
 * @author dain.nilsson
 */
public enum StrokeStyle
{
	SOLID( 1, 0 ), DASHED( 5, 6 ), DOTTED( 1, 2 );

	private final float[] dash;

	private StrokeStyle( float dash1, float dash2 )
	{
		this.dash = new float[] { dash1, dash2 };
	}

	public BasicStroke getStroke( int strokeWidth )
	{
		return new BasicStroke( strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[] {
				strokeWidth * dash[0], strokeWidth * dash[1] }, 0 );
	}
}