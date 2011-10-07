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
/*
 *  loadUI, copyright (C) 2009 eviware.com 
 *
 *  loadUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  loadUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */



package com.eviware.loadui.fx.ui.toolbar;

import java.util.Comparator;
import javafx.util.Sequences;
import com.eviware.loadui.api.component.categories.*;

public class GroupOrder extends Comparator {

	//Used for ordering the groups (They will appear in the reverse order).
	protected var groupOrder:String[] = [
		"AGENTS",
		"PROJECTS",
		
		MiscCategory.CATEGORY.toUpperCase(),
		OutputCategory.CATEGORY.toUpperCase(),
		SchedulerCategory.CATEGORY.toUpperCase(),
		FlowCategory.CATEGORY.toUpperCase(),
		AnalysisCategory.CATEGORY.toUpperCase(),
		RunnerCategory.CATEGORY.toUpperCase(),
		GeneratorCategory.CATEGORY.toUpperCase(),
		"TESTCASES"
	];

	public override function compare(o1, o2) {
		def index1 = Sequences.indexOf(groupOrder, o1.toString().toUpperCase());
		def index2 = Sequences.indexOf(groupOrder, o2.toString().toUpperCase());
	
		if (index1 == index2 )
			o1.toString().compareToIgnoreCase(o2.toString())
		else
			index2-index1;
	}
}

