/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.impl.conversion;

import java.util.ArrayList;

import org.springframework.core.convert.converter.Converter;

import com.eviware.loadui.api.ui.table.StringToStringTableModel;
import com.eviware.loadui.api.ui.table.StringToStringTableModel.StringProperty;

public class StringToStringTableModelToStringConverter implements Converter<StringToStringTableModel, String> {

	@Override
	public String convert(StringToStringTableModel source) {
		String result = "";
		ArrayList<StringProperty> data = source.getData();
		for (StringProperty sp : data) {
			result += escape(sp.getName()) + "=" + escape(sp.getValue()) + ";";
		}
		if(result.endsWith(";")){
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}

	private String escape(String string) {
		return string.replaceAll(";", "\\;").replaceAll("=", "\\=");
	}

}
