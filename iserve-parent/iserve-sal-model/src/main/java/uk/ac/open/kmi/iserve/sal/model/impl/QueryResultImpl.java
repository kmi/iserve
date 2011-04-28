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

import java.util.List;

import uk.ac.open.kmi.iserve.sal.model.query.QueryResult;
import uk.ac.open.kmi.iserve.sal.model.query.QueryRow;

public class QueryResultImpl implements QueryResult {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8488637623771516054L;

	private List<String> variables;

	private List<QueryRow> queryRows;

	public QueryResultImpl() {
		
	}

	public List<String> getVariables() {
		return variables;
	}

	public void setVariables(List<String> variables) {
		this.variables = variables;
	}

	public List<QueryRow> getQueryRows() {
		return queryRows;
	}

	public void setQueryRows(List<QueryRow> queryRows) {
		this.queryRows = queryRows;
	}

}
