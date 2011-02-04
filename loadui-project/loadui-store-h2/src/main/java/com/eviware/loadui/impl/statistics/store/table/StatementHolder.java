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
package com.eviware.loadui.impl.statistics.store.table;

import java.util.ArrayList;
import java.util.List;

public class StatementHolder
{

	private String statementSql;

	private List<String> argumentNameList = new ArrayList<String>();

	public void addArgument( String name )
	{
		argumentNameList.add( name );
	}

	public String getStatementSql()
	{
		return statementSql;
	}

	public void setStatementSql( String statementSql )
	{
		this.statementSql = statementSql;
	}

	public List<String> getArgumentNameList()
	{
		return argumentNameList;
	}

}
