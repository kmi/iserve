/*
   Copyright ${year}  Knowledge Media Institute - The Open University

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package uk.ac.open.kmi.iserve.sal.model.impl;

import java.util.Map;

import uk.ac.open.kmi.iserve.sal.model.query.QueryRow;

public class QueryRowImpl implements QueryRow {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6528680539087013048L;

	private Map<String, String> rowData;

	private Map<String, String> rowDatatype;

	public QueryRowImpl() {
		
	}

	public Map<String, String> getQueryRowData() {
		return rowData;
	}

	public Map<String, String> getQueryRowDatatype() {
		return rowDatatype;
	}

	public void setQueryRow(Map<String, String> rowDatatype, Map<String, String> rowData) {
		this.rowDatatype = rowDatatype;
		this.rowData = rowData;
	}

	public String getValue(String var) {
		return rowData.get(var);
	}

	public String getDatatype(String var) {
		String type = rowDatatype.get(var);
		if ( type.startsWith("datatype:") ) {
			return type.substring("datatype:".length());
		} else  if ( type.startsWith("lang:") ) {
			return "literal";
		} else {
			return type;
		}
	}

	public String getLang(String var) {
		String type = rowDatatype.get(var);
		if ( type.startsWith("lang:") ) {
			return type.substring("lang:".length());
		} else {
			return "";
		}
	}

	public boolean isBlankNode(String var) {
		if ( rowDatatype.get(var).equalsIgnoreCase("blank") )
			return true;
		return false;
	}

	public boolean isLiteral(String var) {
		if ( rowDatatype.get(var).startsWith("datatype:") || rowDatatype.get(var).startsWith("lang:")
				|| rowDatatype.get(var).startsWith("literal") )
			return true;
		return false;
	}

	public boolean isUri(String var) {
		if ( rowDatatype.get(var).equalsIgnoreCase("uri") )
			return true;
		return false;
	}

}
