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

import uk.ac.open.kmi.iserve.sal.gwt.model.ServiceCategoryModel;
import uk.ac.open.kmi.iserve.sal.gwt.model.ServiceListModel;

import com.extjs.gxt.ui.client.Registry;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public interface ServiceBrowseServiceAsync
{

    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see uk.ac.open.kmi.iserve.sal.gwt.client.ServiceBrowseService
     */
    void getService( uk.ac.open.kmi.iserve.sal.model.common.URI p0, AsyncCallback<uk.ac.open.kmi.iserve.sal.model.service.Service> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see uk.ac.open.kmi.iserve.sal.gwt.client.ServiceBrowseService
     */
    void validateSession( AsyncCallback<uk.ac.open.kmi.iserve.sal.model.user.User> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see uk.ac.open.kmi.iserve.sal.gwt.client.ServiceBrowseService
     */
    void login( java.lang.String p0, java.lang.String p1, AsyncCallback<java.lang.String> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see uk.ac.open.kmi.iserve.sal.gwt.client.ServiceBrowseService
     */
    void logout( java.lang.String p0, AsyncCallback<java.lang.Boolean> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see uk.ac.open.kmi.iserve.sal.gwt.client.ServiceBrowseService
     */
    void listServicesByQuery( java.lang.String p0, com.extjs.gxt.ui.client.data.PagingLoadConfig p1, AsyncCallback<com.extjs.gxt.ui.client.data.PagingLoadResult<ServiceListModel>> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see uk.ac.open.kmi.iserve.sal.gwt.client.ServiceBrowseService
     */
    void removeServices( java.util.List p0, AsyncCallback<java.lang.Boolean> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see uk.ac.open.kmi.iserve.sal.gwt.client.ServiceBrowseService
     */
    void reviewService( uk.ac.open.kmi.iserve.sal.model.common.URI p0, uk.ac.open.kmi.iserve.sal.model.review.Rating p1, uk.ac.open.kmi.iserve.sal.model.review.Comment p2, AsyncCallback<java.lang.Boolean> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see uk.ac.open.kmi.iserve.sal.gwt.client.ServiceBrowseService
     */
    void executeQuery( java.lang.String p0, AsyncCallback<uk.ac.open.kmi.iserve.sal.model.query.QueryResult> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see uk.ac.open.kmi.iserve.sal.gwt.client.ServiceBrowseService
     */
    void executeLogQuery( java.lang.String p0, AsyncCallback<uk.ac.open.kmi.iserve.sal.model.query.QueryResult> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see uk.ac.open.kmi.iserve.sal.gwt.client.ServiceBrowseService
     */
    void listTaxonomy( AsyncCallback<java.util.List> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see uk.ac.open.kmi.iserve.sal.gwt.client.ServiceBrowseService
     */
    void getAllCategories( java.lang.String p0, AsyncCallback<java.util.List<ServiceCategoryModel>> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see uk.ac.open.kmi.iserve.sal.gwt.client.ServiceBrowseService
     */
    void getSubCategories( uk.ac.open.kmi.iserve.sal.gwt.model.ServiceCategoryModel p0, AsyncCallback<java.util.List> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see uk.ac.open.kmi.iserve.sal.gwt.client.ServiceBrowseService
     */
    void listServiceClassificationRoots( AsyncCallback<java.util.List> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see uk.ac.open.kmi.iserve.sal.gwt.client.ServiceBrowseService
     */
    void getUser( uk.ac.open.kmi.iserve.sal.model.common.URI p0, AsyncCallback<uk.ac.open.kmi.iserve.sal.model.user.User> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see uk.ac.open.kmi.iserve.sal.gwt.client.ServiceBrowseService
     */
    void updateUser( uk.ac.open.kmi.iserve.sal.model.user.User p0, AsyncCallback<uk.ac.open.kmi.iserve.sal.model.common.URI> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see uk.ac.open.kmi.iserve.sal.gwt.client.ServiceBrowseService
     */
    void addUser( uk.ac.open.kmi.iserve.sal.model.user.User p0, AsyncCallback<uk.ac.open.kmi.iserve.sal.model.common.URI> callback );


    /**
     * Utility class to get the RPC Async interface from client-side code
     */
    public static final class Util 
    { 
        private static ServiceBrowseServiceAsync instance;

        public static final ServiceBrowseServiceAsync getInstance()
        {
            if ( instance == null )
            {
                instance = (ServiceBrowseServiceAsync) GWT.create( ServiceBrowseService.class );
                ServiceDefTarget target = (ServiceDefTarget) instance;
                target.setServiceEntryPoint( GWT.getModuleBaseURL() + "browse" );
            }
            Registry.register(GWT.getModuleBaseURL() + "browse", instance);
            return instance;
        }

        private Util()
        {
            // Utility class should not be instanciated
        }
    }
}
