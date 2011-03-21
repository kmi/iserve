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
package uk.ac.open.kmi.iserve2.sal.model.query;

import java.io.Serializable;
import java.util.Map;

public interface QueryRow extends Serializable {

	public Map<String, String> getQueryRowData();

	public Map<String, String> getQueryRowDatatype();

	public void setQueryRow(Map<String, String> rowDatatype, Map<String, String> rowData);

	public String getValue(String variable);

	public boolean isUri(String variable);

	public boolean isLiteral(String variable);

	public boolean isBlankNode(String variable);

	public String getLang(String variable);

	public String getDatatype(String variable);

}
