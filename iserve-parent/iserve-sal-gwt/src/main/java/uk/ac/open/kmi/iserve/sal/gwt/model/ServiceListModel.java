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

import java.util.Date;

import com.extjs.gxt.ui.client.data.BaseModel;

public class ServiceListModel extends BaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2179334071190964291L;

	public ServiceListModel() {
		super();
		setURIString("");
		setCreateTime(new Date());
		setAuthor("");
		setLabel("");
		setName("");
	}

	public ServiceListModel(String uriString, String label, String name, Date time, String author) {
		super();
		setURIString(uriString);
		setCreateTime(time);
		setAuthor(author);
		setLabel(label);
		setName(name);
	}

	public void setCreateTime(Date createTime) {
		set("createTime", createTime);
	}

	public Date getCreateTime() {
		return get("createTime");
	}

	public String getURIString() {
		return get("uriString");
	}

	public void setURIString(String uriString) {
		set("uriString", uriString);
	}

	public String getLabel() {
		return get("label");
	}

	public String getName() {
		return get("name");
	}

	public void setLabel(String label) {
		set("label", label);
	}

	public String getAuthor() {
		return get("author");
	}

	public void setAuthor(String author) {
		set("author", author);
	}

	public void setName(String name) {
		set("name", name);
	}
}
