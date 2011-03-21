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
package uk.ac.open.kmi.iserve2.sal.gwt.model;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BaseTreeModel;

public class OperationInfoModel extends BaseTreeModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -770041684161304387L;

	public OperationInfoModel() {

	}

	public OperationInfoModel(String propName, String value) {
		setPropName(propName);
		setValue(value);
	}

	public void setPropName(String propName) {
		set("propName", propName);
	}

	public void setValue(String value) {
		set("value", value);
	}

	public String getPropName() {
		return get("propName");
	}

	public String getValue() {
		return get("value");
	}

	public OperationInfoModel(String propName, String value, BaseTreeModel[] children) {
		this(propName, value);
		for (int i = 0; i < children.length; i++) {
			add(children[i]);
		}
	}

}
