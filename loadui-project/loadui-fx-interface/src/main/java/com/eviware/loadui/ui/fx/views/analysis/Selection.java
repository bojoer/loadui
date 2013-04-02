/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.ui.fx.views.analysis;

import javafx.scene.control.TreeItem;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.util.TreeUtils.LabeledStringValue;

@Immutable
public class Selection
{
	public final String source;
	public final String statistic;
	public final String variable;
	public final StatisticHolder holder;

	Selection( @Nonnull TreeItem<Labeled> selected, boolean selectedIsSource )
	{
		if( selectedIsSource )
		{
			source = ( ( LabeledStringValue )selected.getValue() ).getValue();
			statistic = selected.getParent().getValue().getLabel();
			variable = selected.getParent().getParent().getValue().getLabel();
			holder = ( StatisticHolder )selected.getParent().getParent().getParent().getValue();
		}
		else
		{
			source = null;
			statistic = selected.getValue().getLabel();
			variable = selected.getParent().getValue().getLabel();
			holder = ( StatisticHolder )selected.getParent().getParent().getValue();
		}
	}
}
