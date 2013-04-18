/*
   Copyright 2012  Knowledge Media Institute - The Open University

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
package uk.ac.open.kmi.iserve.sal.manager;

import java.net.URI;

import uk.ac.open.kmi.iserve.commons.io.RDFRepositoryConnector;
import uk.ac.open.kmi.iserve.sal.exception.UserException;
import uk.ac.open.kmi.iserve.sal.model.user.User;

/**
 * Class Description
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public interface UserManager {

	public abstract User getUser(URI openId) throws UserException;

	public abstract User getUser(String userName) throws UserException;

	public abstract URI addUser(URI foafId, URI openId, String userName,
			String password) throws UserException;

	public abstract URI addUser(User user) throws UserException;

	public abstract boolean removeUser(URI foafId) throws UserException;

	public abstract boolean removeUser(String userName) throws UserException;

	public abstract URI updateUser(User user) throws UserException;
	
	// TODO: should not be here
	public RDFRepositoryConnector getUsersRepositoryConnector();

}