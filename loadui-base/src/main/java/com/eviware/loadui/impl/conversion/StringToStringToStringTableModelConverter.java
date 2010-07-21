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

import org.springframework.core.convert.converter.Converter;

import com.eviware.loadui.api.ui.table.StringToStringTableModel;

public class StringToStringToStringTableModelConverter implements Converter<String, StringToStringTableModel> {

	@Override
	public StringToStringTableModel convert(String source) {
		StringToStringTableModel model = new StringToStringTableModel();
		String[] pairs = source.split(";");
		for (int i = 0; i < pairs.length; i++) {
			String[] p = pairs[i].split("=");
			if(p.length == 2){
				model.addRow(unescape(p[0]), unescape(p[1]));
			}
		}
		return model;
	}

	private String unescape(String string) {
		return string.replaceAll("\\;", ";").replaceAll("\\=", "=");
	}

}
