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
package uk.ac.open.kmi.iserve.sal.gwt.server;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpSession;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.commons.httpclient.HttpException;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;

import uk.ac.open.kmi.iserve.commons.io.URIUtil;
import uk.ac.open.kmi.iserve.commons.vocabulary.LOG;
import uk.ac.open.kmi.iserve.sal.exception.LogException;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.exception.TaxonomyException;
import uk.ac.open.kmi.iserve.sal.exception.UserException;
import uk.ac.open.kmi.iserve.sal.gwt.client.ServiceBrowseService;
import uk.ac.open.kmi.iserve.sal.gwt.client.exception.BrowserException;
import uk.ac.open.kmi.iserve.sal.gwt.model.ServiceCategoryModel;
import uk.ac.open.kmi.iserve.sal.gwt.model.ServiceListModel;
import uk.ac.open.kmi.iserve.sal.gwt.server.servlets.OpenIdServlet;
import uk.ac.open.kmi.iserve.sal.manager.LogManager;
import uk.ac.open.kmi.iserve.sal.manager.ReviewManager;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
import uk.ac.open.kmi.iserve.sal.manager.TaxonomyManager;
import uk.ac.open.kmi.iserve.sal.manager.UserManager;
import uk.ac.open.kmi.iserve.sal.manager.iServeManager;
import uk.ac.open.kmi.iserve.sal.model.common.URI;
import uk.ac.open.kmi.iserve.sal.model.impl.URIImpl;
import uk.ac.open.kmi.iserve.sal.model.log.LogItem;
import uk.ac.open.kmi.iserve.sal.model.query.QueryResult;
import uk.ac.open.kmi.iserve.sal.model.review.Comment;
import uk.ac.open.kmi.iserve.sal.model.review.Rating;
import uk.ac.open.kmi.iserve.sal.model.service.Service;
import uk.ac.open.kmi.iserve.sal.model.taxonomy.Category;
import uk.ac.open.kmi.iserve.sal.model.user.User;
import uk.ac.open.kmi.iserve.sal.util.ModelConverter;

