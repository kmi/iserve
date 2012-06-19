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
package uk.ac.open.kmi.iserve.sal.manager.impl;

import java.security.NoSuchAlgorithmException;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.Variable;
import org.openrdf.rdf2go.RepositoryModel;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.iserve.commons.io.RDFRepositoryConnector;
import uk.ac.open.kmi.iserve.commons.vocabulary.FOAF;
import uk.ac.open.kmi.iserve.commons.vocabulary.USER;
import uk.ac.open.kmi.iserve.sal.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.exception.UserException;
import uk.ac.open.kmi.iserve.sal.manager.UserManager;
import uk.ac.open.kmi.iserve.sal.model.common.URI;
import uk.ac.open.kmi.iserve.sal.model.impl.URIImpl;
import uk.ac.open.kmi.iserve.sal.model.impl.UserImpl;
import uk.ac.open.kmi.iserve.sal.model.user.User;
import uk.ac.open.kmi.iserve.sal.util.MD5;

public class UserManagerRdf implements UserManager {

	private org.ontoware.rdf2go.model.node.URI hasUserName;

	private org.ontoware.rdf2go.model.node.URI hasPassword;

	private RDFRepositoryConnector repoConnector;

	/**
	 * Constructor for the User Manager. Protected to avoid external access.
	 * Any access to this should take place through the iServeManager
	 * 
	 * @param configuration
	 * @throws RepositoryException
	 */
	protected UserManagerRdf(SystemConfiguration configuration) throws RepositoryException {
		repoConnector = new RDFRepositoryConnector(configuration.getUserServerUrl(), configuration.getUserRepoName());
		//TODO: This should be deployment independent
		hasUserName = new org.ontoware.rdf2go.model.node.impl.URIImpl("http://" + 
				configuration.getUriPrefix() + USER.HAS_USER_NAME);
		hasPassword = new org.ontoware.rdf2go.model.node.impl.URIImpl("http://" + 
				configuration.getUriPrefix() + USER.HAS_PASSWORD);
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.UserManager#getUser(uk.ac.open.kmi.iserve.sal.model.common.URI)
	 */
	@Override
	public User getUser(URI openId) throws UserException {
		if ( null == openId || null == openId.toString() || "".equalsIgnoreCase(openId.toString())) {
			throw new UserException("OpenID is null");
		}
		User result = new UserImpl();
		result.setOpenId(openId);
		//TODO: Correct this. The vocabulary should not depend on the deployment,
		// only the instances should depend!!
		String queryString = "SELECT ?p ?u ?pwd WHERE { " +
			"?p " + FOAF.foafOpenId.toSPARQL() + " " + openId.toSPARQL() + " . " +
			"?p " + hasUserName.toSPARQL() + " ?u . " +
			"?p " + hasPassword.toSPARQL() + " ?pwd . }";
		RepositoryModel repoModel = repoConnector.openRepositoryModel();
		QueryResultTable qrt = repoModel.sparqlSelect(queryString);
		ClosableIterator<QueryRow> iter = qrt.iterator();
		while ( iter.hasNext() ) {
			QueryRow row = iter.next();
			String foafIdString = row.getValue("p").toString();
			String userNameString = row.getValue("u").toString();
			String passwordString = row.getValue("pwd").toString();

			result.setFoafId(new URIImpl(foafIdString));
			result.setUserName(userNameString);
			result.setPassword(passwordString);
		}
		iter.close();
		repoConnector.closeRepositoryModel(repoModel);
		repoModel = null;
		return result;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.UserManager#getUser(java.lang.String)
	 */
	@Override
	public User getUser(String userName) throws UserException {
		if ( null == userName || "" == userName ) {
			throw new UserException("User name is null");
		}
		User result = new UserImpl();
		result.setUserName(userName);
		String queryString = "SELECT ?p ?o ?pwd WHERE { " +
			"?p " + FOAF.foafOpenId.toSPARQL() + " ?o . " +
			"?p " + hasUserName.toSPARQL() + " \"" + userName +  "\" . " +
			"?p " + hasPassword.toSPARQL() + " ?pwd . }";
		RepositoryModel repoModel = repoConnector.openRepositoryModel();
		QueryResultTable qrt = repoModel.sparqlSelect(queryString);
		ClosableIterator<QueryRow> iter = qrt.iterator();
		while ( iter.hasNext() ) {
			QueryRow row = iter.next();
			String foafIdString = row.getValue("p").toString();
			String openIdString = row.getValue("o").toString();
			String passwordString = row.getValue("pwd").toString();
			result.setFoafId(new URIImpl(foafIdString));
			result.setOpenId(new URIImpl(openIdString));
			result.setPassword(passwordString);
		}
		iter.close();
		repoConnector.closeRepositoryModel(repoModel);
		repoModel = null;
		return result;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.UserManager#addUser(uk.ac.open.kmi.iserve.sal.model.common.URI, uk.ac.open.kmi.iserve.sal.model.common.URI, java.lang.String, java.lang.String)
	 */
	@Override
	public URI addUser(URI foafId, URI openId, String userName, String password) throws UserException {
		if ( null == openId || null == openId.toString() || "" == openId.toString() ) {
			throw new UserException("User's OpenID is null");
		}

		RepositoryModel repoModel = repoConnector.openRepositoryModel();

		QueryResultTable qrt = repoModel.sparqlSelect("SELECT ?u WHERE { \n" +
				" ?u " + hasUserName.toSPARQL() + " \"" + userName + "\" . }");
		if ( qrt != null ) {
			ClosableIterator<QueryRow> iter = qrt.iterator();
			if ( iter.hasNext() ) {
				QueryRow row = iter.next();
				if ( row.getValue("u") != null ) {
					iter.close();
					iter = null;
					repoConnector.closeRepositoryModel(repoModel);
					repoModel = null;
					throw new UserException("User name " + userName + " is not available");
				}
			}
		}

		qrt = repoModel.sparqlSelect("SELECT ?o WHERE { \n" +
				foafId.toSPARQL() + " " + FOAF.foafOpenId.toSPARQL() + " ?o . }");
		if ( qrt != null ) {
			ClosableIterator<QueryRow> iter = qrt.iterator();
			if ( iter.hasNext() ) {
				QueryRow row = iter.next();
				if ( row.getValue("o") != null ) {
					iter.close();
					iter = null;
					repoConnector.closeRepositoryModel(repoModel);
					repoModel = null;
					throw new UserException("FOAF ID " + foafId.toString() + " is not available");
				}
			}
		}

		org.ontoware.rdf2go.model.node.URI openIdInst = new org.ontoware.rdf2go.model.node.impl.URIImpl(openId.toString());
		org.ontoware.rdf2go.model.node.URI foafIdInst = new org.ontoware.rdf2go.model.node.impl.URIImpl(foafId.toString());
		if ( foafIdInst != null ) {
			repoModel.addStatement(foafIdInst, FOAF.foafOpenId, openIdInst);
		}
		if ( userName != null && userName != "" ) {
			repoModel.addStatement(foafIdInst, hasUserName, userName);
		}
		if ( password != null && password != "" ) {
			try {
				repoModel.addStatement(foafIdInst, hasPassword, MD5.digest(password));
			} catch (ModelRuntimeException e) {
				throw new UserException(e);
			} catch (NoSuchAlgorithmException e) {
				throw new UserException(e);
			}
		}
		repoConnector.closeRepositoryModel(repoModel);
		repoModel = null;
		return foafId;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.UserManager#addUser(uk.ac.open.kmi.iserve.sal.model.user.User)
	 */
	@Override
	public URI addUser(User user) throws UserException {
		if ( null == user ) {
			throw new UserException("User is null");
		}
		return addUser(user.getFoafId(), user.getOpenId(), user.getUserName(), user.getPassword());
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.UserManager#removeUser(uk.ac.open.kmi.iserve.sal.model.common.URI)
	 */
	@Override
	public void removeUser(URI foafId) throws UserException {
		if ( null == foafId || null == foafId.toString() || "" == foafId.toString() ) {
			throw new UserException("FOAF ID is null");
		}
		RepositoryModel repoModel = repoConnector.openRepositoryModel();
		org.ontoware.rdf2go.model.node.URI foafIdInst = new org.ontoware.rdf2go.model.node.impl.URIImpl(foafId.toString());
		if ( foafIdInst != null ) {
			repoModel.removeStatements(foafIdInst, FOAF.foafOpenId, Variable.ANY);
			repoModel.removeStatements(foafIdInst, hasUserName, Variable.ANY);
			repoModel.removeStatements(foafIdInst, hasPassword, Variable.ANY);
		}
		repoConnector.closeRepositoryModel(repoModel);
		repoModel = null;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.UserManager#removeUser(java.lang.String)
	 */
	@Override
	public void removeUser(String userName) throws UserException {
		User user = getUser(userName);
		if ( null == user ) {
			throw new UserException("Cannot find the user with username:" + userName);
		}
		removeUser(user.getFoafId());
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.UserManager#updateUser(uk.ac.open.kmi.iserve.sal.model.user.User)
	 */
	@Override
	public URI updateUser(User user) throws UserException {
		if ( user == null || user.getOpenId() == null || user.getOpenId().toString() == null || 
				user.getOpenId().toString() == "" ) {
			return null;
		}

		User userInRepo = getUser(user.getOpenId());
		if ( userInRepo == null || userInRepo.getPassword() == null || userInRepo.getPassword().equalsIgnoreCase("") ) {
			return null;
		}

		// check password
		try {
			String pwd = MD5.digest(user.getPassword());
			if ( pwd.equals(userInRepo.getPassword()) == false ) {
				throw new UserException("The Password is incorrect!");
			}
			if ( user.getNewPassword() != null && user.getNewPassword() != "") {
				user.setPassword(user.getNewPassword());
			}
		} catch (NoSuchAlgorithmException e) {
			throw new UserException(e);
		}

		removeUser(userInRepo.getFoafId());
		return addUser(user);
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.UserManager#getRepositoryConnector()
	 */
	@Override
	public RDFRepositoryConnector getUsersRepositoryConnector() {
		return repoConnector;
	}

}
