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
package uk.ac.open.kmi.iserve.sal.gwt.model;

import java.util.List;

import uk.ac.open.kmi.iserve.sal.model.query.QueryRow;

import com.extjs.gxt.ui.client.data.BaseModel;

public class QueryResultDisplayModel extends BaseModel {

	private static final long serialVersionUID = 9008819104036134971L;

	private QueryRow queryRow;

	private List<String> variables;

	public QueryResultDisplayModel(List<String> variables, QueryRow queryRow) {
		super();
		this.variables = variables;
		this.queryRow = queryRow;
		for ( String var : this.variables ) {
			String valueString = this.queryRow.getValue(var);
			if ( valueString.contains("://") ) {
				valueString = createLink(valueString);
			}
			set(var, valueString);
		}
	}

	private String createLink(String uri) {
		String result = "<a href=\"" + uri + "\" target=\"_blank\">" + uri + "</a>";
		return result;
	}

}