import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ServiceBrowseServiceImpl extends RemoteServiceServlet implements ServiceBrowseService {

	private static final long serialVersionUID = -1084702945631347581L;

	private static final String PREFIXED_TAXONOMY_URI = "file://service-categories-dong.rdfs";

	private String rootUri;
	
	private iServeManager manager;

	public ServiceBrowseServiceImpl(iServeManager manager) {
		this.manager = manager;
		rootUri = "http://" + manager.getConfiguration().getUriPrefix();
	}

	public PagingLoadResult<ServiceListModel> listServicesByQuery(String queryString, PagingLoadConfig config) throws BrowserException {
		List<ServiceListModel> serviceList = new ArrayList<ServiceListModel>();
		ArrayList<ServiceListModel> sublist = new ArrayList<ServiceListModel>();
		if ( queryString != null && queryString.equalsIgnoreCase("") == false ) {
			try {
				Map<String, LogItem> serviceLog = manager.getAllLogItems();
				Model model = manager.getServicesRepositoryConnector().openRepositoryModel();
				QueryResultTable qrt = model.sparqlSelect(queryString);
				if ( qrt != null ) {
					ClosableIterator<QueryRow> iter = qrt.iterator();
					while ( iter.hasNext() ) {
						QueryRow row = iter.next();
						String uriString = row.getValue("s").toString();
						String label = null;
						if ( row.getValue("sl") != null ) {
							label = row.getValue("sl").asLiteral().getValue();
						}
						String str = "";
						String name = "";
						if (label != null && "".equalsIgnoreCase(label) == false ) {
							name = label;
							str = "<span qtip='" + uriString + "'>" + label + "</span>";
						} else {
							URIImpl uri = new URIImpl(uriString);
							name = uri.getLocalName();
							str = "<span qtip='" + uriString + "'>" + uri.getLocalName() + "</span>";
						}
						LogItem logItem = serviceLog.get(uriString);
						if ( logItem != null ) {
							serviceList.add(new ServiceListModel(uriString, str, name, logItem.getTime(), "<a target=\"_blank\" href=\"" + logItem.getAuthor() + "\">" + logItem.getAuthor() + "</a>"));
						}
					}
					iter.close();

					if ( config.getSortInfo().getSortField() != null ) {
						final String sortField = config.getSortInfo().getSortField();
						if ( sortField != null ) {
							Collections.sort(serviceList, config.getSortInfo().getSortDir().comparator(new Comparator<ServiceListModel>() {
								public int compare(ServiceListModel p1, ServiceListModel p2) {
									if ( sortField.equals("label") ) {
										return p1.getName().compareTo(p2.getName());
									} else if ( sortField.equals("author") ) {
										return p1.getAuthor().compareTo(p2.getAuthor());
									}  else if ( sortField.equals("createTime") ) {
										return p1.getCreateTime().compareTo(p2.getCreateTime());
									}
									return 0;
								}
							}));
						}
					}
				}
				serviceLog = null;

				for ( int i = 0; (i < config.getLimit()) && (config.getOffset() + i) < serviceList.size(); i++ ) {
					sublist.add(serviceList.get(config.getOffset() + i));
				}

				model.close();
				model = null;
			} catch (DatatypeConfigurationException e) {
				throw new BrowserException(e);
			}
		}

		PagingLoadResult<ServiceListModel> result = new BasePagingLoadResult<ServiceListModel>(sublist, config.getOffset(), serviceList.size());
		return result;
	}

	public Service getService(URI serviceUri) throws BrowserException {
		if ( null == serviceUri || null == serviceUri.toString() || "".equalsIgnoreCase(serviceUri.toString()) ) {
			throw new BrowserException("Service URI is null");
		}
		Service result = null;
		try {
			result = manager.getService(serviceUri);
		} catch (ServiceException e) {
			throw new BrowserException(e);
		}
//		try {
//			//TODO: Fix get reviews
//			result.setReviews(manager.getReviews(serviceUri));
//		} catch (HttpException e) {
//			// ignore the execptions caused by LUF connector
//			// e.printStackTrace();
//		} catch (IOException e) {
//			// ignore the execptions caused by LUF connector
//			// e.printStackTrace();
//		} catch (ParseException e) {
//			// ignore the execptions caused by LUF connector
//			// e.printStackTrace();
//		}
		return result;
	}

	public boolean removeServices(List<URI> serviceURIs) throws BrowserException {
		User user = validateSession();
		if ( user == null ) return false;
		for ( URI serviceUri : serviceURIs ) {
			try {
				String returnedUri = manager.deleteService(serviceUri.toString());
				// TODO: PUsh loggin to the method above
				if ( returnedUri != null && "".equalsIgnoreCase(returnedUri) == false ) {
					manager.log(user.getFoafId().toString(), LOG.ITEM_DELETING, returnedUri, new Date(), "WebApp");
				}
			} catch (ServiceException e) {
				throw new BrowserException(e);
			} catch (LogException e) {
				throw new BrowserException(e);
			}
		}
		return true;
	}

	public boolean reviewService(URI serviceUri, Rating rating, Comment comment) throws BrowserException {
		User user = validateSession();
		if ( user == null ) {
			return false;
		}
		
		return false;
//		try {
//			// TODO: Implement Review manager 
//			return lufConnector.reviewService(serviceUri, user.getFoafId(), rating, comment);
//		} catch (HttpException e) {
//			throw new BrowserException(e);
//		} catch (IOException e) {
//			throw new BrowserException(e);
//		}
	}

	public QueryResult executeQuery(String queryString) throws BrowserException {
		Model model = manager.getServicesRepositoryConnector().openRepositoryModel();
		QueryResultTable qrt = model.sparqlSelect(queryString);
		model.close();
		model = null;
		return ModelConverter.convertQueryResultTable(qrt);
	}

	public QueryResult executeLogQuery(String queryString) throws BrowserException {
		Model model = manager.getServicesRepositoryConnector().openRepositoryModel();
		QueryResultTable qrt = model.sparqlSelect(queryString);
		model.close();
		model = null;
		return ModelConverter.convertQueryResultTable(qrt);
	}

	public List<URI> listTaxonomy() throws BrowserException {
		List<URI> result = new ArrayList<URI>();
		List<String> taxonomies = manager.listTaxonomy();
		if ( taxonomies != null ) {
			for ( String taxonomy : taxonomies ) {
				result.add(new URIImpl(taxonomy));
			}
		}
		return result;
	}

	public List<ServiceCategoryModel> getAllCategories(String uri) throws BrowserException {
		List<Category> allCategories = null;
		try {
			if ( uri.equalsIgnoreCase("Prefixed") ) {
				allCategories = manager.loadTaxonomy(PREFIXED_TAXONOMY_URI);
			} else {
				allCategories = manager.loadTaxonomy(uri);
			}
		} catch (TaxonomyException e) {
			throw new BrowserException(e);
		}
		return convertAllCategories(allCategories);
	}

	private List<ServiceCategoryModel> convertAllCategories(List<Category> allCategories) {
		List<ServiceCategoryModel> result = new ArrayList<ServiceCategoryModel>();
		result.add(new ServiceCategoryModel(UUID.randomUUID().toString(), "All", null, "All"));
		if ( allCategories != null ) {
			List<ServiceCategoryModel> rootCategories = convertRoot(allCategories);
			result.addAll(rootCategories);
			for ( ServiceCategoryModel rootCategory : rootCategories ) {
				convertAllSubCategories(result, rootCategory, allCategories);
			}
			rootCategories = null;
		}
		return result;
	}

	private List<ServiceCategoryModel> convertRoot(List<Category> allCategories) {
		List<ServiceCategoryModel> result = new ArrayList<ServiceCategoryModel>();
		for ( Category category : allCategories ) {
			if ( null == category.getParentUri() ) {
				result.add(new ServiceCategoryModel(UUID.randomUUID().toString(), category.getURI().toString(), null, generateDisplayName(category)));
			}
		}
		return result;
	}

	private void convertAllSubCategories(List<ServiceCategoryModel> result, ServiceCategoryModel rootCategory, List<Category> allCategories) {
		if ( null == rootCategory )
			return;
		List<ServiceCategoryModel> subCategories = convertSubCategories(rootCategory, allCategories);
		if ( null == subCategories )
			return;
		result.addAll(subCategories);
		for ( ServiceCategoryModel subCategory : subCategories ) {
			convertAllSubCategories(result, subCategory, allCategories);
		}
	}

	private List<ServiceCategoryModel> convertSubCategories(ServiceCategoryModel rootCategory, List<Category> allCategories) {
		if ( null == rootCategory )
			return null;
		List<ServiceCategoryModel> result = new ArrayList<ServiceCategoryModel>();
		for ( Category category : allCategories ) {
			if ( category.getParentUri() != null ) {
				String parentCategoryUriString = category.getParentUri().toString();
				int i = 0;
				if ( i == 0 && parentCategoryUriString.equalsIgnoreCase(rootCategory.getURI()) ) {
					result.add(new ServiceCategoryModel(UUID.randomUUID().toString(), category.getURI().toString(), rootCategory, generateDisplayName(category)));
					i = 1;
				}
			}
		}
		return result;
	}

	private String generateDisplayName(Category category) {
		String categoryUriString = category.getURI().toString();
		if ( null == category.getParentUri() ) {
			categoryUriString = URLDecoder.decode(categoryUriString);
			int thirdSlash = categoryUriString.indexOf('/', 7); // after "http://"
			int lastSlash = categoryUriString.lastIndexOf('#');
			if ( lastSlash < 0  || thirdSlash < 0 )
				return categoryUriString;
			return "<span qtip='" + categoryUriString + "'>" + categoryUriString.substring(0, thirdSlash + 1) + "..." + categoryUriString.substring(lastSlash)  + "</span>";
		} else {
			String categoryName = URIUtil.getLocalName(category.getURI().toString());
			categoryName = "<span qtip='" + categoryUriString + "'>" + URLDecoder.decode(categoryName)  + "</span>";
			return categoryName;
		}
	}

	public List<ServiceCategoryModel> getSubCategories(ServiceCategoryModel serviceCategory) throws BrowserException {
		return null;
	}

	public List<ServiceCategoryModel> listServiceClassificationRoots() throws BrowserException {
		// TODO Auto-generated method stub
		return null;
	}

	public User getUser(URI openId) throws BrowserException {
		try {
			return manager.getUser(openId);
		} catch (UserException e) {
			throw new BrowserException(e);
		}
	}

	public URI updateUser(User user) throws BrowserException {
		try {
			return manager.updateUser(user);
		} catch (UserException e) {
			throw new BrowserException(e);
		}
	}

	public URI addUser(User user) throws BrowserException {
		try {
			return manager.addUser(user);
		} catch (UserException e) {
			throw new BrowserException(e);
		}
	}

	public String login(String openId, String url) {
		if (openId == null || openId.length() == 0) {
			return null;
		}
		return rootUri + "/iServeBrowser/" + getAuthenticationURL(openId, rootUri);
	}

	public boolean logout(String userId) {
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if ( null != httpSession ) {
			httpSession.removeAttribute("logged-in");
		}
		return true;
	}

	public User validateSession() throws BrowserException {
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if ( (null != httpSession) && (null != httpSession.getAttribute("logged-in")) ) {
			try {
				URI openid = new URIImpl(httpSession.getAttribute("logged-in").toString());
				User user = manager.getUser(openid);
				return user;
			} catch (UserException e) {
				throw new BrowserException(e);
			}
		}
		return null;
	}

	public static String getAuthenticationURL(String openIdName, String url) {
		// This is where a redirect for the response was supposed to occur; however, since GWT doesn't allow that
		// on responses coming from a GWT servlet, only a redirect via the web page is made.

		return MessageFormat.format("{0}?{1}=true&{2}={3}&returnToUrl={4}", "CallOpenID", OpenIdServlet.authParameter, OpenIdServlet.nameParameter, openIdName, URLEncoder.encode(url));
	}

//	/* (non-Javadoc)
//	 * @see com.google.gwt.user.server.rpc.RemoteServiceServlet#doGetSerializationPolicy(javax.servlet.http.HttpServletRequest, java.lang.String, java.lang.String)
//	 * 
//	 * Overriden to support the deployment in different folders (e.g., behind a proxy) 
//	 * and still be able to load the serialization policy file of GWT
//	 * Taken from: 
//	 * https://groups.google.com/forum/?fromgroups#!topic/google-web-toolkit/j3tAhgbGk08 
//	 */
//	protected SerializationPolicy doGetSerializationPolicy( HttpServletRequest request, String moduleBaseURL, String 
//			strongName) { 
//		// The request can tell you the path of the web app relative to the 
//		// container root. 
//
//		SerializationPolicy serializationPolicy = null; 
//		String serializationPolicyFilePath = ""; 
//		InputStream is = null; 
//
//		String contextPath = request.getContextPath(); 
//		URL moduleUrl = null;
//		String modulePath = null; 
//		if (moduleBaseURL != null) { 
//			try { 
//				moduleUrl = new URL(moduleBaseURL);
//				modulePath = moduleUrl.getPath(); 
//			} catch (MalformedURLException ex) { 
//				// log the information, we will default 
//				getServletContext().log("Malformed moduleBaseURL: " + 
//						moduleBaseURL, ex); 
//			} 
//		} 
//		else {                
//			//Just quit, if we do not know the module base. (07/11/08 -	Danny) 
//			return serializationPolicy; 
//		} 
//
//		if (modulePath == null ) { 
//			String message = "ERROR: The module path requested, " 
//				+ modulePath 
//				+ ", is null, " 
//				+ contextPath 
//				+ ".  Your module may not be properly configured or your client and server code maybe out of date."; 
//			getServletContext().log(message); 
//		} else {  
//
//			// TODO: Ensure the module and the request are for the same host, e.g., proxied server  
//			if (moduleUrl != null) {
//				String policyFile = "http://" + moduleUrl.getHost() + contextPath + modulePath + strongName;
//
//				serializationPolicyFilePath = 
//					SerializationPolicyLoader.getSerializationPolicyFileName(policyFile);
//
//				getServletContext().log("XXX: Loading file " + policyFile);
//
//				// Open the RPC resource file read its contents. 
//				is = getServletContext().getResourceAsStream(
//						serializationPolicyFilePath); 
//			} 
//			try { 
//				if (is != null) { 
//					try { 
//						serializationPolicy = 
//							SerializationPolicyLoader.loadFromStream(is, null); 
//					} catch (ParseException e) { 
//						getServletContext().log( 
//								"ERROR: Failed to parse the policy file '" 
//								+ serializationPolicyFilePath + "'", e); 
//					} catch (IOException e) { 
//						getServletContext().log( 
//								"ERROR: Could not read the policy file '" 
//								+ serializationPolicyFilePath + "'", e); 
//					} 
//				} else { 
//					String message = "ERROR: The serialization policy file '" 
//						+ serializationPolicyFilePath 
//						+ "' was not found; did you forget to include it in this deployment?"; 
//					getServletContext().log(message); 
//				} 
//			} finally { 
//				if (is != null) { 
//					try { 
//						is.close(); 
//					} catch (IOException e) { 
//						// Ignore this error 
//					} 
//				} 
//			} 
//		}
//		return serializationPolicy; 
//	}
	
}
