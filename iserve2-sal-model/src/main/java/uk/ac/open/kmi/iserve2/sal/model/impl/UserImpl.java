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
package uk.ac.open.kmi.iserve2.sal.model.impl;

import uk.ac.open.kmi.iserve2.sal.model.common.URI;
import uk.ac.open.kmi.iserve2.sal.model.user.User;

public class UserImpl implements User {

	private static final long serialVersionUID = 6844889551544098431L;

	private String userName;

	private String password;

	private String newPassword;

	private URI openId;

	private URI foafId;

	public URI getFoafId() {
		return foafId;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public URI getOpenId() {
		return openId;
	}

	public String getPassword() {
		return password;
	}

	public String getUserName() {
		return userName;
	}

	public void setFoafId(URI foafId) {
		this.foafId = foafId;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public void setOpenId(URI openId) {
		this.openId = openId;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}
