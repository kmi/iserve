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

import com.extjs.gxt.ui.client.data.BaseModelData;

public class ServiceCategoryModel extends BaseModelData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3599342049681768057L;

	private ServiceCategoryModel parent;

	protected ServiceCategoryModel() {

	}

	public ServiceCategoryModel(String id, String uri, ServiceCategoryModel parent, String displayName) {
		setId(id);
		setURI(uri);
		setParent(parent);
		setDisplayName(displayName);
	}

	public void setDisplayName(String displayName) {
		set("displayName", displayName);
	}

	public String getDisplayName() {
		return get("displayName");
	}

	public void setURI(String uri) {
		set("uri", uri);
	}

	public void setId(String id) {
		set("id", id);
	}

	public String getURI() {
		return get("uri");
	}

	public String getId() {
		return get("id");
	}

	public void setParent(ServiceCategoryModel parent) {
		this.parent = parent;
	}

	public ServiceCategoryModel getParent() {
		return this.parent;
	}

	@Override
	public boolean equals(Object obj) {
		if ( obj != null && obj instanceof ServiceCategoryModel ) {
			ServiceCategoryModel mobj = (ServiceCategoryModel) obj;
			return getURI().equals(mobj.getURI());
		}
		return super.equals(obj);
	}

}
