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
package uk.ac.open.kmi.iserve.sal.gwt.client;

import java.util.List;

import uk.ac.open.kmi.iserve.sal.gwt.client.exception.BrowserException;
import uk.ac.open.kmi.iserve.sal.gwt.model.ServiceCategoryModel;
import uk.ac.open.kmi.iserve.sal.gwt.model.ServiceListModel;
import uk.ac.open.kmi.iserve.sal.model.common.URI;
import uk.ac.open.kmi.iserve.sal.model.query.QueryResult;
import uk.ac.open.kmi.iserve.sal.model.review.Comment;
import uk.ac.open.kmi.iserve.sal.model.review.Rating;
import uk.ac.open.kmi.iserve.sal.model.service.Service;
import uk.ac.open.kmi.iserve.sal.model.user.User;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("browse")
public interface ServiceBrowseService extends RemoteService {

	public PagingLoadResult<ServiceListModel> listServicesByQuery(String queryString, PagingLoadConfig config) throws BrowserException;

	public Service getService(URI serviceUri) throws BrowserException;

	public boolean removeServices(List<URI> serviceURIs) throws BrowserException;

	public boolean reviewService(URI serviceUri, Rating rating, Comment comment) throws BrowserException; 

	public QueryResult executeQuery(String queryString) throws BrowserException;

	public QueryResult executeLogQuery(String queryString) throws BrowserException;

	public List<URI> listTaxonomy() throws BrowserException;

	public List<ServiceCategoryModel> getAllCategories(String uri) throws BrowserException;

	public List<ServiceCategoryModel> getSubCategories(ServiceCategoryModel serviceCategory) throws BrowserException;

	public List<ServiceCategoryModel> listServiceClassificationRoots() throws BrowserException;

	public User getUser(URI openId) throws BrowserException;

	public URI updateUser(User user) throws BrowserException;

	public URI addUser(User user) throws BrowserException;

	public String login(String openId, String url);

	public boolean logout(String userId);

	public User validateSession() throws BrowserException;

}
