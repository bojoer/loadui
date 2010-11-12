package com.eviware.loadui.impl.statistics.store.util;

import java.util.ArrayList;
import java.util.List;

public class InsertStatementHolder
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
