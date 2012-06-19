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
package uk.ac.open.kmi.iserve.sal.gwt.server.servlets;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import uk.ac.open.kmi.iserve.commons.io.IOUtil;
import uk.ac.open.kmi.iserve.commons.vocabulary.LOG;
import uk.ac.open.kmi.iserve.sal.exception.LogException;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.exception.UserException;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;
import uk.ac.open.kmi.iserve.sal.model.common.URI;
import uk.ac.open.kmi.iserve.sal.model.impl.URIImpl;

public class UploadFileServlet extends HttpServlet {

	private static final long serialVersionUID = 7327068834980432770L;

	private ManagerSingleton iserveManager;

	public UploadFileServlet(ManagerSingleton iserveManager) {
		super();
		this.iserveManager = iserveManager;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String radio = "", url = "", content = "";
		FileItem fileItem = null;
		// parse the request
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);

		// get user's FOAF ID
		String agentId = "";
		HttpSession httpSession = request.getSession(false);
		if ( null != httpSession ) {
			agentId = httpSession.getAttribute("logged-in").toString();
		}
		URI agentUri = null;
		try {
			agentUri = iserveManager.getUser(new URIImpl(agentId)).getFoafId();
		} catch (UserException e1) {
			throw new ServletException(e1);
		}

		try {
			List items = upload.parseRequest(request);
			Iterator it = items.iterator();
          
			while ( it.hasNext() ) {
				FileItem item = (FileItem) it.next();
				if ( item.isFormField() == true ) {
					if ( item.getFieldName().equalsIgnoreCase("radio") )
						radio = item.getString();
					else if ( item.getFieldName().equalsIgnoreCase("url") )
						url = item.getString();
					else if ( item.getFieldName().equalsIgnoreCase("content") )
						content = item.getString();
				} else if ( item.isFormField() == false ) {
					if ( item.getFieldName().equalsIgnoreCase("file") )
						fileItem = item;
				}
			}
		} catch(FileUploadException e) {
//			response.setStatus(500);
//			e.printStackTrace(response.getWriter());
			throw new ServletException(e);
		} catch (Exception e) {
//			response.setStatus(500);
//			e.printStackTrace(response.getWriter());
			throw new ServletException(e);
		}

		try {
			String serviceUri = "";
			if ( radio.equalsIgnoreCase("url") ) {
				URI uri = new URIImpl(url);
				URI returnedUri = addService(uri.getLocalName(), IOUtil.readString(new URL(url)), url, agentUri.toString());
				if ( returnedUri != null )
					serviceUri = returnedUri.toString();
			} else if ( radio.equalsIgnoreCase("content") ) {
				URI returnedUri = addService(null, content, null, agentUri.toString());
				if ( returnedUri != null )
					serviceUri = returnedUri.toString();
			} else if ( radio.equalsIgnoreCase("file") ) {
				URI returnedUri = addService(fileItem.getName(), IOUtil.readString(fileItem.getInputStream()), null, agentUri.toString());
				if ( returnedUri != null )
					serviceUri = returnedUri.toString();
			}
			response.setStatus(201);
			response.setContentType("text/plain");
			if ( serviceUri != null && serviceUri != "" ) {
				response.getWriter().write("Successfully add the new service to the repository, the URI is " + serviceUri);
			} else {
				response.getWriter().write("Fail to add the new service to the repository");
			}
		} catch (ServiceException e) {
//			response.setStatus(500);
//			e.printStackTrace(response.getWriter());
			throw new ServletException(e);
		} catch (LogException e) {
//			response.setStatus(500);
//			e.printStackTrace(response.getWriter());
			throw new ServletException(e);
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().write("this is upload servlet");
	}

	private URI addService(String fileName, String serviceDescription, String sourceUri, String agentUri) throws ServiceException, LogException {
		String serviceUri = iserveManager.addService(fileName, serviceDescription, sourceUri);
		// TODO: We should abstract the higher levels from the logging of Actions
		iserveManager.log(agentUri, LOG.ITEM_CREATION, serviceUri, new Date(), "WebApp");
		return new URIImpl(serviceUri);
	}

}
